package com.linkedpipes.etl.dpu.component;

import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.component.ComponentFactory;
import com.linkedpipes.etl.utils.core.entity.EntityLoader;

/**
 *
 * @author Škoda Petr
 */
@org.osgi.service.component.annotations.Component(immediate = true, service = {ComponentFactory.class})
public class DpuComponentFactory implements ComponentFactory {

    @Override
    public Component createComponent(SparqlSelect definition, String subject, String graph, BundleContext context)
            throws ComponentFactory.InvalidBundle {
        // Scan bundle for class with ComponentInstance class.
        final BundleInformation info = scanBundle(context.getBundle());
        // Create an instance.
        final SequentialExecution instance;
        try {
            instance = (SequentialExecution) info.getComponentClasse().newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new ComponentFactory.InvalidBundle("Can't create component class!", ex);
        }
        // Load configuration.
        final DpuConfiguration configuration = new DpuConfiguration(subject);
        try {
            EntityLoader.load(definition, subject, graph, configuration);
        } catch (EntityLoader.LoadingFailed ex) {
            throw new ComponentFactory.InvalidBundle("Can't load configuration!", ex);
        }
        // Create wrap and return it.
        return new SequentialComponent(instance, info, configuration, definition, graph);
    }

    /**
     * Scan bundle and extract it's content in form of a configuration.
     *
     * @param bundle
     * @return
     * @throws cz.cuni.mff.xrg.cuv.executor.api.v1.plugin.component.ComponentFactory.InvalidBundle
     */
    private BundleInformation scanBundle(Bundle bundle) throws InvalidBundle {
        final List<Class<?>> mainClasses = new ArrayList<>(1);
        final List<String> packages = new ArrayList<>(2);
        //
        final BundleRevision revision = bundle.adapt(BundleRevision.class);
        final BundleWiring wiring = revision.getWiring();
        if (wiring == null) {
            throw new ComponentFactory.InvalidBundle("Can't instantiate BundleWiring!");
        }
        // Search for classes.
        final Enumeration<URL> classContent = bundle.findEntries("/", "*.class", true);
        while (classContent.hasMoreElements()) {
            final String path = classContent.nextElement().getPath();
            final String className = path.substring(1, path.length() - 6)
                    .replaceAll("/", ".");
            if (!className.contains("$")) {
                final Class<?> clazz;
                try {
                    clazz = bundle.loadClass(className);
                } catch (ClassNotFoundException ex) {
                    throw new ComponentFactory.InvalidBundle("Can't create class instance: {}", className, ex);
                }
                // Scan interfaces.
                for (Type item : clazz.getGenericInterfaces()) {
                    if (item.getTypeName().equals(SequentialExecution.class.getCanonicalName())) {
                        mainClasses.add(clazz);
                    }
                }
                // In evere case we can use the package name.
                final String packageName = className.substring(0, className.lastIndexOf("."));
                if (!packages.contains(packageName)) {
                    packages.add(packageName);
                }
            }
        }
        //
        if (mainClasses.isEmpty()) {
            // No class that we can work with.
            return null;
        } else if (mainClasses.size() > 1) {
            // Multiple classes - invalid bundle.
            throw new InvalidBundle("Invalid number of components detected: {}", mainClasses.size());
        }
        return new BundleInformation(mainClasses.get(0), packages);
    }

}
