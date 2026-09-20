package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@example.com"));
    }

    @Test
    void createItem_WithoutRequest_PersistsItem() {
        ItemDto created = itemService.createItem(owner.getId(),
                new ItemDto(null, "Drill", "Powerful drill", true, null));

        assertEquals("Drill", created.getName());
        assertEquals(created.getId(), itemService.getItemById(owner.getId(), created.getId()).getId());
    }

    @Test
    void createItem_WithRequestId_LinksToRequest() {
        ItemRequest request = itemRequestRepository.save(itemRequestOf(booker, "Need a drill"));

        ItemDto created = itemService.createItem(owner.getId(),
                new ItemDto(null, "Drill", "Powerful drill", true, request.getId()));

        assertEquals(request.getId(), created.getRequestId());
    }

    @Test
    void createItem_RequestNotFound_ThrowsNotFoundException() {
        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true, 999L);

        assertThrows(NotFoundException.class, () -> itemService.createItem(owner.getId(), itemDto));
    }

    @Test
    void getItemsByOwnerId_ReturnsOnlyOwnersItems() {
        itemService.createItem(owner.getId(), new ItemDto(null, "Drill", "Powerful drill", true, null));
        itemService.createItem(booker.getId(), new ItemDto(null, "Ladder", "Tall ladder", true, null));

        List<ItemDto> ownerItems = itemService.getItemsByOwnerId(owner.getId());

        assertEquals(1, ownerItems.size());
        assertEquals("Drill", ownerItems.get(0).getName());
    }

    @Test
    void updateItem_NotOwner_ThrowsNotFoundException() {
        ItemDto created = itemService.createItem(owner.getId(),
                new ItemDto(null, "Drill", "Powerful drill", true, null));
        ItemDto update = new ItemDto(null, "New name", null, null, null);

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(booker.getId(), created.getId(), update));
    }

    @Test
    void searchItems_MatchesNameOrDescription() {
        itemService.createItem(owner.getId(), new ItemDto(null, "Drill", "Powerful drill", true, null));

        List<ItemDto> results = itemService.searchItems("drill");

        assertEquals(1, results.size());
    }

    @Test
    void addComment_AfterCompletedBooking_PersistsComment() {
        ItemDto item = itemService.createItem(owner.getId(),
                new ItemDto(null, "Drill", "Powerful drill", true, null));

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        booking.setBooker(booker);
        booking.setItem(itemFrom(item, owner));
        bookingRepository.save(booking);

        CommentDto comment = itemService.addComment(booker.getId(), item.getId(),
                new CommentDto(null, "Great drill!", null, null));

        assertEquals("Great drill!", comment.getText());
        assertEquals("Booker", comment.getAuthorName());
    }

    @Test
    void getItemById_AsOwner_WithPastAndFutureBookings_PopulatesLastAndNextBooking() {
        ItemDto item = itemService.createItem(owner.getId(),
                new ItemDto(null, "Drill", "Powerful drill", true, null));
        ru.practicum.shareit.item.model.Item entity = itemFrom(item, owner);

        Booking past = new Booking();
        past.setStart(LocalDateTime.now().minusDays(2));
        past.setEnd(LocalDateTime.now().minusDays(1));
        past.setStatus(BookingStatus.APPROVED);
        past.setBooker(booker);
        past.setItem(entity);
        bookingRepository.save(past);

        Booking future = new Booking();
        future.setStart(LocalDateTime.now().plusDays(1));
        future.setEnd(LocalDateTime.now().plusDays(2));
        future.setStatus(BookingStatus.APPROVED);
        future.setBooker(booker);
        future.setItem(entity);
        bookingRepository.save(future);

        ItemDto result = itemService.getItemById(owner.getId(), item.getId());

        assertEquals(past.getId(), result.getLastBooking().getId());
        assertEquals(future.getId(), result.getNextBooking().getId());
    }

    @Test
    void getItemsByOwnerId_WithCommentsAndBookings_PopulatesAggregates() {
        ItemDto item = itemService.createItem(owner.getId(),
                new ItemDto(null, "Drill", "Powerful drill", true, null));
        ru.practicum.shareit.item.model.Item entity = itemFrom(item, owner);

        Booking past = new Booking();
        past.setStart(LocalDateTime.now().minusDays(2));
        past.setEnd(LocalDateTime.now().minusDays(1));
        past.setStatus(BookingStatus.APPROVED);
        past.setBooker(booker);
        past.setItem(entity);
        bookingRepository.save(past);

        itemService.addComment(booker.getId(), item.getId(), new CommentDto(null, "Nice!", null, null));

        List<ItemDto> result = itemService.getItemsByOwnerId(owner.getId());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getComments().size());
        assertEquals(past.getId(), result.get(0).getLastBooking().getId());
    }

    @Test
    void addComment_WithoutCompletedBooking_ThrowsValidationException() {
        ItemDto item = itemService.createItem(owner.getId(),
                new ItemDto(null, "Drill", "Powerful drill", true, null));
        CommentDto commentDto = new CommentDto(null, "Great drill!", null, null);

        assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), item.getId(), commentDto));
    }

    private ItemRequest itemRequestOf(User requestor, String description) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    private ru.practicum.shareit.item.model.Item itemFrom(ItemDto dto, User itemOwner) {
        ru.practicum.shareit.item.model.Item item = new ru.practicum.shareit.item.model.Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setOwner(itemOwner);
        return item;
    }
}
