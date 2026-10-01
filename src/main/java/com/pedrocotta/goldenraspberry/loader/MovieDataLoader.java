package com.pedrocotta.goldenraspberry.loader;

import com.pedrocotta.goldenraspberry.movie.Movie;
import com.pedrocotta.goldenraspberry.movie.MovieRepository;
import com.pedrocotta.goldenraspberry.producer.Producer;
import com.pedrocotta.goldenraspberry.producer.ProducerRepository;
import com.pedrocotta.goldenraspberry.studio.Studio;
import com.pedrocotta.goldenraspberry.studio.StudioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Component
public class MovieDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MovieDataLoader.class);

    private final MoviesProperties properties;
    private final ResourceLoader resourceLoader;
    private final MovieCsvReader csvReader;
    private final MovieRepository movieRepository;
    private final ProducerRepository producerRepository;
    private final StudioRepository studioRepository;

    public MovieDataLoader(MoviesProperties properties,
                           ResourceLoader resourceLoader,
                           MovieCsvReader csvReader,
                           MovieRepository movieRepository,
                           ProducerRepository producerRepository,
                           StudioRepository studioRepository) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.csvReader = csvReader;
        this.movieRepository = movieRepository;
        this.producerRepository = producerRepository;
        this.studioRepository = studioRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<MovieCsvRecord> records = readRecords();

        Map<String, Producer> producers = new HashMap<>();
        Map<String, Studio> studios = new HashMap<>();
        List<Movie> movies = records.stream()
                .map(movie -> new Movie(
                        movie.year(),
                        movie.title(),
                        movie.winner(),
                        resolve(movie.studios(), studios, Studio::new),
                        resolve(movie.producers(), producers, Producer::new)))
                .toList();

        studioRepository.saveAll(studios.values());
        producerRepository.saveAll(producers.values());
        movieRepository.saveAll(movies);

        log.info("Loaded {} movies, {} producers and {} studios from {}",
                movies.size(), producers.size(), studios.size(), properties.csvPath());
    }

    private List<MovieCsvRecord> readRecords() {
        Resource resource = resourceLoader.getResource(properties.csvPath());
        if (!resource.exists()) {
            throw new MovieCsvParseException("Movies CSV not found at " + properties.csvPath());
        }
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return csvReader.read(reader);
        } catch (IOException e) {
            throw new MovieCsvParseException("Unable to open movies CSV at " + properties.csvPath(), e);
        }
    }

    private static <T> Set<T> resolve(List<String> names, Map<String, T> cache, Function<String, T> factory) {
        Set<T> resolved = new LinkedHashSet<>();
        names.forEach(name -> resolved.add(cache.computeIfAbsent(name, factory)));
        return resolved;
    }
}
