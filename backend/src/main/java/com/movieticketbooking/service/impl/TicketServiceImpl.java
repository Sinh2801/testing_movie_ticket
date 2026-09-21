package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceAlreadyExistException;
import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.*;
import com.movieticketbooking.repository.*;
import com.movieticketbooking.request.ticket.CreateTicketRequest;
import com.movieticketbooking.response.PagedResponse;
import com.movieticketbooking.response.TicketResponse;
import com.movieticketbooking.service.EmailService;
import com.movieticketbooking.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ShowTimeRepository showTimeRepository;
    private final ShowTimeSeatRepository showTimeSeatRepository;
    private final TicketSeatRepository ticketSeatRepository;
    private final EmailService emailService;

    private final int CANCEL_TICKET_HOURS_LIMIT = 2;

    // Create new ticket booking
    // flow:
    // 1. Get showtime by id and check if it exists and not expired
    // 2. Get showtime seats by ids and check if they are available and not already booked
    // 3. Create a new ticket with status PENDING
    // 4. Create ticket seats and update showtime seats status to BOOKED
    // 5. Return the ticket response
    @Override
    @Transactional
    public TicketResponse createTicket(User user, CreateTicketRequest request) 
    {
        ShowTime showTime = showTimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("ShowTime not found with id: " + request.getShowtimeId()));

        if (showTime.getStartTime() == null || showTime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể đặt vé cho suất chiếu đã qua hoặc không hợp lệ!");
        }

        // check if the selected seats are available and not already booked
        List<ShowTimeSeat> selectedSeats = new ArrayList<>();
        ShowTimeSeat sts = null;
        boolean hasActiveBooking = false;
        for (Integer stsId : request.getShowtimeSeatIds()) {
            sts = showTimeSeatRepository.findByIdWithLock(stsId)
                    .orElseThrow(() -> new ResourceNotFoundException("ShowTimeSeat not found with id: " + stsId));

            if (sts.getStatus() != SeatStatus.AVAILABLE) {
                throw new ResourceAlreadyExistException("Seat " + (sts.getSeat() != null ? sts.getSeat().getSeatName() : stsId) + " is not available!");
            }

            if (sts.getTicketSeats() != null) {
                hasActiveBooking = sts.getTicketSeats().stream()
                        .anyMatch(ts -> ts.getCancelledAt() == null);
                if (hasActiveBooking) {
                    throw new ResourceAlreadyExistException("Seat " + (sts.getSeat() != null ? sts.getSeat().getSeatName() : stsId) + " has already been booked!");
                }
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
        // save ticket seats and set showtime seats status to RESERVED (tạm giữ trong 15 phút)
        List<TicketSeat> ticketSeats = new ArrayList<>();
        for (ShowTimeSeat s : selectedSeats) {
            s.setStatus(SeatStatus.RESERVED);

            TicketSeat ts = new TicketSeat();
            ts.setTicket(savedTicket);
            ts.setShowTimeSeat(s);
            ts.setCreatedAt(LocalDateTime.now());
            ticketSeats.add(ts);
        }
        ticketSeatRepository.saveAll(ticketSeats);
        savedTicket.setTicketSeats(ticketSeats);
        showTimeSeatRepository.saveAll(selectedSeats);

        return mapToResponse(savedTicket);
    }

    // Get ticket by id
    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(int id, User currentUser) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        validateTicketOwnership(ticket, currentUser);
        return mapToResponse(ticket);
    }

    // Get ticket by uid
    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketByUid(String uid, User currentUser) {
        Ticket ticket = ticketRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with uid: " + uid));
        validateTicketOwnership(ticket, currentUser);
        return mapToResponse(ticket);
    }

    private void validateTicketOwnership(Ticket ticket, User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User information required for ticket authorization check!");
        }
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = ticket.getUser() != null && ticket.getUser().getId() == currentUser.getId();
        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("You are not authorized to view this ticket details!");
        }
    }

    // Get all tickets for a user
    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getUserTickets(User user) {
        List<Ticket> tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Confirm ticket payment and update ticket status to CONFIRMED
    @Override
    @Transactional
    public void confirmTicketPayment(int ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        
        if(ticket.getStatus().equals(TicketStatus.PENDING)) {
            ticket.setStatus(TicketStatus.CONFIRMED);
        }else {
            throw new RuntimeException("Ticket status is not PENDING, cannot confirm payment!");
        }
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

    // Cancel ticket and update ticket status to CANCELLED
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

        // Must cancel at least 2 hours before showtime start_time
        LocalDateTime cancellationDeadline = LocalDateTime.now().plusHours(CANCEL_TICKET_HOURS_LIMIT);
        if (cancellationDeadline.isAfter(st.getStartTime())) {
            throw new IllegalStateException(
                "Ticket must be cancelled at least " + CANCEL_TICKET_HOURS_LIMIT + " hours before the showtime!");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);
        List<TicketSeat> ticketSeats = ticket.getTicketSeats();
        // Revert showtime seats status back to AVAILABLE and set cancelledAt timestamp
        if (ticketSeats != null && !ticketSeats.isEmpty()) {
            ShowTimeSeat sts = null;
            for (TicketSeat ts : ticketSeats) {
                ts.setCancelledAt(LocalDateTime.now());

                sts = ts.getShowTimeSeat();
                if (sts != null) {
                    sts.setStatus(SeatStatus.AVAILABLE);
                    sts.setUpdatedAt(LocalDateTime.now());
                    showTimeSeatRepository.save(sts);
                }
            }
            ticketSeatRepository.saveAll(ticketSeats);
        }

        // Send email notification
        //emailService.sendTicketCancellationEmail(savedTicket);

        return mapToResponse(savedTicket);
    }

    // Filter tickets by UID and pagination
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TicketResponse> filter(int page, int size, String uid) {
        int pageIndex = (page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> ticketPage;
        if (uid == null || uid.isBlank()) {
            ticketPage = ticketRepository.findAll(pageable);
        } else {
            ticketPage = ticketRepository.findByUidContaining(uid, pageable);
        }
        Page<TicketResponse> ticketResponsePage = ticketPage.map(this::mapToResponse);
        return new PagedResponse<>(ticketResponsePage);
    }

    // map to response
    private TicketResponse mapToResponse(Ticket ticket) {
        ShowTime st = ticket.getShowTime();
        log.debug("Mapping ticket ID={} to TicketResponse, showTime={}", ticket.getId(), st != null ? st.getId() : "null");
        List<String> seatNames = (ticket.getTicketSeats() != null) ?
                ticket.getTicketSeats().stream()
                        .map(ts -> ts.getShowTimeSeat().getSeat().getSeatName())
                        .collect(Collectors.toList()) : new ArrayList<>();
        TicketResponse resp = new TicketResponse();
        resp.setId(ticket.getId());
        resp.setUid(ticket.getUid());
        resp.setTotalPrice(ticket.getTotalPrice());
        resp.setStatus(ticket.getStatus());
        resp.setCreatedAt(ticket.getCreatedAt());
        resp.setShowtimeId(st != null ? st.getId() : 0);
        resp.setMovieName(st != null && st.getMovie() != null ? st.getMovie().getName() : "");
        resp.setMovieImageUrl(st != null && st.getMovie() != null ? st.getMovie().getImageUrl() : "");
        resp.setCinemaName(st != null && st.getCinema() != null ? st.getCinema().getName() : "");
        resp.setRoomName(st != null && st.getRoom() != null ? st.getRoom().getName() : "");
        resp.setStartTime(st != null ? st.getStartTime() : null);
        resp.setUserName(ticket.getUser() != null ? ticket.getUser().getUsername() : "");
        resp.setUserEmail(ticket.getUser() != null ? ticket.getUser().getEmail() : "");
        resp.setSeatNames(seatNames);
        return resp;
    }

    @Override
    public Ticket findTicketById(int id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    
}
