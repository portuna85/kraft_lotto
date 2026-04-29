package com.lotto.controller;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "잘못된 요청", e.getMessage(), "lotto/bad-request");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
        // 메시지 예: "generateLotto.count: must be greater than or equal to 1"
        // → "count: must be greater than or equal to 1" 처럼 메서드 prefix 제거
        String detail = e.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    int dot = path.lastIndexOf('.');
                    String field = dot >= 0 ? path.substring(dot + 1) : path;
                    return field + ": " + v.getMessage();
                })
                .collect(Collectors.joining("; "));
        if (detail.isEmpty()) {
            detail = e.getMessage();
        }
        return problem(HttpStatus.BAD_REQUEST, "유효성 검사 실패", detail, "lotto/validation");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String required = e.getRequiredType() == null ? "?" : e.getRequiredType().getSimpleName();
        String detail = "파라미터 '" + e.getName() + "' 의 형식이 올바르지 않습니다. (요구 타입: " + required + ")";
        return problem(HttpStatus.BAD_REQUEST, "파라미터 형식 오류", detail, "lotto/type-mismatch");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException e) {
        return problem(HttpStatus.BAD_REQUEST, "필수 파라미터 누락",
                "필수 파라미터 '" + e.getParameterName() + "' 가 누락되었습니다.",
                "lotto/missing-parameter");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, "요청 본문 파싱 실패",
                "요청 본문을 읽을 수 없습니다.", "lotto/malformed-body");
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

