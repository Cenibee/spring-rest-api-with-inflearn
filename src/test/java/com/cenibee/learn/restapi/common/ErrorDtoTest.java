package com.cenibee.learn.restapi.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
class ErrorDtoTest extends BaseTest {

    @Test
    void mapperAutowiredTest() {
        assertThat(objectMapper).isNotNull();
    }

    @ParameterizedTest
    @MethodSource
    void serializeTest(String field, String name, String code, String message, String rejectedValue) throws JsonProcessingException {
        ErrorDto error = ErrorDto.builder()
                .field(field)
                .objectName(name)
                .code(code)
                .defaultMessage(message)
                .rejectedValue(rejectedValue)
                .build();

        String json = objectMapper.writeValueAsString(error);

        assertThat(error).isEqualTo(objectMapper.readValue(json, ErrorDto.class));
    }

    static Stream<Arguments> serializeTest() {
        return Stream.of(
                arguments("filed", "name", "code", "message", "value"),
                arguments("filed", "name", "code", "message", null)
        );
    }
}