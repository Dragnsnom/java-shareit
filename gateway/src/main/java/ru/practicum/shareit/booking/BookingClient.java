package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

public class BookingClient extends BaseClient {
    public static final String API_PREFIX = "/bookings";

    public BookingClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> bookItem(Long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> approveBooking(Long userId, Long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBooking(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return get("/owner?state={state}", userId, parameters);
    }
}
