package com.cenibee.learn.restapi.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.Errors;

import java.util.Collection;
import java.util.LinkedList;

@Data
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class ErrorDto {
    private String objectName;
    private String code;
    private String defaultMessage;

    private String field;
    private String rejectedValue;

    public static Collection<ErrorDto> collectionOf(Errors errors) {
        LinkedList<ErrorDto> list = new LinkedList<>();

        if (errors == null) {
            return list;
        }

        errors.getFieldErrors().forEach(fieldError ->
            list.add(ErrorDto.builder()
                    .field(fieldError.getField())
                    .objectName(fieldError.getObjectName())
                    .code(fieldError.getCode())
                    .defaultMessage(fieldError.getDefaultMessage())
                    .rejectedValue(fieldError.getRejectedValue() != null ?
                            fieldError.getRejectedValue().toString() :
                            null)
                    .build())
        );

        errors.getGlobalErrors().forEach(objectError ->
                list.add(ErrorDto.builder()
                        .objectName(objectError.getObjectName())
                        .code(objectError.getCode())
                        .defaultMessage(objectError.getDefaultMessage())
                        .build())
        );

        return list;
    }
}
