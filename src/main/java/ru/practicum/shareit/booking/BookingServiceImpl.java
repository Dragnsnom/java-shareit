package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private static final Map<BookingState, BookingStateFetchStrategy> BOOKER_STRATEGIES = Map.of(
            BookingState.CURRENT, (repository, id, now) -> repository
                    .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(id, now, now),
            BookingState.PAST, (repository, id, now) -> repository
                    .findAllByBookerIdAndEndBeforeOrderByStartDesc(id, now),
            BookingState.FUTURE, (repository, id, now) -> repository
                    .findAllByBookerIdAndStartAfterOrderByStartDesc(id, now),
            BookingState.WAITING, (repository, id, now) -> repository
                    .findAllByBookerIdAndStatusOrderByStartDesc(id, BookingStatus.WAITING),
            BookingState.REJECTED, (repository, id, now) -> repository
                    .findAllByBookerIdAndStatusOrderByStartDesc(id, BookingStatus.REJECTED),
            BookingState.ALL, (repository, id, now) -> repository.findAllByBookerIdOrderByStartDesc(id)
    );

    private static final Map<BookingState, BookingStateFetchStrategy> OWNER_STRATEGIES = Map.of(
            BookingState.CURRENT, (repository, id, now) -> repository
                    .findAllByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(id, now, now),
            BookingState.PAST, (repository, id, now) -> repository
                    .findAllByItem_Owner_IdAndEndBeforeOrderByStartDesc(id, now),
            BookingState.FUTURE, (repository, id, now) -> repository
                    .findAllByItem_Owner_IdAndStartAfterOrderByStartDesc(id, now),
            BookingState.WAITING, (repository, id, now) -> repository
                    .findAllByItem_Owner_IdAndStatusOrderByStartDesc(id, BookingStatus.WAITING),
            BookingState.REJECTED, (repository, id, now) -> repository
                    .findAllByItem_Owner_IdAndStatusOrderByStartDesc(id, BookingStatus.REJECTED),
            BookingState.ALL, (repository, id, now) -> repository.findAllByItem_Owner_IdOrderByStartDesc(id)
    );

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository,
                               UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookItemRequestDto requestDto) {
        if (requestDto.getStart() == null || requestDto.getEnd() == null
                || !requestDto.getEnd().isAfter(requestDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(requestDto, item, booker);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является владельцем вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже определён");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        if (!ownerId.equals(userId) && !bookerId.equals(userId)) {
            throw new NotFoundException("Доступ к бронированию запрещён");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<Booking> bookings = BOOKER_STRATEGIES.get(state)
                .fetch(bookingRepository, userId, LocalDateTime.now());
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<Booking> bookings = OWNER_STRATEGIES.get(state)
                .fetch(bookingRepository, userId, LocalDateTime.now());
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }
}
