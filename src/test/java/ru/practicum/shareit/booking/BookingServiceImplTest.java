package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@test.com");
        booker = new User(2L, "Booker", "booker@test.com");
        item = new Item(1L, "Test Item", "Test Description", true, owner, null);
        requestDto = new BookItemRequestDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(requestDto.getStart());
        booking.setEnd(requestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
    }

    @Test
    void createBooking_ValidData_ReturnsBookingDto() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.createBooking(2L, requestDto);

        assertEquals(booking.getId(), result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_EndBeforeStart_ThrowsValidationException() {
        BookItemRequestDto invalidDto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(2L, invalidDto));
    }

    @Test
    void createBooking_ItemNotAvailable_ThrowsValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(2L, requestDto));
    }

    @Test
    void createBooking_OwnerBooksOwnItem_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(1L, requestDto));
    }

    @Test
    void approveBooking_ByOwner_ApprovesBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approveBooking(1L, 1L, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void approveBooking_ByNonOwner_ThrowsNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(2L, 1L, true));
    }

    @Test
    void getBooking_ByBookerOrOwner_ReturnsBookingDto() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBooking(2L, 1L);

        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBooking_ByUnrelatedUser_ThrowsNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getBooking(3L, 1L));
    }

    @Test
    void getUserBookings_AllState_ReturnsBookings() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(2L)).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getUserBookings(2L, BookingState.ALL);

        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_AllState_ReturnsBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(1L)).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getOwnerBookings(1L, BookingState.ALL);

        assertEquals(1, result.size());
    }
}
