package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_PersistsUserInDatabase() {
        UserDto created = userService.createUser(new UserDto(null, "Alice", "alice@example.com"));

        assertTrue(userRepository.findById(created.getId()).isPresent());
        assertEquals("alice@example.com", userRepository.findById(created.getId()).get().getEmail());
    }

    @Test
    void createUser_DuplicateEmail_ThrowsConflictException() {
        userService.createUser(new UserDto(null, "Alice", "alice@example.com"));

        assertThrows(ConflictException.class,
                () -> userService.createUser(new UserDto(null, "Bob", "alice@example.com")));
    }

    @Test
    void getUserById_ReturnsPersistedUser() {
        UserDto created = userService.createUser(new UserDto(null, "Alice", "alice@example.com"));

        UserDto found = userService.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Alice", found.getName());
    }

    @Test
    void getAllUsers_ReturnsAllPersistedUsers() {
        userService.createUser(new UserDto(null, "Alice", "alice@example.com"));
        userService.createUser(new UserDto(null, "Bob", "bob@example.com"));

        List<UserDto> all = userService.getAllUsers();

        assertEquals(2, all.size());
    }

    @Test
    void updateUser_PartialUpdate_OnlyChangesProvidedFields() {
        UserDto created = userService.createUser(new UserDto(null, "Alice", "alice@example.com"));

        UserDto updated = userService.updateUser(created.getId(), new UserDto(null, "Alice B", null));

        assertEquals("Alice B", updated.getName());
        assertEquals("alice@example.com", updated.getEmail());
    }

    @Test
    void deleteUser_RemovesUserFromDatabase() {
        UserDto created = userService.createUser(new UserDto(null, "Alice", "alice@example.com"));

        userService.deleteUser(created.getId());

        assertFalse(userRepository.existsById(created.getId()));
    }

    @Test
    void deleteUser_NonExisting_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(999L));
    }
}
