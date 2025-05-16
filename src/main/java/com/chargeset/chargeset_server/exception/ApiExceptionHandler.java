package com.chargeset.chargeset_server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult illegalArgumentExceptionHandle(IllegalArgumentException e) {
        log.error("[exceptionHandle] 400 에러", e);
        return new ErrorResult("BAD REQUEST", e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ErrorResult runtimeException(RuntimeException e) {
        log.error("[exceptionHandle] 500 에러", e);
        return new ErrorResult("알 수 없는 오류", e.getMessage());
    }


}
