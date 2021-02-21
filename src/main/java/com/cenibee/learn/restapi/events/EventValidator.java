package com.cenibee.learn.restapi.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class EventValidator {

    public void validate(EventDto eventDto, Errors errors) {
        if (eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() != 0) {
            errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
            errors.rejectValue("maxPrice", "wrongValue", "BasePrice is wrong");
        }

        if (eventDto.getBeginEnrollmentDateTime()
                .isAfter(eventDto.getCloseEnrollmentDateTime())) {
            errors.rejectValue("beginEnrollmentDateTime", "wrongValue", "BeginEnrollmentDateTime is wrong");
        }
        if (eventDto.getCloseEnrollmentDateTime()
                .isAfter(eventDto.getBeginEventDateTime())) {
            errors.rejectValue("closeEnrollmentDateTime", "wrongValue", "CloseEnrollmentDateTime is wrong");
        }
        if (eventDto.getBeginEventDateTime()
                .isAfter(eventDto.getEndEventDateTime())) {
            errors.rejectValue("beginEventDateTime", "wrongValue", "BeginEventDateTime is wrong");
        }
    }

}
