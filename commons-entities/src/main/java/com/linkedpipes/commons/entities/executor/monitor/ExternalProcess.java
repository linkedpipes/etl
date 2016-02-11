package com.linkedpipes.commons.entities.executor.monitor;

/**
 *
 * @author Petr Škoda
 */
public class ExternalProcess {

    /**
     * Process id.
     */
    private String id;

    /**
     * Time when created.
     */
    private long created;

    /**
     * Command used to start the process.
     */
    private String command;

    /**
     * Generated description.
     */
    private String description;

    /**
     * Link to the service.
     */
    private String linkToService;

    public ExternalProcess() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLinkToService() {
        return linkToService;
    }

    public void setLinkToService(String linkToService) {
        this.linkToService = linkToService;
    }

}
