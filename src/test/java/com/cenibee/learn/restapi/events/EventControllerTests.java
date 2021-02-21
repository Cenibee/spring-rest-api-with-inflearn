package com.cenibee.learn.restapi.events;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@WebMvcTest
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createEvent() throws Exception {
//        mockMvc.perform(post("/api/events")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .accept(MediaTypes.HAL_JSON))
//                .andExpect(status().isCreated());
    }

}
