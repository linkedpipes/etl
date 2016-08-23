package com.linkedpipes.etl.storage.jar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;

/**
 * @author Petr Škoda
 */
@Service
public class JarFacade {

    @Autowired
    private JarManager manager;

    /**
     * @return Collection of all components.
     */
    public Collection<JarComponent> getJarComponents() {
        return manager.getComponents().values();
    }

    /**
     * @param name
     * @return Null if there is no such component.
     */
    public JarComponent getJarComponent(String name) {
        return manager.getComponents().get(name);
    }

    /**
     * @param component
     * @return Path to the JAR file.
     */
    public File getJarFile(JarComponent component) {
        return component.getFile();
    }

}
