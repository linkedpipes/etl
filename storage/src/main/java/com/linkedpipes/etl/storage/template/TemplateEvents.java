package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;

import java.util.ArrayList;
import java.util.List;

public class TemplateEvents {

    public interface Listener {

        default void onPluginTemplateLoaded(PluginTemplate template) {
            // Do nothing.
        }

        default void onReferenceTemplateLoaded(ReferenceTemplate template) {
            // Do nothing.
        }

        default void onReferenceTemplateCreated(ReferenceTemplate template) {
            // Do nothing.
        }

        default void onReferenceTemplateUpdated(
                ReferenceTemplate previous, ReferenceTemplate next) {

        }

        default void onReferenceTemplateDeleted(ReferenceTemplate template) {
            // Do nothing.
        }

        default void onReferenceTemplateReload() {
            // Do nothing.
        }

    }

    private final List<Listener> registered = new ArrayList<>();

    public void register(Listener listener) {
        registered.add(listener);
    }

    public void onPluginTemplateLoaded(PluginTemplate template) {
        for (Listener listener : registered) {
            listener.onPluginTemplateLoaded(template);
        }
    }

    public void onReferenceTemplateLoad(ReferenceTemplate template) {
        for (Listener listener : registered) {
            listener.onReferenceTemplateLoaded(template);
        }
    }

    public void onReferenceTemplateCreated(ReferenceTemplate template) {
        for (Listener listener : registered) {
            listener.onReferenceTemplateCreated(template);
        }
    }

    public void onReferenceTemplateUpdated(
            ReferenceTemplate previous, ReferenceTemplate next) {
        for (Listener listener : registered) {
            listener.onReferenceTemplateUpdated(previous, next);
        }
    }

    public void onReferenceTemplateDeleted(ReferenceTemplate template) {
        for (Listener listener : registered) {
            listener.onReferenceTemplateDeleted(template);
        }
    }

    public void onReferenceTemplateReload() {
        for (Listener listener : registered) {
            listener.onReferenceTemplateReload();
        }
    }

}
