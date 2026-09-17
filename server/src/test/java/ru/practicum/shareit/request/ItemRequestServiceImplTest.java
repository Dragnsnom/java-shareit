package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requestor;
    private ItemRequest itemRequest;
    private NewItemRequestDto newItemRequestDto;

    @BeforeEach
    void setUp() {
        requestor = new User(1L, "Requestor", "requestor@test.com");
        itemRequest = new ItemRequest(1L, "Need a drill", requestor, LocalDateTime.now());
        newItemRequestDto = new NewItemRequestDto("Need a drill");
    }

    @Test
    void createRequest_ValidData_ReturnsItemRequestDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.createRequest(1L, newItemRequestDto);

        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void createRequest_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(1L, newItemRequestDto));
    }

    @Test
    void getOwnRequests_ExistingUser_ReturnsRequestsWithItems() {
        User owner = new User(2L, "Owner", "owner@test.com");
        Item item = new Item(10L, "Drill", "Powerful drill", true, owner, itemRequest);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(List.of(1L))).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getOwnRequests(1L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals("Drill", result.get(0).getItems().get(0).getName());
        assertEquals(2L, result.get(0).getItems().get(0).getOwnerId());
    }

    @Test
    void getOwnRequests_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getOwnRequests(1L));
    }

    @Test
    void getAllRequests_ExcludesOwnRequests() {
        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(1L)).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(List.of(1L))).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getItems().isEmpty());
    }

    @Test
    void getRequestById_ExistingRequest_ReturnsItemRequestDtoWithItems() {
        User owner = new User(2L, "Owner", "owner@test.com");
        Item item = new Item(10L, "Drill", "Powerful drill", true, owner, itemRequest);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getRequestById(1L, 1L);

        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getRequestById_RequestNotFound_ThrowsNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(1L, 1L));
    }

    @Test
    void getRequestById_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(1L, 1L));
    }
}
