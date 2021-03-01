package com.cenibee.learn.restapi.events;

import com.cenibee.learn.restapi.common.ErrorDto;
import com.cenibee.learn.restapi.index.Index.IndexController;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;

    private final EventValidator eventValidator;

    @Autowired
    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
        if (errors.hasErrors()) {
            return errorResponseEntity(errors);
        }

        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return errorResponseEntity(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        Event newEvent = this.eventRepository.save(event);

        return ResponseEntity
                .created(linkTo(EventController.class).slash(newEvent.getId()).toUri())
                .body(eventModel(event));
    }

    @GetMapping
    public ResponseEntity<?> queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        return ResponseEntity.ok(assembler
                .toModel(this.eventRepository.findAll(pageable), this::eventModel)
                // TODO 문서화한 URL 은 어떻게 테스트해야할까?
                .add(Link.of("/docs/index.html#resources-events-list").withRel("profile"))
        );
    }

    private ResponseEntity<?> errorResponseEntity(Errors errors) {
        return ResponseEntity.badRequest().body(
                CollectionModel.of(ErrorDto.collectionOf(errors),
                        linkTo(methodOn(IndexController.class).index()).withRel("index")));
    }

    private EntityModel<? extends Event> eventModel(Event event) {
        return EntityModel.of(event,
                linkTo(EventController.class).slash(event.getId()).withSelfRel(),
                linkTo(EventController.class).slash(event.getId()).withRel("update-event"),
                linkTo(EventController.class).withRel("query-events"));
    }

}
