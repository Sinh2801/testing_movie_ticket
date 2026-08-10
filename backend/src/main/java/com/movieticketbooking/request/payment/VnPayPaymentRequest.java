package com.movieticketbooking.request.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Đối tượng chứa dữ liệu yêu cầu tạo thanh toán VNPay từ Frontend gửi lên
 */
@Getter
@Setter
@NoArgsConstructor
public class VnPayPaymentRequest {
    // Mã đơn vé cần thanh toán (bắt buộc phải có)
    @NotNull(message = "Ticket ID is required")
    private Integer ticketId;
    
    // Mã ngân hàng tùy chọn (Ví dụ: NCB, VNPAYQR, COMBANK). Nếu để trống VNPay sẽ cho người dùng tự chọn trên giao diện
    private String bankCode;
}
