package com.linkedpipes.etl.storage.jar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;

@Service
public class JarFacade {

    @Autowired
    private JarManager manager;

    /**
     * Return collection of all components.
     */
    public Collection<JarComponent> getJarComponents() {
        return manager.getComponents().values();
    }

    /**
     * Return Null if there is no such component.
     */
    public JarComponent getJarComponent(String name) {
        return manager.getComponents().get(name);
    }

    /**
     * Return path to the JAR file.
     */
    public File getJarFile(JarComponent component) {
        return component.getFile();
    }

}
