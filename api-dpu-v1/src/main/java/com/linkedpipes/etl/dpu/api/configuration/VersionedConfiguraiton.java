package com.linkedpipes.etl.dpu.api.configuration;

/**
 *
 * @author Škoda Petr
 * @param <T>
 */
public interface VersionedConfiguraiton<T> {

    public class MigrationFailed extends Exception {

        public MigrationFailed(String message) {
            super(message);
        }

    }

    public T migrate();

}
