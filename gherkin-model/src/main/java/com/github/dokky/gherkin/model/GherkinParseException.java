package com.github.dokky.gherkin.model;

import lombok.Getter;

public class GherkinParseException extends RuntimeException {
    @Getter
    private int lineNumber = 1;

    public GherkinParseException(String message) {
        super(message);
    }

    public GherkinParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public GherkinParseException(String message, int lineNumber, Throwable cause) {
        super("Parsing error at line " + lineNumber + ": " + message, cause);
        this.lineNumber = lineNumber;
    }

}
