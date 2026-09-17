package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserClientTest {

    private UserClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        client = new UserClient("http://localhost:9090", new RestTemplateBuilder());
        server = MockRestServiceServer.bindTo(client.getRestTemplate()).build();
    }

    @Test
    void createUser_SendsPostToUsersEndpoint() {
        server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.createUser(new UserDto(null, "Alice", "alice@example.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void getUserById_SendsGetToUsersIdEndpoint() {
        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void getAllUsers_SendsGetToUsersEndpoint() {
        server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void updateUser_SendsPatchToUsersIdEndpoint() {
        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.updateUser(1L, new UserDto(null, "Alice B", null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void deleteUser_SendsDeleteToUsersIdEndpoint() {
        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = client.deleteUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }
}
