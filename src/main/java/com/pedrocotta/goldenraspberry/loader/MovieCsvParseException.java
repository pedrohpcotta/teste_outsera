package com.pedrocotta.goldenraspberry.loader;

public class MovieCsvParseException extends RuntimeException {

    public MovieCsvParseException(String message) {
        super(message);
    }

    public MovieCsvParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
