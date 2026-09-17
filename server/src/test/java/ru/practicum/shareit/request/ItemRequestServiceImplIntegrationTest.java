package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requestor = userRepository.save(new User(null, "Requestor", "requestor@example.com"));
        otherUser = userRepository.save(new User(null, "Other", "other@example.com"));
    }

    @Test
    void createRequest_PersistsRequestWithCreatedTimestamp() {
        ItemRequestDto created = itemRequestService.createRequest(requestor.getId(),
                new NewItemRequestDto("Need a drill"));

        assertEquals("Need a drill", created.getDescription());
        assertTrue(created.getItems().isEmpty());
        assertNotNull(created.getId());
        assertNotNull(created.getCreated());
    }

    @Test
    void createRequest_UnknownUser_ThrowsNotFoundException() {
        NewItemRequestDto dto = new NewItemRequestDto("Need a drill");

        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(999L, dto));
    }

    @Test
    void getOwnRequests_ReturnsNewestFirstWithResponses() {
        ItemRequestDto first = itemRequestService.createRequest(requestor.getId(),
                new NewItemRequestDto("Need a drill"));
        ItemRequestDto second = itemRequestService.createRequest(requestor.getId(),
                new NewItemRequestDto("Need a ladder"));
        itemService.createItem(otherUser.getId(),
                new ItemDto(null, "Drill", "Powerful drill", true, first.getId()));

        List<ItemRequestDto> ownRequests = itemRequestService.getOwnRequests(requestor.getId());

        assertEquals(2, ownRequests.size());
        assertEquals(second.getId(), ownRequests.get(0).getId());
        assertEquals(1, ownRequests.get(1).getItems().size());
        assertEquals(otherUser.getId(), ownRequests.get(1).getItems().get(0).getOwnerId());
    }

    @Test
    void getAllRequests_ExcludesCallersOwnRequests() {
        itemRequestService.createRequest(requestor.getId(), new NewItemRequestDto("Need a drill"));

        List<ItemRequestDto> allForOther = itemRequestService.getAllRequests(otherUser.getId());
        List<ItemRequestDto> allForRequestor = itemRequestService.getAllRequests(requestor.getId());

        assertEquals(1, allForOther.size());
        assertTrue(allForRequestor.isEmpty());
    }

    @Test
    void getRequestById_AnyUserCanView() {
        ItemRequestDto created = itemRequestService.createRequest(requestor.getId(),
                new NewItemRequestDto("Need a drill"));

        ItemRequestDto found = itemRequestService.getRequestById(otherUser.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getRequestById_UnknownRequest_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requestor.getId(), 999L));
    }
}
