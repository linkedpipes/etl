package com.linkedpipes.etl.executor.plugin;

public class BannedComponent extends PluginException {

    public BannedComponent(Object... args) {
        super("Required component: {} is banned by: {}", args);
    }

}
