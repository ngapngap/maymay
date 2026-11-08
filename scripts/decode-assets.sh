#!/bin/bash
# Giải mã ảnh game từ dạng base64 text (www/assets/*.webp.b64) thành
# file ảnh .webp để build app.
#
# Vì sao ảnh lưu dạng .b64: GitHub connector của Muse chỉ push được
# file text (UTF-8), nên ảnh game được lưu dưới dạng base64 text trong
# repo. Chạy script này (CI chạy tự động) trước `npx cap sync android`.
# Script chỉ giải mã khi file .webp chưa có; sau khi giải mã xong nó xóa
# các file .b64 khỏi thư mục làm việc để chúng không bị đóng gói vào APK
# (file .b64 vẫn còn nguyên trong git, muốn giải mã lại thì chạy
# `git checkout -- www/assets` rồi chạy lại script).
set -e
cd "$(dirname "$0")/.."

found=0
for f in www/assets/*.webp.b64; do
    [ -e "$f" ] || continue
    found=1
    out="${f%.b64}"
    if [ ! -f "$out" ]; then
        base64 -d "$f" > "$out"
        echo "decoded $f -> $out"
    fi
done

if [ "$found" -eq 0 ]; then
    echo "no .b64 files (assets already decoded)"
    exit 0
fi

# Xóa bản .b64 khỏi thư mục làm việc để khỏi đóng gói trùng vào APK.
rm -f www/assets/*.webp.b64
echo "assets ready"
