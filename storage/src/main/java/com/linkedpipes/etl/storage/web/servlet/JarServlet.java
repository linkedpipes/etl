package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.jar.JarComponent;
import com.linkedpipes.etl.storage.jar.JarFacade;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping(value = "/jars")
public class JarServlet {

    @Autowired
    private JarFacade jars;

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    @ResponseBody
    public void getJarFile(
            @RequestParam(name = "iri") String iri,
            HttpServletResponse response)
            throws IOException, MissingResource {
        JarComponent component = getComponent(iri);
        response.setStatus(HttpStatus.SC_OK);
        response.setHeader("Content-Type", "application/octet-stream");
        try (OutputStream stream = response.getOutputStream()) {
            FileUtils.copyFile(jars.getJarFile(component), stream);
        }
    }

    private JarComponent getComponent(String iri) throws MissingResource {
        JarComponent component = jars.getJarComponent(iri);
        if (component == null) {
            throw new MissingResource("Missing jar file: {}", iri);
        }
        return component;
    }

}
