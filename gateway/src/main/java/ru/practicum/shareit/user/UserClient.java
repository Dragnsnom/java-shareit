package ru.practicum.shareit.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

public class UserClient extends BaseClient {
    public static final String API_PREFIX = "/users";

    public UserClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post("", userDto);
    }

    public ResponseEntity<Object> getUserById(Long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> updateUser(Long userId, UserDto userDto) {
        return patch("/" + userId, userDto);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete("/" + userId);
    }
}
