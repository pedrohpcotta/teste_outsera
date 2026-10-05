package com.pedrocotta.goldenraspberry;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProducerAwardIntervalsIntegrationTest {

    private static final String AWARD_INTERVALS = "/producers/award-intervals";

    @Autowired
    private MockMvc mockMvc;

    private static void assertAwardIntervals(MockMvc mockMvc, String expectedJson) throws Exception {
        mockMvc.perform(get(AWARD_INTERVALS))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    void returnsMinAndMaxIntervalsFromProvidedDataset() throws Exception {
        assertAwardIntervals(mockMvc, """
                {
                  "min": [
                    { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
                  ],
                  "max": [
                    { "producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015 }
                  ]
                }
                """);
    }

    @Nested
    @TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/ties.csv")
    class WhenSeveralProducersShareTheSameInterval {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void returnsEveryTiedProducer() throws Exception {
            assertAwardIntervals(mockMvc, """
                    {
                      "min": [
                        { "producer": "Producer A", "interval": 1, "previousWin": 2000, "followingWin": 2001 },
                        { "producer": "Producer B", "interval": 1, "previousWin": 2010, "followingWin": 2011 }
                      ],
                      "max": [
                        { "producer": "Producer C", "interval": 19, "previousWin": 1980, "followingWin": 1999 },
                        { "producer": "Producer D", "interval": 19, "previousWin": 2000, "followingWin": 2019 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/same-producer-min-and-max.csv")
    class WhenTheSameProducerHasTheMinAndMaxIntervals {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void considersOnlyConsecutiveWins() throws Exception {
            assertAwardIntervals(mockMvc, """
                    {
                      "min": [
                        { "producer": "Producer X", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
                      ],
                      "max": [
                        { "producer": "Producer X", "interval": 10, "previousWin": 1991, "followingWin": 2001 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/same-year-wins.csv")
    class WhenAProducerWinsTwiceInTheSameYear {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void countsAZeroYearInterval() throws Exception {
            assertAwardIntervals(mockMvc, """
                    {
                      "min": [
                        { "producer": "Producer Y", "interval": 0, "previousWin": 2000, "followingWin": 2000 }
                      ],
                      "max": [
                        { "producer": "Producer Z", "interval": 7, "previousWin": 2000, "followingWin": 2007 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/producer-separators.csv")
    class WhenAMovieHasMultipleProducers {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void splitsProducersByCommaAndAndIgnoringNonWinners() throws Exception {
            assertAwardIntervals(mockMvc, """
                    {
                      "min": [
                        { "producer": "Alice Anderson", "interval": 3, "previousWin": 1995, "followingWin": 1998 },
                        { "producer": "Bob Brand", "interval": 3, "previousWin": 1995, "followingWin": 1998 }
                      ],
                      "max": [
                        { "producer": "Carol Candy", "interval": 5, "previousWin": 1995, "followingWin": 2000 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/producer-name-case.csv")
    class WhenNamesAndSeparatorsVaryInCase {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void treatsTheProducerAsTheSameKeepingTheFirstSpelling() throws Exception {
            assertAwardIntervals(mockMvc, """
                    {
                      "min": [
                        { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
                      ],
                      "max": [
                        { "producer": "Anna Bell", "interval": 8, "previousWin": 1991, "followingWin": 1999 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/byte-order-mark.csv")
    class WhenTheFileStartsWithAByteOrderMark {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void readsTheHeaderAndCalculatesTheIntervals() throws Exception {
            assertAwardIntervals(mockMvc, """
                    {
                      "min": [
                        { "producer": "Producer B", "interval": 1, "previousWin": 2000, "followingWin": 2001 }
                      ],
                      "max": [
                        { "producer": "Producer C", "interval": 7, "previousWin": 2003, "followingWin": 2010 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/single-interval.csv")
    class WhenThereIsASingleInterval {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void returnsItAsBothMinAndMax() throws Exception {
            assertAwardIntervals(mockMvc, """
                    {
                      "min": [
                        { "producer": "Solo Producer", "interval": 4, "previousWin": 2010, "followingWin": 2014 }
                      ],
                      "max": [
                        { "producer": "Solo Producer", "interval": 4, "previousWin": 2010, "followingWin": 2014 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/no-intervals.csv")
    class WhenNoProducerHasTwoWins {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void returnsEmptyLists() throws Exception {
            assertAwardIntervals(mockMvc, """
                    { "min": [], "max": [] }
                    """);
        }
    }
}
