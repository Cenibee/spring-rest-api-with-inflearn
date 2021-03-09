package com.cenibee.learn.restapi.events;

import com.cenibee.learn.restapi.accounts.Account;
import com.cenibee.learn.restapi.accounts.CurrentUser;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

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
    public ResponseEntity<?> createEvent(@RequestBody @Valid EventDto eventDto,
                                         Errors errors,
                                         @CurrentUser Account currentAccount) {
        if (errors.hasErrors()) {
            return invalidEventResponse(errors);
        }

        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return invalidEventResponse(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        event.setManager(currentAccount);
        Event newEvent = this.eventRepository.save(event);

        return ResponseEntity
                .created(linkTo(EventController.class).slash(newEvent.getId()).toUri())
                .body(eventModel(event));
    }

    @GetMapping
    public ResponseEntity<?> queryEvents(Pageable pageable,
                                         PagedResourcesAssembler<Event> assembler,
                                         @CurrentUser Account currentAccount) {
        return ResponseEntity.ok(assembler
                .toModel(this.eventRepository.findAll(pageable), this::eventModel)
                // TODO 문서화한 URL 은 어떻게 테스트해야할까?
                .add(Link.of("/docs/index.html#resources-events-list").withRel("profile"))
                .addIf(currentAccount != null, () -> linkTo(EventController.class).withRel("create-event"))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable Integer id) {
        Optional<Event> eventOptional = this.eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(eventModel(eventOptional.get())
                    // TODO 문서화한 URL 은 어떻게 테스트해야할까?
                    .add(Link.of("/docs/index.html#resources-events-get").withRel("profile"))
            );
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> patchEvent(@PathVariable Integer id,
                                        @RequestBody @Valid EventDto eventDto,
                                        Errors errors) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) {
            return invalidEventResponse(errors);
        }

        this.eventValidator.validate(eventDto, errors);

        if (errors.hasErrors()) {
            return invalidEventResponse(errors);
        }

        Event existingEvent = optionalEvent.get();
        this.modelMapper.map(eventDto, existingEvent);

        return ResponseEntity.ok(eventModel(this.eventRepository.save(existingEvent))
                // TODO 문서화한 URL 은 어떻게 테스트해야할까?
                .add(Link.of("/docs/index.html#resources-events-update").withRel("profile")));
    }

    private ResponseEntity<?> invalidEventResponse(Errors errors) {
        return ResponseEntity.badRequest().body(
                CollectionModel.of(ErrorDto.collectionOf(errors),
                        linkTo(methodOn(IndexController.class).index()).withRel("index"),
                        Link.of("/docs/index.html#overview-errors").withRel("profile")));
    }

    private EntityModel<? extends Event> eventModel(Event event) {
        return EntityModel.of(event,
                linkTo(EventController.class).slash(event.getId()).withSelfRel(),
                linkTo(EventController.class).slash(event.getId()).withRel("update-event"),
                linkTo(EventController.class).withRel("query-events"));
    }

}
