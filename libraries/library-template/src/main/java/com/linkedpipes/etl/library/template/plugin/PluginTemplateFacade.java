package com.linkedpipes.etl.library.template.plugin;

import com.linkedpipes.etl.library.template.plugin.adapter.JavaFileToJavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;

public class PluginTemplateFacade {

    protected PluginTemplateFacade() {
    }

    public static JavaPlugin loadJavaFile(File file) throws PluginException {
        return JavaFileToJavaPlugin.asJavaPlugins(file);
    }

    public static byte[] readFile(JavaPlugin plugin, JarEntry entry)
            throws PluginException {
        try (InputStream stream = plugin.jarFile().getInputStream(entry)) {
            return stream.readAllBytes();
        } catch (IOException ex) {
            throw new PluginException(
                    "Can't read entry: {}", entry.getName());
        }
    }

    public static void transferFile(
            JavaPlugin plugin, JarEntry entry, OutputStream stream)
            throws PluginException {
        try (InputStream input = plugin.jarFile().getInputStream(entry)) {
            input.transferTo(stream);
        } catch (IOException ex) {
            throw new PluginException(
                    "Can't read entry: {}", entry.getName());
        }
    }

}
