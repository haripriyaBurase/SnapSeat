
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import SeatMap from '../components/SeatMap';

describe('SeatMap Component', () => {

    const mockSeats = [
        {
            id: 1,
            seatNumber: 'A1',
            status: 'AVAILABLE'
        },
        {
            id: 2,
            seatNumber: 'A2',
            status: 'LOCKED'
        },
        {
            id: 3,
            seatNumber: 'A3',
            status: 'BOOKED'
        }
    ];

    it('should render all seats', () => {

        render(
            <SeatMap seats={mockSeats} onSelect={vi.fn()} />
        );

        expect(screen.getByRole('button', { name: 'A1' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'A2' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'A3' })).toBeInTheDocument();

    });

    it('should enable available seats', () => {

        render(
            <SeatMap seats={mockSeats} onSelect={vi.fn()} />
        );

        expect(screen.getByRole('button', { name: 'A1' })).toBeEnabled();

    });

    it('should disable locked and booked seats', () => {

        render(
            <SeatMap seats={mockSeats} onSelect={vi.fn()} />
        );

        expect(screen.getByRole('button', { name: 'A2' })).toBeDisabled();
        expect(screen.getByRole('button', { name: 'A3' })).toBeDisabled();

    });

    it('should call onSelect when an available seat is clicked', () => {

        const mockOnSelect = vi.fn();

        render(
            <SeatMap seats={mockSeats} onSelect={mockOnSelect} />
        );

        fireEvent.click(screen.getByRole('button', { name: 'A1' }));

        expect(mockOnSelect).toHaveBeenCalledWith(mockSeats[0]);

    });

    it('should not select locked or booked seats', () => {

        const mockOnSelect = vi.fn();

        render(
            <SeatMap seats={mockSeats} onSelect={mockOnSelect} />
        );

        fireEvent.click(screen.getByRole('button', { name: 'A2' }));
        fireEvent.click(screen.getByRole('button', { name: 'A3' }));

        expect(mockOnSelect).not.toHaveBeenCalled();

    });

});
