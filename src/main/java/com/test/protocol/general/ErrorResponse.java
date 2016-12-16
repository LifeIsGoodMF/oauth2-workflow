package com.test.protocol.general;

public class ErrorResponse extends Response {
    private final String path;

    public ErrorResponse(int code, String message, String path) {
        super(code, message);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
