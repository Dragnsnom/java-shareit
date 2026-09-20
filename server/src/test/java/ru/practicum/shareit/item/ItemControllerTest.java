package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_ReturnsCreatedItem() throws Exception {
        ItemDto request = new ItemDto(null, "Drill", "Powerful drill", true, null);
        ItemDto response = new ItemDto(1L, "Drill", "Powerful drill", true, null);
        when(itemService.createItem(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void createItem_WithRequestId_ForwardsRequestId() throws Exception {
        ItemDto request = new ItemDto(null, "Drill", "Powerful drill", true, 5L);
        ItemDto response = new ItemDto(1L, "Drill", "Powerful drill", true, 5L);
        when(itemService.createItem(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(5));
    }

    @Test
    void createItem_UnknownUser_Returns404() throws Exception {
        ItemDto request = new ItemDto(null, "Drill", "Powerful drill", true, null);
        when(itemService.createItem(eq(1L), any())).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_ReturnsUpdatedItem() throws Exception {
        ItemDto request = new ItemDto(null, "New name", null, null, null);
        when(itemService.updateItem(eq(1L), eq(10L), any()))
                .thenReturn(new ItemDto(10L, "New name", "Powerful drill", true, null));

        mockMvc.perform(patch("/items/10")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    void getItemById_ReturnsItem() throws Exception {
        when(itemService.getItemById(1L, 10L)).thenReturn(new ItemDto(10L, "Drill", "Powerful drill", true, null));

        mockMvc.perform(get("/items/10").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getItemsByOwnerId_ReturnsList() throws Exception {
        when(itemService.getItemsByOwnerId(1L))
                .thenReturn(List.of(new ItemDto(10L, "Drill", "Powerful drill", true, null)));

        mockMvc.perform(get("/items").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchItems_ReturnsMatches() throws Exception {
        when(itemService.searchItems("drill"))
                .thenReturn(List.of(new ItemDto(10L, "Drill", "Powerful drill", true, null)));

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addComment_ReturnsCreatedComment() throws Exception {
        CommentDto request = new CommentDto(null, "Great!", null, null);
        when(itemService.addComment(eq(1L), eq(10L), any()))
                .thenReturn(new CommentDto(1L, "Great!", "Alice", null));

        mockMvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great!"))
                .andExpect(jsonPath("$.authorName").value("Alice"));
    }

    @Test
    void addComment_WithoutCompletedBooking_Returns400() throws Exception {
        CommentDto request = new CommentDto(null, "Great!", null, null);
        when(itemService.addComment(eq(1L), eq(10L), any()))
                .thenThrow(new ValidationException("Пользователь не арендовал эту вещь"));

        mockMvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
