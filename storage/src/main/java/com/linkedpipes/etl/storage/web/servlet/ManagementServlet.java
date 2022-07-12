package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.pipeline.PipelineFacade;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This API should not be exposed to public as it provides service and
 * management capabilities.
 */
@RestController
@RequestMapping(value = "/management")
public class ManagementServlet {

    @Autowired
    private TemplateFacade templateFacade;

    @Autowired
    private PipelineFacade pipelineFacade;

    @RequestMapping(value = "/reload", method = RequestMethod.POST)
    public void reloadAll() {
        templateFacade.reload();
        pipelineFacade.reload();
    }

}
