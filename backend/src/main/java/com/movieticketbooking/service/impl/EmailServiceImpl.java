package com.movieticketbooking.service.impl;

import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.model.TicketSeat;
import com.movieticketbooking.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@movieticket.com}")
    private String fromEmail;

    @Override
    public void sendTicketConfirmationEmail(Ticket ticket) {
        String userEmail = (ticket.getUser() != null) ? ticket.getUser().getEmail() : "user@example.com";
        String userName = (ticket.getUser() != null) ? ticket.getUser().getUsername() : "Khách hàng";

        String movieTitle = (ticket.getShowTime() != null && ticket.getShowTime().getMovie() != null) 
                ? ticket.getShowTime().getMovie().getName() : "Phim";
        String cinemaName = (ticket.getShowTime() != null && ticket.getShowTime().getCinema() != null) 
                ? ticket.getShowTime().getCinema().getName() : "Rạp chiếu";
        String roomName = (ticket.getShowTime() != null && ticket.getShowTime().getRoom() != null) 
                ? ticket.getShowTime().getRoom().getName() : "Phòng chiếu";

        String startTimeStr = (ticket.getShowTime() != null && ticket.getShowTime().getStartTime() != null)
                ? ticket.getShowTime().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")) : "";

        String seats = (ticket.getTicketSeats() != null)
                ? ticket.getTicketSeats().stream()
                .map(ts -> ts.getShowTimeSeat().getSeat().getSeatName())
                .collect(Collectors.joining(", ")) : "";

        String htmlContent = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #141414; color: #ffffff; padding: 24px; border-radius: 12px; border: 1px solid #333;">
                <h2 style="color: #e50914; text-align: center; font-size: 24px; margin-bottom: 20px;">XÁC NHẬN ĐẶT VÉ THÀNH CÔNG</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Cảm ơn bạn đã lựa chọn dịch vụ đặt vé của chúng tôi. Dưới đây là thông tin vé phim của bạn:</p>

                <div style="background-color: #1f1f1f; padding: 16px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #e50914;">
                    <p style="margin: 8px 0;"><strong>Mã Vé (UID):</strong> <span style="color: #f5c518; font-size: 18px; font-weight: bold;">%s</span></p>
                    <p style="margin: 8px 0;"><strong>Tên Phim:</strong> <span style="color: #ffffff;">%s</span></p>
                    <p style="margin: 8px 0;"><strong>Rạp Chiếu:</strong> %s</p>
                    <p style="margin: 8px 0;"><strong>Phòng Chiếu:</strong> %s</p>
                    <p style="margin: 8px 0;"><strong>Thời Gian Chiếu:</strong> %s</p>
                    <p style="margin: 8px 0;"><strong>Ghế Đã Chọn:</strong> <span style="color: #46d369; font-weight: bold;">%s</span></p>
                    <p style="margin: 8px 0;"><strong>Tổng Tiền:</strong> <span style="color: #e50914; font-size: 18px; font-weight: bold;">%,.0f VNĐ</span></p>
                </div>

                <p style="font-size: 13px; color: #888888; text-align: center;">Vui lòng xuất trình mã vé UID tại quầy rạp để đổi vé cứng. Chúc bạn xem phim vui vẻ!</p>
            </div>
            """, userName, ticket.getUid(), movieTitle, cinemaName, roomName, startTimeStr, seats, ticket.getTotalPrice());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(userEmail);
            helper.setSubject("[MovieTicket] Xác nhận đặt vé thành công - Mã vé: " + ticket.getUid());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully sent booking confirmation email to {}", userEmail);
        } catch (Exception e) {
            log.warn("Could not send email via SMTP (Fallback Mode Active): {}. Printing email content to log:\nRecipient: {}\nContent:\n{}", 
                    e.getMessage(), userEmail, htmlContent);
        }
    }

    @Override
    public void sendTicketCancellationEmail(Ticket ticket) {
        String userEmail = (ticket.getUser() != null) ? ticket.getUser().getEmail() : "user@example.com";
        String userName = (ticket.getUser() != null) ? ticket.getUser().getUsername() : "Khách hàng";

        String movieTitle = (ticket.getShowTime() != null && ticket.getShowTime().getMovie() != null) 
                ? ticket.getShowTime().getMovie().getName() : "Phim";
        String cinemaName = (ticket.getShowTime() != null && ticket.getShowTime().getCinema() != null) 
                ? ticket.getShowTime().getCinema().getName() : "Rạp chiếu";
        String roomName = (ticket.getShowTime() != null && ticket.getShowTime().getRoom() != null) 
                ? ticket.getShowTime().getRoom().getName() : "Phòng chiếu";

        String startTimeStr = (ticket.getShowTime() != null && ticket.getShowTime().getStartTime() != null)
                ? ticket.getShowTime().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")) : "";

        String seats = (ticket.getTicketSeats() != null)
                ? ticket.getTicketSeats().stream()
                .map(ts -> ts.getShowTimeSeat().getSeat().getSeatName())
                .collect(Collectors.joining(", ")) : "";

        String htmlContent = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #141414; color: #ffffff; padding: 24px; border-radius: 12px; border: 1px solid #333;">
                <h2 style="color: #e50914; text-align: center; font-size: 24px; margin-bottom: 20px;">THÔNG BÁO HỦY VÉ THÀNH CÔNG</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Yêu cầu hủy vé xem phim của bạn đã được xử lý thành công. Dưới đây là thông tin vé đã hủy:</p>

                <div style="background-color: #1f1f1f; padding: 16px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #e50914;">
                    <p style="margin: 8px 0;"><strong>Mã Vé (UID):</strong> <span style="color: #f5c518; font-size: 18px; font-weight: bold;">%s</span></p>
                    <p style="margin: 8px 0;"><strong>Tên Phim:</strong> <span style="color: #ffffff;">%s</span></p>
                    <p style="margin: 8px 0;"><strong>Rạp & Phòng Chiếu:</strong> %s - %s</p>
                    <p style="margin: 8px 0;"><strong>Thời Gian Chiếu:</strong> %s</p>
                    <p style="margin: 8px 0;"><strong>Ghế Đã Hủy Giải Phóng:</strong> <span style="color: #e50914; font-weight: bold;">%s</span></p>
                    <p style="margin: 8px 0;"><strong>Giá Trị Vé:</strong> <span style="color: #ffffff; font-size: 16px; font-weight: bold;">%,.0f VNĐ</span></p>
                </div>

                <p style="font-size: 13px; color: #888888; text-align: center;">Ghế của vé đã được giải phóng để phục vụ quý khách hàng khác. Cảm ơn bạn đã sử dụng dịch vụ!</p>
            </div>
            """, userName, ticket.getUid(), movieTitle, cinemaName, roomName, startTimeStr, seats, ticket.getTotalPrice());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(userEmail);
            helper.setSubject("[MovieTicket] Thông báo hủy vé thành công - Mã vé: " + ticket.getUid());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully sent ticket cancellation email to {}", userEmail);
        } catch (Exception e) {
            log.warn("Could not send cancellation email via SMTP (Fallback Mode Active): {}. Printing email content to log:\nRecipient: {}\nContent:\n{}", 
                    e.getMessage(), userEmail, htmlContent);
        }
    }
}
