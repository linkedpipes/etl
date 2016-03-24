package com.linkedpipes.executor.monitor.web.servlet;

import com.linkedpipes.commons.entities.executor.CreateExecution;
import java.io.File;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.linkedpipes.commons.entities.executor.MessageSelectList;
import com.linkedpipes.commons.entities.executor.monitor.ExecutionBasic;
import com.linkedpipes.commons.entities.executor.monitor.ExecutionBasicEntity;
import com.linkedpipes.commons.entities.executor.monitor.ExecutionBasicList;
import com.linkedpipes.commons.entities.rest.BaseResponse;
import com.linkedpipes.commons.entities.rest.RestException;
import com.linkedpipes.executor.monitor.execution.boundary.ExecutionFacade;
import com.linkedpipes.executor.monitor.execution.entity.InitializeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Škoda Petr
 */
@RestController
@RequestMapping(value = "/executions")
public class ExecutorMonitorServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorMonitorServlet.class);

    @Autowired
    private ExecutionFacade executionFacade;

    @RequestMapping(value = "/{id}/messages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MessageSelectList getExecutionMessage(@PathVariable String id, HttpServletResponse response) {
        return executionFacade.selectQueryMessages(id);
    }

    @RequestMapping(value = "/{id}/logs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FileSystemResource getExecutionLogs(@PathVariable String id, HttpServletResponse response) {
        final File file = executionFacade.getExecutionLogsFile(id);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        return new FileSystemResource(file);
    }

    @RequestMapping(value = "/{id}/debug", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FileSystemResource getExecutionDebug(@PathVariable String id, HttpServletResponse response) {
        final File file = executionFacade.getExecutionDebugFile(id);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        return new FileSystemResource(file);
    }

    @RequestMapping(value = "/{id}/labels", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FileSystemResource getExecutionLabels(@PathVariable String id, HttpServletResponse response) {
        final File file = executionFacade.getExecutionLabelFile(id);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        return new FileSystemResource(file);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ExecutionBasicEntity getExecution(@PathVariable String id,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String language,
            HttpServletResponse response) {
        final ExecutionBasic execution = executionFacade.getExecutionBasic(id, language);
        if (execution == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        } else {
            final ExecutionBasicEntity output = new ExecutionBasicEntity();
            output.setPayload(execution);
            return output;
        }
    }

    /**
     * Warning: can be used to delete running execution.
     *
     * @param id
     * @param response
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void deleteExecution(@PathVariable String id, HttpServletResponse response) {
        executionFacade.deleteExecution(id);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ExecutionBasicList getDefinitionsSimple(
            @RequestParam(value = "limit", required = false, defaultValue = "1000") int limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String language,
            @RequestParam(value = "changedSince", required = false) Long changedSince) {

        //
        // TODO There is a problem with changedSince as this value is taken from file on disk, it
        // may not be accisible at the "right time" we should use our current time.
        //
        // ...
        final Date date = changedSince == null ? null : new Date(changedSince);
        final ExecutionBasicList result;
        if (date == null) {
            result = executionFacade.getExecutionsBasic(offset, limit, language);
        } else {
            result = executionFacade.getExecutionsBasic(offset, limit, language, date);
        }
        return result;
    }

    @RequestMapping(value = "", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public CreateExecution acceptMultipart(@RequestParam("file") MultipartFile multipartDefinition,
            @RequestParam(value = "extension") String extension, HttpServletResponse response)
            throws IOException, InitializeException {
        return executionFacade.createExecution(multipartDefinition.getInputStream(), extension);
    }

    @RequestMapping(value = "", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateExecution acceptJsonLd(@RequestBody String body, HttpServletResponse response)
            throws IOException, InitializeException {
        final InputStream inputStream = new ByteArrayInputStream(body.getBytes("UTF-8"));
        return executionFacade.createExecution(inputStream, "jsonld");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BaseResponse handleException(Exception exception) {
        // TODO Provide more precise message here!
        LOG.error("Exception handler.", exception);
        return new BaseResponse(new RestException(
                exception.getMessage(), "", "Internal error!", RestException.Codes.ERROR)
        );
    }

}
