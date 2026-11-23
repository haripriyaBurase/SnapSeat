
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';

import EventDetails from '../pages/EventDetails';

import { getSeatsByEventId } from '../services/eventService';

import {
    reserveSeat,
    getMyActiveBooking,
    payForBooking
} from '../services/bookingService';

// Mock API services
vi.mock('../services/eventService', () => ({
    getSeatsByEventId: vi.fn()
}));

vi.mock('../services/bookingService', () => ({
    reserveSeat: vi.fn(),
    getMyActiveBooking: vi.fn(),
    payForBooking: vi.fn()
}));

// Mock WebSocket hook
vi.mock('../hooks/useSeatUpdates', () => ({
    default: vi.fn()
}));

// Mock countdown hook
vi.mock('../hooks/useCountdown', () => ({
    default: vi.fn(() => ({
        timeLeft: 299,
        formattedTime: '04:59',
        expired: false
    }))
}));

describe('EventDetails Component', () => {

    const mockSeats = [
        {
            id: 1,
            seatNumber: 'A1',
            status: 'AVAILABLE',
            price: 200,
            tier: 'REGULAR'
        },
        {
            id: 2,
            seatNumber: 'A2',
            status: 'LOCKED',
            price: 200,
            tier: 'REGULAR'
        }
    ];

    beforeEach(() => {
        vi.clearAllMocks();

        getSeatsByEventId.mockResolvedValue(mockSeats);
        getMyActiveBooking.mockResolvedValue(null);
    });

    it('should display the event and seats after loading', async () => {

        render(<EventDetails />);

        expect(
            await screen.findByText('SnapSeat Live Concert')
        ).toBeInTheDocument();

        expect(
            screen.getByRole('button', { name: 'A1' })
        ).toBeInTheDocument();

        expect(
            screen.getByRole('button', { name: 'A2' })
        ).toBeInTheDocument();

    });

    it('should display an error when seats cannot be loaded', async () => {

        getSeatsByEventId.mockRejectedValueOnce(
            new Error('Network Error')
        );

        render(<EventDetails />);

        expect(
            await screen.findByText(
                'Unable to load seats or restore your booking.'
            )
        ).toBeInTheDocument();

    });

    it('should reserve a seat when an available seat is selected', async () => {

        const mockBooking = {
            id: 101,
            seatId: 1,
            status: 'PENDING',
            lockedUntil: '2026-11-23T10:00:00'
        };

        reserveSeat.mockResolvedValueOnce(mockBooking);

        render(<EventDetails />);

        const seatButton = await screen.findByRole('button', {
            name: 'A1'
        });

        fireEvent.click(seatButton);

        expect(
            await screen.findByText('Checkout')
        ).toBeInTheDocument();

        expect(reserveSeat).toHaveBeenCalledWith(1);

        expect(
            screen.getByText('Booking ID: 101')
        ).toBeInTheDocument();

    });

    it('should confirm booking and mark seat as booked after successful payment', async () => {

        const mockBooking = {
            id: 101,
            seatId: 1,
            status: 'PENDING',
            lockedUntil: '2026-11-23T10:00:00'
        };

        const confirmedBooking = {
            id: 101,
            seatId: 1,
            status: 'CONFIRMED'
        };

        reserveSeat.mockResolvedValueOnce(mockBooking);
        payForBooking.mockResolvedValueOnce(confirmedBooking);

        render(<EventDetails />);

        fireEvent.click(
            await screen.findByRole('button', { name: 'A1' })
        );

        const payButton = await screen.findByRole('button', {
            name: 'Pay Now - ₹200'
        });

        fireEvent.click(payButton);

        expect(
            await screen.findByText('Payment Successful!')
        ).toBeInTheDocument();

        expect(payForBooking).toHaveBeenCalledWith(101);

        expect(
            screen.getByRole('button', { name: 'A1' })
        ).toBeDisabled();

    });

    it('should display an error when payment fails due to a network issue', async () => {

        const mockBooking = {
            id: 101,
            seatId: 1,
            status: 'PENDING',
            lockedUntil: '2026-11-23T10:00:00'
        };

        reserveSeat.mockResolvedValueOnce(mockBooking);

        payForBooking.mockRejectedValueOnce(
            new Error('Network Error')
        );

        render(<EventDetails />);

        fireEvent.click(
            await screen.findByRole('button', { name: 'A1' })
        );

        const payButton = await screen.findByRole('button', {
            name: 'Pay Now - ₹200'
        });

        fireEvent.click(payButton);

        expect(
            await screen.findByText('Payment failed. Please try again.')
        ).toBeInTheDocument();

        expect(
            screen.getByText('Checkout')
        ).toBeInTheDocument();

        expect(
            screen.queryByText('Payment Successful!')
        ).not.toBeInTheDocument();

        expect(
            screen.getByRole('button', { name: 'A1' })
        ).toBeEnabled();

    });


});

