package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createRequest_ValidData_DelegatesToClient() throws Exception {
        NewItemRequestDto request = new NewItemRequestDto();
        request.setDescription("Need a drill");
        when(itemRequestClient.createRequest(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(itemRequestClient).createRequest(eq(1L), any());
    }

    @Test
    void createRequest_BlankDescription_Returns400WithoutCallingClient() throws Exception {
        NewItemRequestDto request = new NewItemRequestDto();
        request.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).createRequest(any(), any());
    }

    @Test
    void createRequest_MissingUserHeader_Returns400() throws Exception {
        NewItemRequestDto request = new NewItemRequestDto();
        request.setDescription("Need a drill");

        mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnRequests_DelegatesToClient() throws Exception {
        when(itemRequestClient.getOwnRequests(1L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests").header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).getOwnRequests(1L);
    }

    @Test
    void getAllRequests_DelegatesToClient() throws Exception {
        when(itemRequestClient.getAllRequests(1L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests/all").header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAllRequests(1L);
    }

    @Test
    void getRequestById_DelegatesToClient() throws Exception {
        when(itemRequestClient.getRequestById(1L, 5L)).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mockMvc.perform(get("/requests/5").header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).getRequestById(1L, 5L);
    }
}
