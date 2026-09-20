package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.shareit.config.ClientConfig;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItemClientTest {

    private ItemClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        client = new ClientConfig().itemClient("http://localhost:9090", new RestTemplateBuilder());
        server = MockRestServiceServer.bindTo(client.getRestTemplate()).build();
    }

    @Test
    void createItem_SendsPostWithUserHeader() {
        server.expect(requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);
        ResponseEntity<Object> response = client.createItem(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void updateItem_SendsPatchToItemsIdEndpoint() {
        server.expect(requestTo("http://localhost:9090/items/10"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":10}", MediaType.APPLICATION_JSON));

        ItemDto dto = new ItemDto();
        dto.setName("New name");
        ResponseEntity<Object> response = client.updateItem(1L, 10L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void getItemById_SendsGetToItemsIdEndpoint() {
        server.expect(requestTo("http://localhost:9090/items/10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":10}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getItemById(1L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void getItemsByOwnerId_SendsGetToItemsEndpoint() {
        server.expect(requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getItemsByOwnerId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void searchItems_SendsGetWithTextQueryParam() {
        server.expect(requestTo("http://localhost:9090/items/search?text=drill"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.searchItems("drill");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void addComment_SendsPostToCommentEndpoint() {
        server.expect(requestTo("http://localhost:9090/items/10/comment"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        CommentDto dto = new CommentDto();
        dto.setText("Great!");
        ResponseEntity<Object> response = client.addComment(1L, 10L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }
}
