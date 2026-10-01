package com.pedrocotta.goldenraspberry.movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public Page<MovieResponse> findAll(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Boolean winner,
            @PageableDefault(size = 20, sort = {"year", "id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return movieService.findAll(new MovieFilter(year, winner), pageable);
    }

    @GetMapping("/{id}")
    public MovieResponse findById(@PathVariable Long id) {
        return movieService.findById(id);
    }
}
