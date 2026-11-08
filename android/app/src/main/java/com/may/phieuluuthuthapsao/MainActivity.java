package com.may.phieuluuthuthapsao;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Plugin tự cập nhật (tải APK từ GitHub Releases rồi mở trình cài đặt).
        registerPlugin(UpdatePlugin.class);
        super.onCreate(savedInstanceState);
    }
}
