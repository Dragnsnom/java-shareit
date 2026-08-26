package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @Override
    public User save(User user) {
        checkEmailDuplication(user.getEmail(), user.getId());
        if (user.getId() == null) {
            user.setId(++currentId);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }
        users.remove(id);
    }

    @Override
    public User update(Long id, User user) {
        User existingUser = users.get(id);
        if (existingUser == null) {
            throw new NotFoundException("User not found with id: " + id);
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            checkEmailDuplication(user.getEmail(), id);
            existingUser.setEmail(user.getEmail());
        }
        return existingUser;
    }

    private void checkEmailDuplication(String email, Long userId) {
        boolean duplicate = users.values().stream()
                .anyMatch(u -> u.getEmail().equals(email) && !Objects.equals(u.getId(), userId));
        if (duplicate) {
            throw new ConflictException("Email already exists: " + email);
        }
    }
}
