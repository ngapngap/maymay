package com.may.phieuluuthuthapsao;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;

/**
 * Tự cập nhật app từ GitHub Releases của ngapngap/maymay.
 *
 * Luồng: JS trong game (www/index.html) kiểm tra
 * https://api.github.com/repos/ngapngap/maymay/releases/latest, nếu có
 * bản mới thì gọi downloadAndInstall({url}) với link file APK của release.
 * Plugin tải APK bằng DownloadManager, rồi mở trình cài đặt Android
 * qua FileProvider. Nếu máy chưa bật "Cho phép cài đặt từ nguồn không
 * rõ" thì mở màn hình cài đặt đó cho người dùng bật rồi bấm lại.
 */
@CapacitorPlugin(name = "UpdatePlugin")
public class UpdatePlugin extends Plugin {

    private long downloadId = -1;
    private BroadcastReceiver receiver;

    @PluginMethod
    public void downloadAndInstall(PluginCall call) {
        String url = call.getString("url");
        if (url == null || url.isEmpty()) {
            call.reject("Thiếu URL file APK");
            return;
        }
        Context ctx = getContext();

        // Android 8+: cài app ngoài cần quyền "Install unknown apps".
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!ctx.getPackageManager().canRequestPackageInstalls()) {
                JSObject ret = new JSObject();
                ret.put("needsPermission", true);
                call.resolve(ret);
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + ctx.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
                return;
            }
        }

        DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
        req.setTitle("Phiêu Lưu Thu Thập Sao — bản mới");
        req.setDescription("Đang tải bản cập nhật cho Mây");
        req.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setDestinationInExternalFilesDir(
                ctx, Environment.DIRECTORY_DOWNLOADS, "maymay-update.apk");
        req.setMimeType("application/vnd.android.package-archive");

        if (receiver != null) {
            try { ctx.unregisterReceiver(receiver); } catch (Exception ignored) {}
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    openInstaller();
                }
            }
        };
        ctx.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadId = dm.enqueue(req);
        JSObject ret = new JSObject();
        ret.put("started", true);
        call.resolve(ret);
    }

    private void openInstaller() {
        Context ctx = getContext();
        File apk = new File(
                ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "maymay-update.apk");
        if (!apk.exists()) {
            return;
        }
        Uri uri = FileProvider.getUriForFile(
                ctx, ctx.getPackageName() + ".fileprovider", apk);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ctx.startActivity(intent);
    }
}
