package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;

@FunctionalInterface
public interface BookingStateFetchStrategy {
    List<Booking> fetch(BookingRepository bookingRepository, Long id, LocalDateTime now);
}
