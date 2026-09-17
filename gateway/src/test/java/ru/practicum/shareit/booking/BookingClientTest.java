package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BookingClientTest {

    private BookingClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        client = new BookingClient("http://localhost:9090", new RestTemplateBuilder());
        server = MockRestServiceServer.bindTo(client.getRestTemplate()).build();
    }

    @Test
    void bookItem_SendsPostToBookingsEndpoint() {
        server.expect(requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        BookItemRequestDto dto = new BookItemRequestDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        ResponseEntity<Object> response = client.bookItem(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void approveBooking_SendsPatchWithApprovedQueryParam() {
        server.expect(requestTo("http://localhost:9090/bookings/1?approved=true"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.approveBooking(1L, 1L, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void getBooking_SendsGetToBookingsIdEndpoint() {
        server.expect(requestTo("http://localhost:9090/bookings/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getBooking(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void getUserBookings_SendsGetWithStateQueryParam() {
        server.expect(requestTo("http://localhost:9090/bookings?state=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getUserBookings(1L, BookingState.ALL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void getOwnerBookings_SendsGetToOwnerEndpointWithStateQueryParam() {
        server.expect(requestTo("http://localhost:9090/bookings/owner?state=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getOwnerBookings(1L, BookingState.ALL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }
}
