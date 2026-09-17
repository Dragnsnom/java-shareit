package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookItemRequestDto> json;

    @Test
    void deserialize_ParsesIsoLocalDateTimeFields() throws Exception {
        String content = "{\"itemId\":10,\"start\":\"2024-06-01T10:00:00\",\"end\":\"2024-06-02T12:30:00\"}";

        BookItemRequestDto dto = json.parse(content).getObject();

        assertThat(dto.getItemId()).isEqualTo(10L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 6, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 6, 2, 12, 30, 0));
    }

    @Test
    void serialize_WritesDatesInIsoLocalFormatWithoutOffset() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(10L,
                LocalDateTime.of(2024, 6, 1, 10, 0, 0), LocalDateTime.of(2024, 6, 2, 12, 30, 0));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2024-06-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2024-06-02T12:30:00");
    }
}
