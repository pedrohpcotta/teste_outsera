package com.pedrocotta.goldenraspberry.movie;

import org.springframework.data.jpa.domain.Specification;

final class MovieSpecifications {

    private MovieSpecifications() {
    }

    static Specification<Movie> withFilter(MovieFilter filter) {
        return Specification.allOf(
                filter.year() == null ? null : hasYear(filter.year()),
                filter.winner() == null ? null : isWinner(filter.winner()));
    }

    private static Specification<Movie> hasYear(int year) {
        return (root, query, cb) -> cb.equal(root.get("year"), year);
    }

    private static Specification<Movie> isWinner(boolean winner) {
        return (root, query, cb) -> cb.equal(root.get("winner"), winner);
    }
}
