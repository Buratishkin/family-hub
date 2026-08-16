package org.buratishkin.familyhub.task.dto;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    public FlexibleLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        String value = parser.getString();
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        try {
            return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return OffsetDateTime.parse(normalized, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        } catch (DateTimeParseException ignored) {
            throw context.weirdStringException(
                    normalized,
                    LocalDateTime.class,
                    "Expected ISO date-time like 2026-07-27T19:00:00 or 2026-07-27T19:00:00.000Z"
            );
        }
    }
}
