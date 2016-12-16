package com.test.protocol.general;

import org.springframework.http.HttpStatus;

public class Response {
    public static final Response OK = new Response();

    private int code;
    private String message;

    public Response(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Response() {
        code = HttpStatus.OK.value();
        message = HttpStatus.OK.getReasonPhrase();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    protected void setCode(int code) {
        this.code = code;
    }
}
