package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test User", "test@test.com");
        userDto = new UserDto(1L, "Test User", "test@test.com");
    }

    @Test
    void createUser_ValidDto_ReturnsCreatedUserDto() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_NullEmail_ThrowsValidationException() {
        userDto.setEmail(null);

        assertThrows(ValidationException.class, () -> userService.createUser(userDto));
    }

    @Test
    void createUser_ConflictEmail_ThrowsConflictException() {
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(new User(2L, "Other", "test@test.com")));

        assertThrows(ConflictException.class, () -> userService.createUser(userDto));
    }

    @Test
    void getUserById_ExistingId_ReturnsUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_NonExistingId_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getAllUsers_ReturnsListOfUserDtos() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(userDto.getId(), result.get(0).getId());
    }

    @Test
    void updateUser_ValidDto_ReturnsUpdatedUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto updateDto = new UserDto(null, "Updated Name", null);
        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals(userDto.getId(), result.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_ExistingId_CallsDeleteOnRepository() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistingId_ThrowsNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
    }
}
