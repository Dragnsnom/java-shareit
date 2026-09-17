package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    public static Booking toBooking(BookItemRequestDto dto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());
        dto.setItem(new BookingDto.ItemShort(booking.getItem().getId(), booking.getItem().getName()));
        dto.setBooker(new BookingDto.UserShort(booking.getBooker().getId(), booking.getBooker().getName()));
        return dto;
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        return new BookingShortDto(booking.getId(), booking.getBooker().getId());
    }
}
