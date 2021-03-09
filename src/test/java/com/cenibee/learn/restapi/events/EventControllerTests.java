package com.cenibee.learn.restapi.events;

import com.cenibee.learn.restapi.common.AppProperties;
import com.cenibee.learn.restapi.common.BaseControllerTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// TODO 인가/비인가 에 따라 link 반환 테스트 추가

@SpringBootTest
public class EventControllerTests extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AppProperties appProperties;

    @Test
    @DisplayName("정상적으로 이벤트를 생성하는 테스트")
    void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 2, 22, 21, 11))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 2, 23, 21, 11))
                .beginEventDateTime(LocalDateTime.of(2021, 2, 23, 21, 11))
                .endEventDateTime(LocalDateTime.of(2021, 2, 23, 21, 11))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(Matchers.not(EventStatus.PUBLISHED)))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("data time of begin of new event enroll"),
                                fieldWithPath("closeEnrollmentDateTime").description("data time of close of new event enroll"),
                                fieldWithPath("beginEventDateTime").description("data time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("data time of close of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment of new event")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("data time of begin of new event enroll"),
                                fieldWithPath("closeEnrollmentDateTime").description("data time of close of new event enroll"),
                                fieldWithPath("beginEventDateTime").description("data time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("data time of close of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment of new event"),
                                fieldWithPath("free").description("it tells if this event is free or not"),
                                fieldWithPath("offline").description("it tells if this event is offline or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("manager.id").description("manager's id"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.update-event.href").description("link to update event"),
                                fieldWithPath("_links.query-events.href").description("link to query event")
                        )
                ));
    }

    private String getBearerToken() throws Exception {
        return "Bearer" + getAccessToken();
    }

    private String getAccessToken() throws Exception {
        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());

        String responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    @Test
    @DisplayName("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    void create_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("_embedded.errorDtoList").exists())
                .andExpect(jsonPath("_links.index.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists());
    }

    @Test
    @DisplayName("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    void create_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 2, 25, 21, 11))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 2, 23, 21, 11))
                .beginEventDateTime(LocalDateTime.of(2021, 2, 25, 21, 11))
                .endEventDateTime(LocalDateTime.of(2021, 2, 23, 21, 11))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 스타텁 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("_embedded.errorDtoList[0].objectName").exists())
                .andExpect(jsonPath("_embedded.errorDtoList[0].defaultMessage").exists())
                .andExpect(jsonPath("_embedded.errorDtoList[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
                .andDo(document("errors",
                        responseFields(
                                subsectionWithPath("_embedded.errorDtoList[]").description("a list of errors"),
                                fieldWithPath("_links.index.href").description("a link to index"),
                                fieldWithPath("_links.profile.href").description("a link to errors profile")
                        ),
                        responseFields(
                                beneathPath("_embedded.errorDtoList"),
                                fieldWithPath("objectName").description("a name of invalid object"),
                                fieldWithPath("code").description("a error code"),
                                fieldWithPath("defaultMessage").description("default error message"),
                                fieldWithPath("field").description("rejected field's name"),
                                fieldWithPath("rejectedValue").description("rejected value")
                        )
                ));
    }

    @Test
    @DisplayName("30 개의 이벤트를 10개씩 두번째 페이지 조회하기증 - 비인증")
    void queryEvents() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generateValidEvent);

        // When
        this.mockMvc.perform(get("/api/events")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "name,DESC")
                )
                // Then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self.href").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.create-event.href").doesNotExist())
        ;
    }

    @Test
    @DisplayName("30 개의 이벤트를 10개씩 두번째 페이지 조회하기 - 인증")
    void queryEventsWithAuthentication() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generateValidEvent);

        // When
        this.mockMvc.perform(get("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .param("page", "1")
                .param("size", "2")
                .param("sort", "name,DESC")
        )
                // Then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self.href").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.create-event.href").exists())
                .andDo(document("get-events",
                        links(
                                linkWithRel("self").description("a self link"),
                                linkWithRel("profile").description("a link to the next page"),
                                linkWithRel("first").description("a link to the first page"),
                                linkWithRel("prev").description("a link to the prev page"),
                                linkWithRel("next").description("a link to the next page"),
                                linkWithRel("last").description("a link to the last page"),
                                linkWithRel("create-event").description("a link for create new event")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.eventList[]").description("a list of events"),
                                subsectionWithPath("_links").description("relational links"),
                                subsectionWithPath("page").description("paged info")
                        )
                ));
    }

    @Test
    @DisplayName("기존의 이벤트를 하나 조회하기")
    void getEvent() throws Exception {
        // Given
        Event event = this.generateValidEvent(100);

        // When
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                // TODO 문서화 구현
                .andDo(document("get-an-event"))
        ;
    }

    @Test
    @DisplayName("없는 이벤트는 조회했을 때 404 응답받기")
    void getEvent404() throws Exception {
        // When & Then
        this.mockMvc.perform(get("/api/events{id}", 100))
                .andExpect(status().isNotFound())
        ;
    }

    private Event generateValidEvent(int i) {
        Event event = Event.builder()
                .name("Spring")
                .description("REST API Development")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 2, 22, 21, 11))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 2, 23, 21, 11))
                .beginEventDateTime(LocalDateTime.of(2021, 2, 23, 21, 11))
                .endEventDateTime(LocalDateTime.of(2021, 2, 23, 21, 11))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 스타텁 팩토리")
                .build();

        return this.eventRepository.save(event);
    }

    @Test
    @DisplayName("이벤트를 정상적으로 수정하기")
    void updateEvent() throws Exception {
        // Given
        Event event = this.generateValidEvent(100);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        String eventName = "updateEvent";
        eventDto.setName(eventName);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .content(objectMapper.writeValueAsString(eventDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                // TODO 문서화 구현
                .andDo(document("update-an-event"))
        ;
    }
    
    @Test
    @DisplayName("입력값이 비어있는 경우에 이벤트 수정 실패")
    void updateEvent400empty() throws Exception {
        // Given
        Event event = this.generateValidEvent(100);
        EventDto eventDto = new EventDto();

        String eventName = "updateEvent";
        eventDto.setName(eventName);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .content(objectMapper.writeValueAsString(eventDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("_links.index.href").exists())
        ;
    }

    @Test
    @DisplayName("입력값이 비어있는 경우에 이벤트 수정 실패")
    void updateEvent400() throws Exception {
        // Given
        Event event = this.generateValidEvent(100);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        eventDto.setName("");

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .content(objectMapper.writeValueAsString(eventDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("_links.index.href").exists())
        ;
    }
    
    @Test
    @DisplayName("없는 이벤트를 업데이트했을 때 404 응답 받기")
    void updateEvent404() throws Exception {
        Event event = generateValidEvent(3);
        EventDto eventDto = modelMapper.map(event, EventDto.class);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", 100)
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .content(objectMapper.writeValueAsString(eventDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

}
