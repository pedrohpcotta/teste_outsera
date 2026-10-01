package com.pedrocotta.goldenraspberry.loader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class MovieCsvReader {

    private static final String YEAR = "year";
    private static final String TITLE = "title";
    private static final String STUDIOS = "studios";
    private static final String PRODUCERS = "producers";
    private static final String WINNER = "winner";
    private static final Set<String> REQUIRED_HEADERS = Set.of(YEAR, TITLE, STUDIOS, PRODUCERS, WINNER);

    private static final Pattern STUDIO_SEPARATOR = Pattern.compile(",");
    private static final Pattern PRODUCER_SEPARATOR = Pattern.compile(",|\\s+and\\s+");

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .get();

    public List<MovieCsvRecord> read(Reader reader) {
        try (CSVParser parser = FORMAT.parse(reader)) {
            validateHeaders(parser.getHeaderNames());
            List<MovieCsvRecord> movies = new ArrayList<>();
            for (CSVRecord record : parser) {
                movies.add(toMovie(record));
            }
            return movies;
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            throw new MovieCsvParseException("Unable to read movies CSV: " + e.getMessage(), e);
        }
    }

    private static void validateHeaders(List<String> headers) {
        Set<String> normalized = new LinkedHashSet<>();
        headers.forEach(header -> normalized.add(header.toLowerCase()));
        if (!normalized.containsAll(REQUIRED_HEADERS)) {
            throw new MovieCsvParseException("Movies CSV must contain the headers " + REQUIRED_HEADERS + " but found " + headers);
        }
    }

    private static MovieCsvRecord toMovie(CSVRecord record) {
        long line = record.getRecordNumber() + 1;
        if (!record.isConsistent()) {
            throw new MovieCsvParseException("Line %d has %d columns but the header defines %d"
                    .formatted(line, record.size(), record.getParser().getHeaderNames().size()));
        }
        String title = record.get(TITLE);
        if (title.isBlank()) {
            throw new MovieCsvParseException("Line %d has an empty title".formatted(line));
        }
        return new MovieCsvRecord(
                parseYear(record.get(YEAR), line),
                title,
                splitNames(record.get(STUDIOS), STUDIO_SEPARATOR),
                splitNames(record.get(PRODUCERS), PRODUCER_SEPARATOR),
                "yes".equalsIgnoreCase(record.get(WINNER)));
    }

    private static int parseYear(String value, long line) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new MovieCsvParseException("Line %d has an invalid year '%s'".formatted(line, value), e);
        }
    }

    private static List<String> splitNames(String value, Pattern separator) {
        return Arrays.stream(separator.split(value))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();
    }
}
