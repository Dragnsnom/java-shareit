package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto sampleBookingDto() {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setStatus(BookingStatus.WAITING);
        dto.setItem(new BookingDto.ItemShort(10L, "Drill"));
        dto.setBooker(new BookingDto.UserShort(2L, "Booker"));
        return dto;
    }

    @Test
    void createBooking_ReturnsCreatedBooking() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingService.createBooking(eq(2L), any())).thenReturn(sampleBookingDto());

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(10))
                .andExpect(jsonPath("$.booker.id").value(2));
    }

    @Test
    void createBooking_ItemNotAvailable_Returns400() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingService.createBooking(eq(2L), any()))
                .thenThrow(new ValidationException("Вещь недоступна для бронирования"));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_ReturnsApprovedBooking() throws Exception {
        BookingDto approved = sampleBookingDto();
        approved.setStatus(BookingStatus.APPROVED);
        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(approved);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBooking_ReturnsBooking() throws Exception {
        when(bookingService.getBooking(2L, 1L)).thenReturn(sampleBookingDto());

        mockMvc.perform(get("/bookings/1").header(USER_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getBooking_UnrelatedUser_Returns404() throws Exception {
        when(bookingService.getBooking(3L, 1L)).thenThrow(new NotFoundException("Доступ к бронированию запрещён"));

        mockMvc.perform(get("/bookings/1").header(USER_HEADER, 3L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookings_ReturnsList() throws Exception {
        when(bookingService.getUserBookings(2L, BookingState.ALL)).thenReturn(List.of(sampleBookingDto()));

        mockMvc.perform(get("/bookings").header(USER_HEADER, 2L).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUserBookings_UnknownState_Returns400() throws Exception {
        mockMvc.perform(get("/bookings").header(USER_HEADER, 2L).param("state", "NOT_A_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_ReturnsList() throws Exception {
        when(bookingService.getOwnerBookings(1L, BookingState.ALL)).thenReturn(List.of(sampleBookingDto()));

        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 1L).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
