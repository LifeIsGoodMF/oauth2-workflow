package com.test.controller;

import com.test.controller.validation.OperationNotAllowedException;
import com.test.controller.validation.UserExistException;
import com.test.controller.validation.ValidationException;
import com.test.protocol.general.ErrorResponse;
import com.test.protocol.general.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlingControllersAdvice {

    private final Logger log = LoggerFactory.getLogger(ExceptionHandlingControllersAdvice.class);

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(HttpServletRequest req, AccessDeniedException ex) {
        log.warn("Access denied conflict on " + formatRequest(req));

        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(UserExistException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserExistException(HttpServletRequest req, UserExistException ex) {
        final String message = ex.getMessage();
        log.warn("UserExistException: {}. {}", message, formatRequest(req));
        return new ErrorResponse(HttpStatus.CONFLICT.value(), message == null ? "Email has already taken" : message, req.getRequestURI());
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOperationNotAllowedException(HttpServletRequest req, OperationNotAllowedException ex) {
        final String message = ex.getMessage();
        log.warn("OperationNotAllowedException: {}. {}", message, formatRequest(req));
        return new ErrorResponse(HttpStatus.CONFLICT.value(), message == null ? "Operation not allowed" : message, req.getRequestURI());
    }

     @ExceptionHandler(ValidationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(HttpServletRequest req, ValidationException ex) {
        log.warn("ValidationException: {}. {}",  ex.getMessage(), formatRequest(req));
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(HttpServletRequest req, UsernameNotFoundException ex) {
        log.warn("Unauthorized " + formatRequest(req));

        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Bad credentials", req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationException(HttpServletRequest req, MethodArgumentNotValidException ex) {
        BindingResult bindResult = ex.getBindingResult();

        Map<String, List<String>> errors = new HashMap<>();

        for (FieldError e : bindResult.getFieldErrors()) {
            List<String> fieldErrors = errors.computeIfAbsent(e.getField(), k -> new ArrayList<>(2));
            fieldErrors.add(e.getDefaultMessage());
        }

        for (ObjectError e : bindResult.getGlobalErrors()) {
            List<String> objErrors = errors.computeIfAbsent(e.getObjectName(), k -> new ArrayList<>(2));
            objErrors.add(e.getDefaultMessage());
        }

        log.info("Validation conflicts on " + formatRequest(req), ex);

        return new ValidationErrorResponse(HttpStatus.BAD_REQUEST.value(), errors, req.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleMessageNotReadableException(HttpServletRequest req, HttpMessageNotReadableException ex) {
        log.warn("Empty body exception " + formatRequest(req), ex);
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Request body is missing or invalid: " + ex.getLocalizedMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownException(HttpServletRequest req, Exception ex) {
        log.warn("Exception occurred on " + formatRequest(req), ex);
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), req.getRequestURI());
    }



    private String formatRequest(HttpServletRequest req) {
        return req.getMethod() + ' ' + req.getRequestURI();
    }
}
