package com.linkedpipes.etl.storage.web.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestController
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG =
            LoggerFactory.getLogger(RestExceptionHandler.class);

    private static class ErrorResponse {

        private static final String SOURCE = "STORAGE";

        private HttpStatus status;

        private String message;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String cause;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String stackTrace;

        public ErrorResponse(
                HttpStatus status, String message,
                String cause, String stackTrace) {
            this.status = status;
            this.message = message;
            this.cause = cause;
            this.stackTrace = stackTrace;
        }

        public String getSOURCE() {
            return SOURCE;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getCause() {
            return cause;
        }

        public String getStackTrace() {
            return stackTrace;
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
        LOG.error("Missing entry.", ex);
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(),
                null, null);
        return buildResponseEntity(response);
    }

    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<String> handleThrowable(Throwable ex)
            throws JsonProcessingException {
        LOG.error("Handling error.", ex);
        Throwable cause = getRootCause(ex);
        ErrorResponse response;
        if (cause == ex) {
            response = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage(),
                    null,
                    getStackTraceAsString(ex));
        } else {
            response = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage(),
                    cause.getLocalizedMessage(),
                    getStackTraceAsString(cause));
        }
        return buildResponseEntity(response);
    }

    private Throwable getRootCause(Throwable ex) {
        while (ex.getCause() != null) {
            ex = ex.getCause();
        }
        return ex;
    }

    private String getStackTraceAsString(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        return stringWriter.toString();
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
