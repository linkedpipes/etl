package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.storage.jar.JavaPluginService;
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
    private JavaPluginService jars;

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    @ResponseBody
    public void getJarFile(
            @RequestParam(name = "iri") String iri,
            HttpServletResponse response)
            throws IOException, MissingResource {
        JavaPlugin plugin = jars.getJavaPlugin(iri);
        if (plugin == null) {
            throw new MissingResource("Missing jar file: {}", iri);
        }
        response.setStatus(HttpStatus.SC_OK);
        response.setHeader("Content-Type", "application/octet-stream");
        try (OutputStream stream = response.getOutputStream()) {
            FileUtils.copyFile(plugin.file(), stream);
        }
    }

}
