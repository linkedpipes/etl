package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.linkedpipes.etl.executor.monitor.debug.http.DebugEntry;
import com.linkedpipes.etl.executor.monitor.debug.http.FileContentEntry;
import com.linkedpipes.etl.executor.monitor.debug.http.HttpDebugFilesFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping(value = "/debug")
public class DebugServlet {

    private final HttpDebugFilesFacade debugFacade;

    @Autowired
    public DebugServlet(HttpDebugFilesFacade debugFacade) {
        this.debugFacade = debugFacade;
    }

    @ResponseBody
    @RequestMapping(value = "/metadata/**", method = RequestMethod.GET)
    public void getMetadata(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "offset", required = false) Long offset,
            @RequestParam(value = "limit", required = false) Long limit,
            HttpServletRequest request,
            HttpServletResponse response)
            throws MissingResource, IOException {
        String iri = request.getPathInfo().substring(
                "/debug/metadata/".length());
        Optional<DebugEntry> dataHolder = debugFacade.resolve(iri);
        if (!dataHolder.isPresent()) {
            throw new MissingResource("Missing debug entry: {}", iri);
        }
        if (offset == null) {
            offset = 0L;
        }
        if (limit == null) {
            limit = Long.MAX_VALUE;
        }
        offset = Math.max(0, offset);
        limit = Math.max(1, limit);
        //
        DebugEntry data = dataHolder.get().
                prepareData(name, source, offset, limit);
        response.setHeader("Content-Type", "application/json");
        response.setHeader("Content-Length", Integer.toString(data.getSize()));
        response.setStatus(HttpServletResponse.SC_OK);
        data.write(response.getOutputStream());
        response.getOutputStream().flush();
    }

    @ResponseBody
    @RequestMapping(value = "/data/**", method = RequestMethod.GET)
    public void getData(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "source", required = false) String source,
            HttpServletRequest request,
            HttpServletResponse response)
            throws MissingResource, IOException {
        String iri = request.getPathInfo().substring("/debug/data/".length());
        Optional<DebugEntry> dataHolder = debugFacade.resolve(iri);
        if (!dataHolder.isPresent()) {
            throw new MissingResource("Missing debug entry: {}", iri);
        }
        //
        DebugEntry data = dataHolder.get().prepareData(name, source, 0, 1);
        if (!(data instanceof FileContentEntry)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        FileContentEntry fileData = (FileContentEntry) data;
        response.setHeader("Content-Type", fileData.getFileMimeType());
        response.setHeader("Content-Length", fileData.getFileSize().toString());
        response.setStatus(HttpServletResponse.SC_OK);
        fileData.writeFileContent(response.getOutputStream());
        response.getOutputStream().flush();
    }
}
