package com.pedrocotta.goldenraspberry.loader;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.movies")
public record MoviesProperties(@NotBlank String csvPath) {
}
