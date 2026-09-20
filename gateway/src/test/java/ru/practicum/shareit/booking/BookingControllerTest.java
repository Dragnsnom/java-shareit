package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBooking_ValidData_DelegatesToClient() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingClient.bookItem(eq(2L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(eq(2L), any());
    }

    @Test
    void createBooking_MissingItemId_Returns400WithoutCallingClient() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(any(), any());
    }

    @Test
    void createBooking_StartInPast_Returns400() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(10L,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_EndNotInFuture_Returns400() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_UnknownState_Returns400WithoutCallingClient() throws Exception {
        mockMvc.perform(get("/bookings").header(USER_HEADER, 2L).param("state", "NOT_A_STATE"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(any(), any());
    }

    @Test
    void getUserBookings_ValidState_DelegatesToClient() throws Exception {
        when(bookingClient.getUserBookings(2L, BookingState.ALL)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings").header(USER_HEADER, 2L).param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingClient).getUserBookings(2L, BookingState.ALL);
    }

    @Test
    void approveBooking_DelegatesToClient() throws Exception {
        when(bookingClient.approveBooking(1L, 1L, true)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(patch("/bookings/1").header(USER_HEADER, 1L).param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approveBooking(1L, 1L, true);
    }
}
