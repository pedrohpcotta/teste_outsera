package com.pedrocotta.goldenraspberry;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovieIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loadsEveryMovieFromTheProvidedDataset() throws Exception {
        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(206))
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.content[0].year").value(1980))
                .andExpect(jsonPath("$.content[0].title").value("Can't Stop the Music"))
                .andExpect(jsonPath("$.content[0].producers[0]").value("Allan Carr"))
                .andExpect(jsonPath("$.content[0].winner").value(true));
    }

    @Test
    void filtersWinnersByYear() throws Exception {
        mockMvc.perform(get("/movies").param("year", "1990").param("winner", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].year", everyItem(is(1990))))
                .andExpect(jsonPath("$.content[*].winner", everyItem(is(true))));
    }

    @Test
    void returnsTheMovieWithItsStudiosAndSplitProducers() throws Exception {
        String body = mockMvc.perform(get("/movies").param("year", "1983"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(body, "$.content[?(@.title == 'Hercules')].id");

        mockMvc.perform(get("/movies/{id}", ids.getFirst().longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hercules"))
                .andExpect(jsonPath("$.studios", hasSize(3)))
                .andExpect(jsonPath("$.producers", hasSize(2)))
                .andExpect(jsonPath("$.producers[0]").value("Menahem Golan"))
                .andExpect(jsonPath("$.producers[1]").value("Yoram Globus"));
    }

    @Test
    void returnsNotFoundForUnknownMovie() throws Exception {
        mockMvc.perform(get("/movies/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void returnsBadRequestForInvalidYear() throws Exception {
        mockMvc.perform(get("/movies").param("year", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsBadRequestForUnknownSortProperty() throws Exception {
        mockMvc.perform(get("/movies").param("sort", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
