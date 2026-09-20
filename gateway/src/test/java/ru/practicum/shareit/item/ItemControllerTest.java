package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    private ItemClient itemClient;

    @Test
    void createItem_ValidData_DelegatesToClient() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Drill");
        request.setDescription("Powerful drill");
        request.setAvailable(true);
        when(itemClient.createItem(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createItem_WithRequestId_ForwardsToClient() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Drill");
        request.setDescription("Powerful drill");
        request.setAvailable(true);
        request.setRequestId(5L);
        when(itemClient.createItem(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 10, "requestId", 5)));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(5));
    }

    @Test
    void createItem_BlankName_Returns400WithoutCallingClient() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("");
        request.setDescription("Powerful drill");
        request.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(any(), any());
    }

    @Test
    void createItem_MissingAvailable_Returns400() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Drill");
        request.setDescription("Powerful drill");

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_MissingUserHeader_Returns400() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Drill");
        request.setDescription("Powerful drill");
        request.setAvailable(true);

        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_BlankText_Returns400() throws Exception {
        CommentDto request = new CommentDto();
        request.setText("");

        mockMvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_DelegatesToClient() throws Exception {
        when(itemClient.searchItems("drill")).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk());

        verify(itemClient).searchItems("drill");
    }

    @Test
    void updateItem_DelegatesToClient() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("New name");
        when(itemClient.updateItem(eq(1L), eq(10L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(patch("/items/10")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getItemById_DelegatesToClient() throws Exception {
        when(itemClient.getItemById(1L, 10L)).thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(get("/items/10").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getItemsByOwnerId_DelegatesToClient() throws Exception {
        when(itemClient.getItemsByOwnerId(1L)).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mockMvc.perform(get("/items").header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemClient).getItemsByOwnerId(1L);
    }

    @Test
    void addComment_ValidData_DelegatesToClient() throws Exception {
        CommentDto request = new CommentDto();
        request.setText("Great");
        when(itemClient.addComment(eq(1L), eq(10L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
