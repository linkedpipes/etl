package com.linkedpipes.etl.executor.plugin;

public class BannedComponent extends PluginException {

    public BannedComponent(String messages, Object... args) {
        super("Required component: {} is banned by: {}", args);
    }

}
