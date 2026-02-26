# Banking Notification

Một ứng dụng Flutter cho Android đọc thông báo giao dịch ngân hàng thành tiếng sử dụng Text-to-Speech (TTS).

## Tính năng

- Lắng nghe thông báo từ các ứng dụng ngân hàng Việt Nam (ACB, Vietcombank, Momo)
- Tự động đọc số tiền giao dịch bằng tiếng Việt
- Cấu hình ngưỡng số tiền tối thiểu
- Bật/tắt thông báo giọng nói

## Ngân hàng | Ví điện tử được hỗ trợ

- ACB (`mobile.acb.com.vn`)
- Vietcombank (`com.VCB`)
- Momo (`com.mservice.momotransfer`)
- MB Bank (`com.mbmobile`)
- Agribank (`com.vnpay.Agribank3g`)

## Cài đặt

1. Cài đặt ứng dụng trên thiết bị Android
2. Cấp quyền **Truy cập Thông báo**:
   - Vào **Cài đặt > Ứng dụng > Truy cập ứng dụng đặc biệt > Truy cập thông báo**
   - Bật **banking_notification**
3. Đảm bảo giọng đọc tiếng Việt (TTS) đã được cài đặt:
   - Vào **Cài đặt > Ngôn ngữ & nhập liệu > Chuyển văn bản thành giọng nói**
   - Tải về dữ liệu giọng nói tiếng Việt nếu chưa có

## Sử dụng

1. Mở ứng dụng
2. Cấp quyền truy cập thông báo khi được nhắc
3. Sử dụng nút **Test Voice** để kiểm tra TTS có hoạt động hay không
4. Cấu hình cài đặt:
   - **Voice Enabled**: Bật/tắt giọng nói
   - **Minimum Amount**: Đặt số tiền giao dịch tối thiểu để thông báo

## Yêu cầu

- Android 6.0+
- Đã cài đặt giọng đọc tiếng Việt (khuyên dùng Google TTS)
