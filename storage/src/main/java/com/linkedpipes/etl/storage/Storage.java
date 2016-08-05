package com.linkedpipes.etl.storage;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Petr Škoda
 */
public class Storage {

    public static void main(String[] args) {
        final AbstractApplicationContext context
                = new ClassPathXmlApplicationContext(
                        "spring/context-storage.xml");
        context.registerShutdownHook();
        context.start();
    }

}
