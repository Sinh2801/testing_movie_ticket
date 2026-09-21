package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Payment;
import com.movieticketbooking.model.PaymentMethod;
import com.movieticketbooking.model.PaymentStatus;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.repository.PaymentRepository;
import com.movieticketbooking.request.payment.StripePaymentRequest;
import com.movieticketbooking.response.PaymentResponse;
import com.movieticketbooking.response.StripePaymentResponse;
import com.movieticketbooking.service.StripeService;
import com.movieticketbooking.service.TicketService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp triển khai các nghiệp vụ kết nối và xử lý thanh toán qua Stripe SDK.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    // Lấy Secret Key (sk_test_...) từ file application.properties
    @Value("${stripe.secret.key}")
    private String secretKey;

    // Lấy Publishable Key (pk_test_...) từ file application.properties
    @Value("${stripe.publishable.key}")
    private String publishableKey;

    private final TicketService ticketService;
    private final PaymentRepository paymentRepository;

    /**
     * Tự động chạy sau khi Spring Bean được khởi tạo để nạp Secret Key vào Stripe SDK tĩnh.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * BƯỚC 1: Khởi tạo PaymentIntent với Stripe API
     * Khi khách hàng bấm vào tab "Thẻ Quốc Tế Stripe", Frontend sẽ gọi tới method này.
     */
    @Override
    @Transactional
    public StripePaymentResponse createPaymentIntent(StripePaymentRequest request) {
        // Gán lại apiKey để đảm bảo SDK luôn sử dụng secret key mới nhất
        Stripe.apiKey = secretKey;

        // 1. Tìm thông tin vé từ CSDL dựa theo ticketId gửi lên
        Ticket ticket = ticketService.findTicketById(request.getTicketId());

        // 2. Lấy tổng số tiền cần thanh toán của vé
        BigDecimal totalPrice = ticket.getTotalPrice();
        long amountInVnd = totalPrice != null ? totalPrice.longValue() : 0L;

        // 3. Chuẩn bị mã UID vé để lưu vết giao dịch
        String ticketUid = ticket.getUid() != null ? ticket.getUid() : ("TIC-" + ticket.getId());

        // 4. Kiểm tra an toàn: Nếu người dùng chưa cấu hình Secret Key thật thì ném lỗi thông báo rõ ràng
        if (secretKey == null || secretKey.contains("YOUR_STRIPE_SECRET_KEY") || secretKey.isBlank()) {
            throw new IllegalArgumentException("Khóa API Stripe (stripe.secret.key) trong application.properties chưa được thay bằng Secret Key thật từ Stripe Dashboard!");
        }

        try {
            // 5. Tạo Idempotency Key duy nhất cho vé này để tránh tạo trùng PaymentIntent trên Stripe
            String idempotencyKey = "stripe_intent_ticket_" + ticket.getId();
            com.stripe.net.RequestOptions requestOptions = com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(idempotencyKey)
                    .build();

            // 6. Tạo tham số yêu cầu (CreateParams) gửi tới Stripe Server
            // Lưu ý: Stripe tính tiền bằng đơn vị nhỏ nhất (VND không dùng số thập phân)
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInVnd) // Số tiền thanh toán (Ví dụ: 90000 = 90,000 VNĐ)
                    .setCurrency("vnd")    // Đơn vị tiền tệ Việt Nam Đồng
                    .putMetadata("ticketId", String.valueOf(ticket.getId())) // Gắn metadata mã vé để tra cứu sau
                    .putMetadata("ticketUid", ticketUid)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            // 7. Gọi SDK Stripe với RequestOptions (chứa IdempotencyKey) để khởi tạo phiên giao dịch an toàn
            PaymentIntent paymentIntent = PaymentIntent.create(params, requestOptions);

            // 8. Lưu hoặc cập nhật bản ghi Payment trong CSDL local với trạng thái PENDING (Chờ thanh toán)
            Payment payment = paymentRepository.findByTicketId(ticket.getId())
                    .orElse(new Payment());

            payment.setTicket(ticket);
            payment.setAmount(totalPrice);
            payment.setMethod(PaymentMethod.STRIPE);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(paymentIntent.getId()); // Mã giao dịch Stripe (ví dụ: pi_3M...)
            payment.setPaymentCode(ticketUid);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // 9. Trả về clientSecret và publishableKey cho Frontend để hiển thị thẻ nhập Visa/Mastercard
            return StripePaymentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .publishableKey(publishableKey)
                    .amount(amountInVnd)
                    .currency("vnd")
                    .ticketId(ticket.getId())
                    .paymentIntentId(paymentIntent.getId())
                    .build();

        } catch (com.stripe.exception.AuthenticationException e) {
            log.error("Stripe Authentication Error: Key '{}' không hợp lệ", secretKey);
            throw new IllegalArgumentException("Khóa API Stripe (stripe.secret.key) không hợp lệ hoặc đã bị revoked! Vui lòng dán khóa 'sk_test_...' đúng từ https://dashboard.stripe.com/apikeys vào application.properties");
        } catch (StripeException e) {
            log.error("Stripe payment intent creation error: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khởi tạo cổng thanh toán Stripe: " + e.getMessage());
        }
    }

    /**
     * BƯỚC 2: Xác nhận thanh toán thành công
     * Được gọi sau khi Frontend thông báo thẻ đã quẹt thành công qua stripe.confirmCardPayment()
     */
    @Override
    @Transactional
    public PaymentResponse confirmStripePayment(int ticketId, String paymentIntentId) {
        // 1. Tìm bản ghi Payment tương ứng với vé
        Payment payment = paymentRepository.findByTicketId(ticketId)
                .orElseGet(() -> {
                    Ticket ticket = ticketService.findTicketById(ticketId);
                    Payment newP = new Payment();
                    newP.setTicket(ticket);
                    newP.setAmount(ticket.getTotalPrice());
                    newP.setMethod(PaymentMethod.STRIPE);
                    newP.setPaymentCode(ticket.getUid());
                    return newP;
                });

        // ⚡ 2. IDEMPOTENCY GUARD: Nếu thanh toán đã SUCCESS trước đó -> Bỏ qua không xử lý trùng lặp
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("⚠️ [Idempotency Guard] Vé {} đã được xác nhận thanh toán trước đó. Trả về kết quả an toàn!", ticketId);
            return PaymentResponse.builder()
                    .paymentUrl("/profile.html?payment=success&ticketId=" + ticketId)
                    .transactionId(payment.getTransactionId())
                    .ticketId(ticketId)
                    .message("Stripe Payment already confirmed (Idempotent response)")
                    .build();
        }

        // 3. Đổi trạng thái Payment sang SUCCESS và cập nhật thời gian thanh toán
        payment.setStatus(PaymentStatus.SUCCESS);
        if (paymentIntentId != null && !paymentIntentId.isBlank()) {
            payment.setTransactionId(paymentIntentId);
        }
        payment.setPaidAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 4. Gọi TicketService để chuyển vé sang CONFIRMED và chuyển trạng thái các ghế sang BOOKED
        ticketService.confirmTicketPayment(ticketId);

        // 5. Trả về kết quả thành công cho Frontend
        return PaymentResponse.builder()
                .paymentUrl("/profile.html?payment=success&ticketId=" + ticketId)
                .transactionId(payment.getTransactionId())
                .ticketId(ticketId)
                .message("Stripe Payment successfully confirmed")
                .build();
    }
}
