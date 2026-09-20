package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BaseClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private BaseClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:9090/items"));
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new BaseClient(restTemplate);
    }

    @Test
    void get_SuccessfulResponse_ReturnsBodyAndForwardsUserHeader() {
        server.expect(requestTo("http://localhost:9090/items/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "5"))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Drill\"}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/1", 5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        server.verify();
    }

    @Test
    void get_WithoutUserId_DoesNotSendUserHeader() {
        server.expect(requestTo("http://localhost:9090/items/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/1", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void get_WithQueryParameters_ExpandsThem() {
        server.expect(requestTo("http://localhost:9090/items/search?text=drill"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/search?text={text}", null, Map.of("text", "drill"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void post_SendsJsonBodyAndReturnsCreatedResponse() {
        server.expect(requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .body("{\"id\":1,\"name\":\"Drill\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.post("", 1L, Map.of("name", "Drill"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        server.verify();
    }

    @Test
    void patch_SendsPatchMethod() {
        server.expect(requestTo("http://localhost:9090/items/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.patch("/1", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void patch_WithQueryParameters_ExpandsThem() {
        server.expect(requestTo("http://localhost:9090/items/1?approved=true"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response =
                client.patch("/1?approved={approved}", 1L, Map.of("approved", true), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void delete_SendsDeleteMethod() {
        server.expect(requestTo("http://localhost:9090/items/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = client.delete("/1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void get_ServerReturnsError_PassesThroughStatusAndBody() {
        server.expect(requestTo("http://localhost:9090/items/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Вещь не найдена\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/999", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        server.verify();
    }

    @Test
    void getRestTemplate_ReturnsUnderlyingTemplate() {
        assertThat(client.getRestTemplate()).isSameAs(restTemplate);
    }
}
