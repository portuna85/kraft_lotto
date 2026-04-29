package com.lotto.controller;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "잘못된 요청", e.getMessage(), "lotto/bad-request");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
        return problem(HttpStatus.BAD_REQUEST, "유효성 검사 실패", e.getMessage(), "lotto/validation");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException e) {
        return problem(HttpStatus.CONFLICT, "처리 불가", e.getMessage(), "lotto/conflict");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception e) {
        log.error("처리되지 않은 예외", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류",
                "예기치 못한 오류가 발생했습니다.", "lotto/internal-error");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, String type) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("urn:problem-type:" + type));
        return pd;
    }
}

