package com.linkedpipes.etl.storage.http.servlet;

import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

class ServletUtilities {

    @FunctionalInterface
    public interface Handler {

        void handle() throws ServerError, InvalidRequest;

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(ServletUtilities.class);

    public static int HTTP_INVALID_REQUEST = 400;

    public static int HTTP_NOT_FOUND = 404;

    public static int HTTP_SERVER_ERROR = 500;

    public static int HTTP_OK = 200;

    public static String CONTENT_ZIP = "application/x-zip-compressed";

    public static Statements read(MultipartFile multipartFile)
            throws InvalidRequest {
        if (multipartFile == null) {
            return Statements.empty();
        }
        RDFFormat format = Rio.getParserFormatForMIMEType(
                multipartFile.getContentType()).orElse(null);
        if (format == null) {
            String fileName = multipartFile.getOriginalFilename();
            if (fileName == null) {
                fileName = multipartFile.getName();
            }
            format = Rio.getParserFormatForFileName(fileName).orElse(null);
        }
        Statements result = Statements.arrayList();
        try (InputStream stream = multipartFile.getInputStream()) {
            result.file().addAll(stream, format);
        } catch (IOException ex) {
            throw new InvalidRequest(
                    "Can't read multipart file '{}'.",
                    multipartFile.getName(), ex);
        }
        return result;
    }

    public static void sendResponse(
            HttpServletRequest request, HttpServletResponse response,
            Statements statements) {
        RDFFormat format = getFormat(request, RDFFormat.TRIG);
        response.setHeader("content-type", format.getDefaultMIMEType());
        try (OutputStream stream = response.getOutputStream()) {
            statements.file().writeToStream(stream, format);
        } catch (IOException ex) {
            LOG.error("Can't write HTTP response.", ex);
            response.setStatus(ServletUtilities.HTTP_SERVER_ERROR);
            return;
        }
        response.setStatus(ServletUtilities.HTTP_OK);
    }

    public static RDFFormat getFormat(
            HttpServletRequest request, RDFFormat defaultValue) {
        // TODO Add support for text/html; charset=UTF-8 .
        // TODO Add support for content negotiation (text/*;q=0.5,*/*;q=0.1) .
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader == null) {
            return defaultValue;
        }
        String[] mimeTypes = acceptHeader.split(",");
        for (String mimeTypeString : mimeTypes) {
            Optional<RDFFormat> format =
                    Rio.getParserFormatForMIMEType(mimeTypeString);
            if (format.isPresent()) {
                return format.get();
            }
        }
        return defaultValue;
    }

    public static void wrap(
            HttpServletRequest request, HttpServletResponse response,
            Handler handler) {
        measure(request.getMethod(), request.getRequestURI(), () -> {
            try {
                handler.handle();
            } catch (InvalidRequest ex) {
                LOG.error("Invalid request '{}' '{}'.",
                        request.getMethod(),
                        request.getRequestURI(),
                        ex);
                response.setStatus(ServletUtilities.HTTP_INVALID_REQUEST);
            } catch (ServerError ex) {
                LOG.error("Server can't handle request '{}' '{}'.",
                        request.getMethod(), request.getRequestURI(), ex);
                response.setStatus(ServletUtilities.HTTP_SERVER_ERROR);
            }
        });
    }

    public static void measure(String method, String name, Runnable runnable) {
        LocalDateTime start = LocalDateTime.now();
        runnable.run();
        LOG.debug("[{}] '{}' took {} ms", method, name,
                Duration.between(start, LocalDateTime.now()).toMillis());
    }

}
