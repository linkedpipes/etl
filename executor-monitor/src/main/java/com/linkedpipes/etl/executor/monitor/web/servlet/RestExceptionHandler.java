package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestController
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static class ErrorResponse {

        private static final String SOURCE = "EXECUTOR-MONITOR";

        private HttpStatus status;

        private String message;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String cause;

        public ErrorResponse(HttpStatus status, String message, String cause) {
            this.status = status;
            this.message = message;
            this.cause = cause;
        }

        public String getSource() {
            return SOURCE;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCause() {
            return cause;
        }

        public void setCause(String cause) {
            this.cause = cause;
        }

    }

    private static class RestExceptionEnvelop {

        private ErrorResponse error;

        public RestExceptionEnvelop(ErrorResponse error) {
            this.error = error;
        }

        public ErrorResponse getError() {
            return error;
        }
    }

    @ExceptionHandler(MissingResource.class)
    protected ResponseEntity<String> handleMissingResource(MissingResource ex)
            throws JsonProcessingException {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(),
                null);
        return buildResponseEntity(response);
    }

    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<String> handleThrowable(Throwable ex)
            throws JsonProcessingException {
        Throwable cause = getRootCause(ex);
        ErrorResponse response;
        if (cause == ex) {
            response = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage(),
                    null);
        } else {
            response = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage(),
                    cause.getLocalizedMessage());
        }
        return buildResponseEntity(response);
    }

    private Throwable getRootCause(Throwable ex) {
        while (ex.getCause() != null) {
            ex = ex.getCause();
        }
        return ex;
    }

    private ResponseEntity<String> buildResponseEntity(
            ErrorResponse error) throws JsonProcessingException {
        // This is to force JSON response for missing Accept header.
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(
                new RestExceptionEnvelop(error));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");

        return new ResponseEntity<>(content, headers, error.getStatus());
    }

}
