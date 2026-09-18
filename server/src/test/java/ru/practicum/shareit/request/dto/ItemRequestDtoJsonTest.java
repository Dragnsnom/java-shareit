package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serialize_WritesCreatedInIsoLocalFormatAndNestedItems() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Need a drill",
                LocalDateTime.of(2024, 6, 1, 10, 0, 0),
                List.of(new ItemForRequestDto(10L, "Drill", 2L)));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-06-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
    }

    @Test
    void deserialize_ParsesDescriptionAndCreated() throws Exception {
        String content = "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"2024-06-01T10:00:00\",\"items\":[]}";

        ItemRequestDto dto = json.parse(content).getObject();

        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 6, 1, 10, 0, 0));
    }
}
