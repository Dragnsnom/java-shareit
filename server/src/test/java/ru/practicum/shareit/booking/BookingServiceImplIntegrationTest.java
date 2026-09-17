package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@example.com"));
        item = itemRepository.save(new Item(null, "Drill", "Powerful drill", true, owner, null));
    }

    @Test
    void createBooking_ValidData_PersistsBooking() {
        BookItemRequestDto request = new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.createBooking(booker.getId(), request);

        assertEquals(BookingStatus.WAITING, created.getStatus());
        assertEquals(item.getId(), created.getItem().getId());
    }

    @Test
    void createBooking_OwnerBooksOwnItem_ThrowsNotFoundException() {
        BookItemRequestDto request = new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(owner.getId(), request));
    }

    @Test
    void approveBooking_ByOwner_ChangesStatusToApproved() {
        BookingDto created = bookingService.createBooking(booker.getId(), new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        BookingDto approved = bookingService.approveBooking(owner.getId(), created.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void approveBooking_ByNonOwner_ThrowsValidationException() {
        BookingDto created = bookingService.createBooking(booker.getId(), new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(booker.getId(), created.getId(), true));
    }

    @Test
    void getBooking_ByUnrelatedUser_ThrowsNotFoundException() {
        BookingDto created = bookingService.createBooking(booker.getId(), new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));
        User stranger = userRepository.save(new User(null, "Stranger", "stranger@example.com"));

        assertThrows(NotFoundException.class, () -> bookingService.getBooking(stranger.getId(), created.getId()));
    }

    @Test
    void getUserBookings_AllState_ReturnsBookersBookings() {
        bookingService.createBooking(booker.getId(), new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        List<BookingDto> bookings = bookingService.getUserBookings(booker.getId(), BookingState.ALL);

        assertEquals(1, bookings.size());
    }

    @Test
    void getOwnerBookings_AllState_ReturnsBookingsForOwnersItems() {
        bookingService.createBooking(booker.getId(), new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        List<BookingDto> bookings = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL);

        assertEquals(1, bookings.size());
    }
}
