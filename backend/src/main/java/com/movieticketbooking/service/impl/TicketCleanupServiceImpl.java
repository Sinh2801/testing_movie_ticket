package com.movieticketbooking.service.impl;

import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.ShowTimeSeat;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.model.TicketStatus;
import com.movieticketbooking.repository.ShowTimeSeatRepository;
import com.movieticketbooking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service chạy ẩn (Background Worker) định kỳ dọn dẹp các đơn vé quá hạn 15 phút chưa thanh toán.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketCleanupServiceImpl {

    private final TicketRepository ticketRepository;
    private final ShowTimeSeatRepository showTimeSeatRepository;

    /**
     * Tự động chạy mỗi 60,000ms (1 phút) để quét CSDL.
     * Nhả các ghế thuộc các vé ở trạng thái PENDING đã quá 15 phút.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredTickets() {
        // Mốc thời gian 15 phút trước
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(15);

        // 1. Tìm các vé PENDING được tạo trước thời điểm 15 phút trước
        List<Ticket> expiredTickets = ticketRepository.findByStatusAndCreatedAtBefore(
                TicketStatus.PENDING,
                expirationThreshold
        );

        if (expiredTickets.isEmpty()) {
            return;
        }

        log.info("⏰ [Scheduled Task] Phát hiện {} vé quá hạn 15 phút chưa thanh toán. Đang tự động hủy vé và nhả ghế...", expiredTickets.size());

        for (Ticket ticket : expiredTickets) {
            // 2. Chuyển trạng thái vé sang CANCELLED
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setUpdatedAt(LocalDateTime.now());

            // 3. Nhả các ghế từ RESERVED về AVAILABLE
            if (ticket.getTicketSeats() != null) {
                ticket.getTicketSeats().forEach(ticketSeat -> {
                    ShowTimeSeat showTimeSeat = ticketSeat.getShowTimeSeat();
                    if (showTimeSeat != null && showTimeSeat.getStatus().equals(SeatStatus.RESERVED)) {
                        showTimeSeat.setStatus(SeatStatus.AVAILABLE);
                        showTimeSeat.setUpdatedAt(LocalDateTime.now());
                        showTimeSeatRepository.save(showTimeSeat);
                    }
                });
            }

            ticketRepository.save(ticket);
        }

        log.info("✅ [Scheduled Task] Hoàn tất tự động hủy {} vé và trả lại trạng thái ghế AVAILABLE.", expiredTickets.size());
    }
}
