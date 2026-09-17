package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BookItemRequestDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validate_AllFieldsPresentAndFuture_NoViolations() {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_MissingItemId_HasViolation() {
        BookItemRequestDto dto = new BookItemRequestDto(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("itemId");
    }

    @Test
    void validate_StartInPast_HasViolation() {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("start");
    }

    @Test
    void validate_EndNotInFuture_HasViolation() {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1));

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("end");
    }
}
