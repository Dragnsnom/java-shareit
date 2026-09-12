package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test User", "test@test.com");
        item = new Item(1L, "Test Item", "Test Description", true, user, null);
        itemDto = new ItemDto(1L, "Test Item", "Test Description", true, null);
    }

    @Test
    void createItem_ValidData_ReturnsItemDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(1L, itemDto);

        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        verify(userRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(1L, itemDto));
    }

    @Test
    void createItem_InvalidData_ThrowsValidationException() {
        itemDto.setName("");
        itemDto.setDescription("");
        itemDto.setAvailable(null);
        assertThrows(ValidationException.class, () -> itemService.createItem(1L, itemDto));
    }

    @Test
    void updateItem_ValidData_ReturnsUpdatedItemDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto updateDto = new ItemDto(null, "Updated Name", null, null, null);
        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertEquals(itemDto.getId(), result.getId());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_NotOwner_ThrowsNotFoundException() {
        User anotherUser = new User(2L, "Another", "another@test.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(2L, 1L, itemDto));
    }

    @Test
    void getItemById_ExistingId_ReturnsItemDto() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(Collections.emptyList());

        ItemDto result = itemService.getItemById(1L, 1L);

        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
    }

    @Test
    void getItemsByOwnerId_ExistingOwner_ReturnsListOfItemDtos() {
        when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item));
        when(commentRepository.findAllByItemIdIn(List.of(1L))).thenReturn(Collections.emptyList());
        when(bookingRepository.findAllByItem_IdInAndStatusOrderByStartAsc(any(), any()))
                .thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getItemsByOwnerId(1L);

        assertEquals(1, result.size());
        assertEquals(itemDto.getId(), result.get(0).getId());
    }

    @Test
    void searchItems_ValidText_ReturnsListOfItemDtos() {
        when(itemRepository.search("Test")).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("Test");

        assertEquals(1, result.size());
        assertEquals(itemDto.getId(), result.get(0).getId());
    }
}
