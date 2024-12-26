package com.mss.codi.exception;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


import java.util.MissingFormatArgumentException;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({BindException.class})
    public ResponseEntity<ErrorResponse> handleBindingException(BindException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining("\n"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().message(msg).build());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleBindingException(ConstraintViolationException e, HttpServletRequest request) {
        String msg = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("\n"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().message(msg).build());
    }

    @ExceptionHandler({NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleResponseStatusException(NoResourceFoundException e) {
        return ResponseEntity.status(e.getStatusCode()).build();
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleResponseStatusException(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder().message("필수값인 [" + e.getParameterName() + "]항목이 없습니다.").build());
    }

    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(ErrorResponse.builder().message(e.getReason()).build());
    }

    @ExceptionHandler({Exception.class})
    @Priority(Integer.MAX_VALUE)
    public ResponseEntity<ErrorResponse> handleAbnormalException(Exception e) {
        log.error("unknown error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().message("처리중 오류가 발생했습니다.").build());
    }
}
