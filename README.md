# Phiêu Lưu Thu Thập Sao — Game của Mây

Web game nhẹ nhàng cho bé Mây (7 tuổi): phiêu lưu thu thập sao, vừa chơi
vừa học chữ cái tiếng Việt và cộng trừ trong phạm vi 20, có Phòng thay đồ
cho Mây và chế độ ghép hình (trả lời đúng nhận mảnh ghép, đủ 6 mảnh thì
ghép thành tranh).

## Lấy file APK

- Vào tab **Actions** của repo, chọn lần chạy **Build APK** mới nhất,
  tải artifact `phieu-luu-thu-thap-sao-apk`.
- Khi có tag `v*` (ví dụ `v1.2`), workflow tự tạo **Release** và đính kèm
  file APK ở tab **Releases**.
- Cài trên điện thoại: mở file APK, bật **Cho phép cài đặt từ nguồn không
  rõ** cho app này khi máy hỏi, rồi bấm **Cài đặt**. App chơi offline,
  không cần mạng.

## Tự cập nhật trong app

Mỗi lần mở app, app tự kiểm tra Release mới nhất của repo này
(`api.github.com/repos/ngapngap/maymay/releases/latest`) và so với bản
đang chạy (hiện tại 1.2, versionCode 3). Nếu có bản mới hơn, app hiện
nút **Cập nhật bản mới**: bấm thì app tải file APK của release đó về
(plugin `UpdatePlugin`, dùng DownloadManager) rồi tự mở trình cài đặt
Android qua FileProvider. Nếu máy chưa bật cài đặt từ nguồn không rõ,
app sẽ mở màn hình cài đặt đó để bật rồi bấm cập nhật lại.

Muốn phát hành bản mới: sửa `versionCode`/`versionName` trong
`android/app/build.gradle` và số phiên bản trong `www/index.html`
(`CURRENT`), push lên `main`, rồi tạo tag `vX.Y` (ví dụ `v1.3`) — lúc
đó workflow sẽ build và tạo Release kèm APK.

## Cấu trúc repo

- `www/index.html` — game hoàn chỉnh (1 file, chơi offline). Ảnh game nằm
  trong `www/assets/` (xem ghi chú bên dưới).
- `www/assets/*.webp.b64` — ảnh game lưu dưới dạng base64 text.
- `android/` — project Android Capacitor 6, package
  `com.may.phieuluuthuthapsao`, app name "Phiêu Lưu Thu Thập Sao".
- `android/app/src/main/java/com/may/phieuluuthuthapsao/UpdatePlugin.java`
  — plugin Capacitor lo phần tải APK và mở trình cài đặt.
- `.github/workflows/build-apk.yml` — build APK trên GitHub Actions
  (JDK 17, Android SDK 34, Gradle 8.2.1) và tạo Release khi có tag `v*`.
- `scripts/decode-assets.sh` — giải mã ảnh game trước khi build.

## Vì sao ảnh lưu dạng `.b64`?

Repo này được đẩy lên GitHub bằng connector của Muse, connector chỉ
nhận file text (UTF-8) nên ảnh game (`.webp`) được lưu dưới dạng base64
text (`.webp.b64`). Trước khi build, chạy `bash scripts/decode-assets.sh`
để giải mã thành file `.webp` trong `www/assets/` (CI chạy tự động).
File `.webp` đã giải mã không đưa vào git.

## Build trên máy của mình

```bash
bash scripts/decode-assets.sh
npm ci
npx cap sync android
cd android
gradle :app:assembleDebug
# APK ở android/app/build/outputs/apk/debug/app-debug.apk
```

Cần JDK 17 và Android SDK 34. (Lưu ý: Gradle daemon từng lỗi trong
máy ảo Linux của Muse nên trước đây phải build thủ công bằng aapt2;
trên GitHub Actions thì Gradle chạy bình thường.)
