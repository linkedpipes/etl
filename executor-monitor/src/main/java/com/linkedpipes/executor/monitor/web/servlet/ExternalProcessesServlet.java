package com.linkedpipes.executor.monitor.web.servlet;

import com.linkedpipes.commons.entities.executor.monitor.ExternalProcessBasicList;
import com.linkedpipes.commons.entities.executor.monitor.ExternalProcessEntity;
import com.linkedpipes.executor.monitor.process.boundary.ExternalProcessFacade;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Petr Škoda
 */
@RestController
@RequestMapping(value = "/processes")
public class ExternalProcessesServlet {

    @Autowired
    private ExternalProcessFacade processFacade;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ExternalProcessBasicList getList() {
        return processFacade.getProcesses();
    }

    @RequestMapping(value = "/fuseki/{executionId}/{dataUnitId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ExternalProcessEntity getProcess(@PathVariable String executionId, @PathVariable String dataUnitId) {
        return processFacade.startFuseki(executionId, dataUnitId);
    }

    @RequestMapping(value = "/{pid}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteExecution(@PathVariable String pid, HttpServletResponse response) {
        processFacade.terminate(pid);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
