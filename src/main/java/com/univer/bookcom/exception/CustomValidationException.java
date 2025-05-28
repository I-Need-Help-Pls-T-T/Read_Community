package com.univer.bookcom.exception;

import java.util.Map;

public class CustomValidationException extends RuntimeException {
    private Map<String, String> errors;

    public CustomValidationException(Map<String, String> errors) {
        super("Constraint violation");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
