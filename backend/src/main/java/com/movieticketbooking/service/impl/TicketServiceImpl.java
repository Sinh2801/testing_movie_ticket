package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceAlreadyExistException;
import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.*;
import com.movieticketbooking.repository.*;
import com.movieticketbooking.request.ticket.CreateTicketRequest;
import com.movieticketbooking.response.TicketResponse;
import com.movieticketbooking.service.EmailService;
import com.movieticketbooking.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ShowTimeRepository showTimeRepository;
    private final ShowTimeSeatRepository showTimeSeatRepository;
    private final TicketSeatRepository ticketSeatRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public TicketResponse createTicket(User user, CreateTicketRequest request) {
        ShowTime showTime = showTimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("ShowTime not found with id: " + request.getShowtimeId()));

        List<ShowTimeSeat> selectedSeats = new ArrayList<>();
        for (Integer stsId : request.getShowtimeSeatIds()) {
            ShowTimeSeat sts = showTimeSeatRepository.findById(stsId)
                    .orElseThrow(() -> new ResourceNotFoundException("ShowTimeSeat not found with id: " + stsId));

            if (sts.getStatus() == SeatStatus.BOOKED) {
                throw new ResourceAlreadyExistException("Seat " + sts.getSeat().getSeatName() + " has already been booked!");
            }
            selectedSeats.add(sts);
        }

        BigDecimal unitPrice = showTime.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(selectedSeats.size()));

        String ticketUid = "TIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Ticket ticket = new Ticket();
        ticket.setUid(ticketUid);
        ticket.setUser(user);
        ticket.setShowTime(showTime);
        ticket.setTotalPrice(totalPrice);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        List<TicketSeat> ticketSeats = new ArrayList<>();
        for (ShowTimeSeat sts : selectedSeats) {
            TicketSeat ts = new TicketSeat();
            ts.setTicket(savedTicket);
            ts.setShowTimeSeat(sts);
            ts.setCreatedAt(LocalDateTime.now());
            ticketSeats.add(ts);
        }
        ticketSeatRepository.saveAll(ticketSeats);
        savedTicket.setTicketSeats(ticketSeats);

        return mapToResponse(savedTicket);
    }

    @Override
    public TicketResponse getTicketById(int id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        return mapToResponse(ticket);
    }

    @Override
    public TicketResponse getTicketByUid(String uid) {
        Ticket ticket = ticketRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with uid: " + uid));
        return mapToResponse(ticket);
    }

    @Override
    public List<TicketResponse> getUserTickets(User user) {
        List<Ticket> tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void confirmTicketPayment(int ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        // Update showtime seats status to BOOKED
        if (ticket.getTicketSeats() != null) {
            for (TicketSeat ts : ticket.getTicketSeats()) {
                ShowTimeSeat sts = ts.getShowTimeSeat();
                if (sts != null) {
                    sts.setStatus(SeatStatus.BOOKED);
                    sts.setUpdatedAt(LocalDateTime.now());
                    showTimeSeatRepository.save(sts);
                }
            }
        }

        // Send email notification
        emailService.sendTicketConfirmationEmail(ticket);
    }

    @Override
    @Transactional
    public TicketResponse cancelTicket(int ticketId, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (ticket.getUser() != null && ticket.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You are not authorized to cancel this ticket!");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Ticket has already been cancelled!");
        }

        ShowTime st = ticket.getShowTime();
        if (st == null || st.getStartTime() == null) {
            throw new ResourceNotFoundException("ShowTime information not found for ticket!");
        }

        // Rule: Must cancel at least 2 hours before showtime start_time
        LocalDateTime cancellationDeadline = LocalDateTime.now().plusHours(2);
        if (cancellationDeadline.isAfter(st.getStartTime())) {
            throw new IllegalStateException("Chỉ được hủy vé trước giờ chiếu ít nhất 2 tiếng!");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        // Revert showtime seats status back to AVAILABLE
        if (ticket.getTicketSeats() != null) {
            for (TicketSeat ts : ticket.getTicketSeats()) {
                ShowTimeSeat sts = ts.getShowTimeSeat();
                if (sts != null) {
                    sts.setStatus(SeatStatus.AVAILABLE);
                    sts.setUpdatedAt(LocalDateTime.now());
                    showTimeSeatRepository.save(sts);
                }
            }
        }

        // Send email notification
        emailService.sendTicketCancellationEmail(savedTicket);

        return mapToResponse(savedTicket);
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        ShowTime st = ticket.getShowTime();
        List<String> seatNames = (ticket.getTicketSeats() != null) ?
                ticket.getTicketSeats().stream()
                        .map(ts -> ts.getShowTimeSeat().getSeat().getSeatName())
                        .collect(Collectors.toList()) : new ArrayList<>();

        return TicketResponse.builder()
                .id(ticket.getId())
                .uid(ticket.getUid())
                .totalPrice(ticket.getTotalPrice())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .showtimeId(st != null ? st.getId() : 0)
                .movieName(st != null && st.getMovie() != null ? st.getMovie().getName() : "")
                .movieImageUrl(st != null && st.getMovie() != null ? st.getMovie().getImageUrl() : "")
                .cinemaName(st != null && st.getCinema() != null ? st.getCinema().getName() : "")
                .roomName(st != null && st.getRoom() != null ? st.getRoom().getName() : "")
                .startTime(st != null ? st.getStartTime() : null)
                .userName(ticket.getUser() != null ? ticket.getUser().getUsername() : "")
                .userEmail(ticket.getUser() != null ? ticket.getUser().getEmail() : "")
                .seatNames(seatNames)
                .build();
    }
}
