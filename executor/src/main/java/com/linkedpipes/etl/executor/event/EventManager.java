package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.event.Event;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * Designed to handle publishing of event in the application.
 */
public class EventManager {

    @FunctionalInterface
    public static interface EventListener {

        /**
         * Called whenever someone publish an event, the event
         * has assigned IRI.
         *
         * @param event
         */
        public void onEvent(Event event);

    }

    /**
     * List of registered listeners.
     */
    private final List<WeakReference<EventListener>> listeners
            = new LinkedList<>();

    /**
     * Base prefix for events.
     */
    private final String iriPrefix;

    /**
     * Message counter used to create messages.
     */
    private Long counter = 0l;

    public EventManager(String executionIri) {
        this.iriPrefix = executionIri + "/events/";
    }

    /**
     * Add listener as a weak reference. If the object is destroyed the
     * reference is dropped.
     *
     * @param listener
     */
    public void addListener(EventListener listener) {
        listeners.add(new WeakReference(listener));
    }

    /**
     * Notify all registered listeners about new event.
     *
     * @param event
     */
    public synchronized void publish(Event event) {
        event.setResource(iriPrefix + counter);
        counter += 1;
        // Notify listeners and also remove null.
        final List<WeakReference<EventListener>> nullReferences
                = new LinkedList();
        for (WeakReference<EventListener> reference : listeners) {
            final EventListener listener = reference.get();
            if (listener != null) {
                listener.onEvent(event);
            } else {
                nullReferences.add(reference);
            }
        }
        listeners.removeAll(nullReferences);
    }

}
