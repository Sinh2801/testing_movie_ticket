import { CONFIG } from '../config/api.js';

// Booking & Payment Service
export const BookingService = {
    async createTicket(showtimeId, showtimeSeatIds) {
        const token = localStorage.getItem('cine_token');
        const response = await fetch(`${CONFIG.API_BASE_URL}/tickets/book`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ showtimeId, showtimeSeatIds })
        });
        return await response.json();
    },

    async createMoMoPayment(ticketId) {
        const token = localStorage.getItem('cine_token');
        const response = await fetch(`${CONFIG.API_BASE_URL}/payments/momo/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ ticketId })
        });
        return await response.json();
    },

    async processMoMoCallback(transactionId, responseCode, ticketId) {
        const response = await fetch(`${CONFIG.API_BASE_URL}/payments/momo/callback?transactionId=${transactionId}&responseCode=${responseCode}&ticketId=${ticketId}`, {
            method: 'POST'
        });
        return await response.json();
    },

    async getUserTicketHistory() {
        const token = localStorage.getItem('cine_token');
        const response = await fetch(`${CONFIG.API_BASE_URL}/tickets/history`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return await response.json();
    },

    async cancelTicket(ticketId) {
        const token = localStorage.getItem('cine_token');
        const response = await fetch(`${CONFIG.API_BASE_URL}/tickets/${ticketId}/cancel`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });
        return await response.json();
    }
};
