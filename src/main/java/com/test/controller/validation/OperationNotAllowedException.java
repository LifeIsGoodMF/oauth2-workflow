package com.test.controller.validation;

public class OperationNotAllowedException extends RuntimeException {
    public OperationNotAllowedException(String msg) {
        super(msg);
    }
}
