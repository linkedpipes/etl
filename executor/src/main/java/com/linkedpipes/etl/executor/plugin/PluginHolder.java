package com.linkedpipes.etl.executor.plugin;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;

/**
 * Plugin holder interface, specialization must provide instance factory method.
 */
public interface PluginHolder {

    PluginTemplate template();

}
