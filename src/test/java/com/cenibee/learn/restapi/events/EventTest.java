package com.cenibee.learn.restapi.events;

import com.cenibee.learn.restapi.common.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EventTest extends BaseTest {
    @Test
    void builder() {
        Event event = Event.builder()
                .name("this is a name")
                .description("REST API test")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    void javaBean() {
        // Given
        String name = "Event";
        String description = "Spring";

        // When
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        // Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }

    @ParameterizedTest
    @MethodSource
    void testFree(int basePrice, int maxPrice, boolean isFree) {
        // Given:
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // When:
        event.update();

        // Then:
        assertThat(event.isFree()).isEqualTo(isFree);
    }

    static Stream<Arguments> testFree() {
        return Stream.of(
                arguments(0, 0, true),
                arguments(100, 0, false),
                arguments(0, 100, false),
                arguments(100, 200, false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOffline(String location, boolean isOffline) {
        // Given:
        Event event = Event.builder()
                .location(location)
                .build();

        // When:
        event.update();

        // Then:
        assertThat(event.isOffline()).isEqualTo(isOffline);
    }

    static Stream<Arguments> testOffline() {
        return Stream.of(
                arguments("강남", true),
                arguments("", false),
                arguments("   ", false),
                arguments(null, false)
        );
    }
}