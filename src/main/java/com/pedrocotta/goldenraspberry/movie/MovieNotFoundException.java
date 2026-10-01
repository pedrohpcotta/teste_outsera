package com.pedrocotta.goldenraspberry.movie;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class MovieNotFoundException extends ErrorResponseException {

    public MovieNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Movie %d not found".formatted(id)), null);
    }
}
