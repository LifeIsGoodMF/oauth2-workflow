package com.test.protocol.general;

import java.util.List;
import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {

    private Map<String, List<String>> errors;

    public ValidationErrorResponse(int code, Map<String, List<String>> errors, String url) {
        super(code, "Validation error", url);
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
