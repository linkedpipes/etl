package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.component.jar.JarComponent;
import com.linkedpipes.etl.storage.component.jar.JarFacade;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Petr Škoda
 */
@RestController
@RequestMapping(value = "/jars")
public class JarServlet {

    @Autowired
    private JarFacade jars;

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    @ResponseBody
    public void getJarFile(@RequestParam(name = "iri") String iri,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Get component.
        final JarComponent component = jars.getJarComponent(iri);
        if (component == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Stream the JAR file.
        try (OutputStream stream = response.getOutputStream()) {
            FileUtils.copyFile(jars.getJarFile(component), stream);
        }
    }

}
