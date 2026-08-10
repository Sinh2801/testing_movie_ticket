package com.movieticketbooking.service.impl;

import com.movieticketbooking.config.VnPayConfig;
import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Payment;
import com.movieticketbooking.model.PaymentMethod;
import com.movieticketbooking.model.PaymentStatus;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.repository.PaymentRepository;
import com.movieticketbooking.repository.TicketRepository;
import com.movieticketbooking.request.payment.MoMoPaymentRequest;
import com.movieticketbooking.request.payment.VnPayPaymentRequest;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.service.PaymentService;
import com.movieticketbooking.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    // Repository thao tác cơ sở dữ liệu với bảng payments
    private final PaymentRepository paymentRepository;
    // Repository thao tác cơ sở dữ liệu với bảng tickets
    private final TicketRepository ticketRepository;
    // Service hỗ trợ xác nhận đơn vé và gửi email
    private final TicketService ticketService;

    // Lấy thông tin cấu hình VNPay từ file application.properties
    @Value("${vnpay.tmn-code:CGXZ8008}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:SECRETKEYGOESHERE}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url:http://localhost:8081/api/payments/vnpay/callback}")
    private String vnpReturnUrl;

    @Override
    @Transactional
    public PaymentResponse createMoMoPaymentUrl(MoMoPaymentRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + request.getTicketId()));

        String transactionId = "MOMO-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Payment payment = paymentRepository.findByTicketId(ticket.getId())
                .orElse(new Payment());

        payment.setTicket(ticket);
        payment.setAmount(ticket.getTotalPrice());
        payment.setMethod(PaymentMethod.MOMO);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(transactionId);
        payment.setUid("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        String redirectUrl = "/payment-success.html?ticketId=" + ticket.getId() + "&transactionId=" + transactionId;

        return PaymentResponse.builder()
                .paymentUrl(redirectUrl)
                .transactionId(transactionId)
                .ticketId(ticket.getId())
                .message("Generated MoMo payment URL successfully")
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse processMoMoCallback(String transactionId, String responseCode, int ticketId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseGet(() -> paymentRepository.findByTicketId(ticketId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment not found")));

        payment.setResponseCode(responseCode);
        payment.setUpdatedAt(LocalDateTime.now());

        if ("0".equals(responseCode) || "00".equals(responseCode) || "SUCCESS".equalsIgnoreCase(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            ticketService.confirmTicketPayment(ticketId);

            return PaymentResponse.builder()
                    .paymentUrl("/profile.html")
                    .transactionId(transactionId)
                    .ticketId(ticketId)
                    .message("MoMo Payment successfully completed")
                    .build();
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentUrl("/booking.html")
                    .transactionId(transactionId)
                    .ticketId(ticketId)
                    .message("MoMo Payment failed")
                    .build();
        }
    }

    /**
     * BƯỚC 1: KHỞI TẠO URL THANH TOÁN VNPAY
     * Hàm này nhận ticketId, tạo record Payment trạng thái PENDING vào database,
     * đồng thời ghép các tham số cần thiết và ký chữ ký điện tử HMAC-SHA512 để trả
     * về link VNPay.
     */
    @Override
    @Transactional
    public PaymentResponse createVnPayPaymentUrl(VnPayPaymentRequest request, HttpServletRequest httpRequest) {
        // 1. Kiểm tra mã vé (ticketId) xem vé có tồn tại trong database hay không
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + request.getTicketId()));

        // 2. Tạo mã giao dịch duy nhất cho VNPay (Ví dụ: VNP1723281234567)
        String vnp_TxnRef = "VNP" + System.currentTimeMillis();

        // 3. Đổi số tiền cần thanh toán sang đơn vị Đồng (VNPay yêu cầu nhân 100, ví dụ
        // 100,000 VND -> 10000000)
        long amount = ticket.getTotalPrice().longValue() * 100;

        // 4. Tìm đơn thanh toán cũ của vé này nếu có, hoặc khởi tạo đối tượng Payment
        // mới
        Payment payment = paymentRepository.findByTicketId(ticket.getId())
                .orElse(new Payment());

        // 5. Cập nhật thông tin giao dịch thanh toán ban đầu vào Database với trạng
        // thái CHỜ THANH TOÁN (PENDING)
        payment.setTicket(ticket);
        payment.setAmount(ticket.getTotalPrice());
        payment.setMethod(PaymentMethod.VNPAY);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(vnp_TxnRef);
        payment.setUid("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment); // Lưu thông tin thanh toán vào DB

        // 6. Đóng gói danh sách tham số quy định bởi VNPay vào Map
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0"); // Phiên bản API VNPay
        vnp_Params.put("vnp_Command", "pay"); // Lệnh thanh toán
        vnp_Params.put("vnp_TmnCode", vnpTmnCode); // Mã Website Merchant
        vnp_Params.put("vnp_Amount", String.valueOf(amount)); // Số tiền x 100
        vnp_Params.put("vnp_CurrCode", "VND"); // Đơn vị tiền tệ

        // Nếu người dùng chọn sẵn ngân hàng (VD: NCB, VNPAYQR)
        if (request.getBankCode() != null && !request.getBankCode().trim().isEmpty()) {
            vnp_Params.put("vnp_BankCode", request.getBankCode());
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef); // Mã tham chiếu giao dịch
        vnp_Params.put("vnp_OrderInfo", "Thanh toan ve xem phim #" + ticket.getId() + " - Ref: " + vnp_TxnRef); // Nội
                                                                                                                // dung
                                                                                                                // thanh
                                                                                                                // toán
        vnp_Params.put("vnp_OrderType", "other"); // Loại danh mục hàng hóa
        vnp_Params.put("vnp_Locale", "vn"); // Ngôn ngữ giao diện (vn/en)
        vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl); // URL nhận kết quả trả về
        vnp_Params.put("vnp_IpAddr", VnPayConfig.getIpAddress(httpRequest)); // IP thiết bị người dùng

        // 7. Thiết lập thời gian tạo đơn và thời gian hết hạn giao dịch (15 phút)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Cộng thêm 15 phút hết hạn
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // 8. Sắp xếp thứ tự các tham số theo bảng chữ cái A-Z (Bắt buộc theo chuẩn
        // VNPay)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        // 9. Xây dựng chuỗi dữ liệu băm (hashData) và chuỗi URL truy vấn (query)
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Thêm vào chuỗi băm để mã hóa chữ ký
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                // Thêm vào chuỗi URL query parameter
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 10. Tạo chữ ký mã hóa HMAC-SHA512 bằng vnpHashSecret
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnpHashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash; // Gắn chữ ký bảo mật vào cuối URL

        // 11. Đường dẫn URL thanh toán hoàn chỉnh của VNPay
        String paymentUrl = vnpPayUrl + "?" + queryUrl;

        // Trả về cho Frontend URL để thực hiện chuyển hướng trình duyệt
        return PaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .transactionId(vnp_TxnRef)
                .ticketId(ticket.getId())
                .message("Generated VNPay payment URL successfully")
                .build();
    }

    /**
     * BƯỚC 2: XỬ LÝ KẾT QUẢ VNPAY TRẢ VỀ (CALLBACK / RETURN URL)
     * Sau khi khách hàng thực hiện thanh toán trên cổng VNPay, VNPay sẽ gọi lại hàm
     * này.
     * Hàm sẽ kiểm tra chữ ký checksum để đảm bảo an toàn, sau đó cập nhật dữ liệu
     * vào Database.
     */
    @Override
    @Transactional
    public PaymentResponse processVnPayCallback(Map<String, String> queryParams) {
        // 1. Lấy chữ ký bảo mật vnp_SecureHash mà VNPay trả về trên URL
        String vnp_SecureHash = queryParams.get("vnp_SecureHash");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef"); // Mã giao dịch của mình
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode"); // Mã trạng thái thanh toán (00 = Thành công)

        // 2. Lọc tất cả các tham số bắt đầu bằng "vnp_" ngoại trừ chuỗi chữ ký
        // vnp_SecureHash
        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getKey().startsWith("vnp_") && !"vnp_SecureHash".equals(entry.getKey())
                    && !"vnp_SecureHashType".equals(entry.getKey())) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        // 3. Sắp xếp danh sách tên tham số theo thứ tự A-Z
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        // 4. Tự tính toán lại chữ ký checksum với vnpHashSecret của hệ thống
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // 5. So sánh chữ ký vừa tính lại (signValue) với chữ ký do VNPay gửi về
        // (vnp_SecureHash)
        String signValue = VnPayConfig.hmacSHA512(vnpHashSecret, hashData.toString());

        // 6. Truy vấn đơn thanh toán tương ứng trong cơ sở dữ liệu dựa vào mã giao dịch
        // vnp_TxnRef
        Payment payment = paymentRepository.findByTransactionId(vnp_TxnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with TxnRef: " + vnp_TxnRef));

        // Cập nhật mã phản hồi trả về từ ngân hàng
        payment.setResponseCode(vnp_ResponseCode);
        payment.setUpdatedAt(LocalDateTime.now());

        // 7. Kiểm tra tính hợp lệ của chữ ký và mã phản hồi (00 có nghĩa là giao dịch
        // thành công)
        boolean isValidChecksum = signValue.equalsIgnoreCase(vnp_SecureHash);
        boolean isSuccess = isValidChecksum && "00".equals(vnp_ResponseCode);

        int ticketId = payment.getTicket() != null ? payment.getTicket().getId() : 0;

        // BƯỚC 8: CẬP NHẬT TRẠNG THÁI VÀO DATABASE
        if (isSuccess) {
            // 8.1. Cập nhật bảng PAYMENT: Chuyển trạng thái thanh toán sang SUCCESS và lưu
            // ngày giờ thanh toán
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // 8.2. Cập nhật bảng TICKET & SHOWTIME_SEATS: Chuyển trạng thái vé sang
            // CONFIRMED và giữ ghế BOOKED + Gửi Email vé
            if (ticketId > 0) {
                ticketService.confirmTicketPayment(ticketId);
            }

            return PaymentResponse.builder()
                    .paymentUrl("/profile.html?payment=success&ticketId=" + ticketId)
                    .transactionId(vnp_TxnRef)
                    .ticketId(ticketId)
                    .message("VNPay Payment successfully completed")
                    .build();
        } else {
            // 8.3. Nếu thất bại hoặc sai chữ ký checksum: Cập nhật bảng PAYMENT thành
            // FAILED
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentUrl("/booking.html?payment=failed&ticketId=" + ticketId)
                    .transactionId(vnp_TxnRef)
                    .ticketId(ticketId)
                    .message("VNPay Payment failed or checksum mismatch")
                    .build();
        }
    }
}
