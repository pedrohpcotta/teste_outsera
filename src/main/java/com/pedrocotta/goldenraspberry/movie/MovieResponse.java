package com.pedrocotta.goldenraspberry.movie;

import com.pedrocotta.goldenraspberry.producer.Producer;
import com.pedrocotta.goldenraspberry.studio.Studio;

import java.util.List;

public record MovieResponse(Long id, int year, String title, List<String> studios, List<String> producers, boolean winner) {

    static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getYear(),
                movie.getTitle(),
                movie.getStudios().stream().map(Studio::getName).toList(),
                movie.getProducers().stream().map(Producer::getName).toList(),
                movie.isWinner());
    }
}
