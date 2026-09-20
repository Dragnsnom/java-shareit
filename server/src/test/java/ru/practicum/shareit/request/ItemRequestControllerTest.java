package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequest_ReturnsCreatedRequest() throws Exception {
        NewItemRequestDto request = new NewItemRequestDto("Need a drill");
        ItemRequestDto response = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), List.of());
        when(itemRequestService.createRequest(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void getOwnRequests_ReturnsListWithItems() throws Exception {
        ItemRequestDto response = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(),
                List.of(new ItemForRequestDto(10L, "Drill", 2L)));
        when(itemRequestService.getOwnRequests(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/requests").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items[0].name").value("Drill"))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(2));
    }

    @Test
    void getAllRequests_ReturnsList() throws Exception {
        when(itemRequestService.getAllRequests(1L))
                .thenReturn(List.of(new ItemRequestDto(2L, "Need a ladder", LocalDateTime.now(), List.of())));

        mockMvc.perform(get("/requests/all").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequestById_ReturnsRequest() throws Exception {
        when(itemRequestService.getRequestById(1L, 5L))
                .thenReturn(new ItemRequestDto(5L, "Need a drill", LocalDateTime.now(), List.of()));

        mockMvc.perform(get("/requests/5").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getRequestById_UnknownRequest_Returns404() throws Exception {
        when(itemRequestService.getRequestById(1L, 999L)).thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/999").header(USER_HEADER, 1L))
                .andExpect(status().isNotFound());
    }
}
