package com.pedrocotta.goldenraspberry.movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public Page<MovieResponse> findAll(MovieFilter filter, Pageable pageable) {
        return movieRepository.findAll(MovieSpecifications.withFilter(filter), pageable).map(MovieResponse::from);
    }

    @Transactional(readOnly = true)
    public MovieResponse findById(Long id) {
        return movieRepository.findById(id)
                .map(MovieResponse::from)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }
}
