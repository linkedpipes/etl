package com.linkedpipes.etl.storage.jar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;

@Service
public class JarFacade {

    private final JarService service;

    @Autowired
    public JarFacade(JarService service) {
        this.service = service;
    }

    /**
     * Return collection of all components.
     */
    public Collection<JarComponent> getJarComponents() {
        return service.getComponents().values();
    }

    /**
     * Return Null if there is no such component.
     */
    public JarComponent getJarComponent(String name) {
        return service.getComponents().get(name);
    }

    /**
     * Return path to the JAR file.
     */
    public File getJarFile(JarComponent component) {
        return component.getFile();
    }

}
