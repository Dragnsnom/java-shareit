package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ShareItAppIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void contextLoadsAndSchemaSupportsFullPersistenceFlow() {
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        User booker = userRepository.save(new User(null, "Booker", "booker@example.com"));

        Item item = itemRepository.save(new Item(null, "Drill", "Powerful drill", true, owner, null));

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Works great!");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        List<Item> ownerItems = itemRepository.findAllByOwnerId(owner.getId());
        assertEquals(1, ownerItems.size());

        List<Item> searchResults = itemRepository.search("drill");
        assertEquals(1, searchResults.size());

        boolean hasBooked = bookingRepository.existsByItem_IdAndBooker_IdAndEndBeforeAndStatus(
                item.getId(), booker.getId(), LocalDateTime.now(), BookingStatus.APPROVED);
        assertTrue(hasBooked);

        List<Comment> comments = commentRepository.findAllByItemId(item.getId());
        assertEquals(1, comments.size());
        assertEquals(booking.getId(), bookingRepository.findAllByBookerIdOrderByStartDesc(booker.getId())
                .get(0).getId());
    }
}
