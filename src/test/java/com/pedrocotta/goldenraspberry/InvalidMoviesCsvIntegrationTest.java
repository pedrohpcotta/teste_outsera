package com.pedrocotta.goldenraspberry;

import com.pedrocotta.goldenraspberry.loader.MovieCsvParseException;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvalidMoviesCsvIntegrationTest {

    private static AbstractThrowableAssert<?, ? extends Throwable> assertStartupFails(String csvPath) {
        return assertThatThrownBy(() -> new SpringApplicationBuilder(GoldenRaspberryAwardsApplication.class)
                .web(WebApplicationType.NONE)
                .run("--app.movies.csv-path=" + csvPath))
                .isInstanceOf(MovieCsvParseException.class);
    }

    private static void assertStartupFailsWith(String csvPath, String expectedMessage) {
        assertStartupFails(csvPath).hasMessage(expectedMessage);
    }

    @Test
    void failsWhenTheFileDoesNotExist() {
        assertStartupFailsWith("classpath:datasets/invalid/does-not-exist.csv",
                "Movies CSV not found at classpath:datasets/invalid/does-not-exist.csv");
    }

    @Test
    void failsWhenARequiredHeaderIsMissing() {
        assertStartupFailsWith("classpath:datasets/invalid/missing-header.csv",
                "Movies CSV is missing the headers [winner]");
    }

    @Test
    void failsWhenTheYearIsNotNumeric() {
        assertStartupFailsWith("classpath:datasets/invalid/invalid-year.csv",
                "Line 3 has an invalid year '19x1'");
    }

    @Test
    void failsWhenTheTitleIsEmpty() {
        assertStartupFailsWith("classpath:datasets/invalid/empty-title.csv",
                "Line 2 has an empty title");
    }

    @Test
    void failsWhenALineHasADifferentNumberOfColumns() {
        assertStartupFailsWith("classpath:datasets/invalid/inconsistent-columns.csv",
                "Line 4 has 4 columns but the header defines 5");
    }

    @Test
    void failsWhenAQuotedValueIsNotClosed() {
        assertStartupFails("classpath:datasets/invalid/unclosed-quote.csv")
                .hasMessageStartingWith("Unable to read movies CSV")
                .hasMessageContaining("(startline 2)");
    }
}
