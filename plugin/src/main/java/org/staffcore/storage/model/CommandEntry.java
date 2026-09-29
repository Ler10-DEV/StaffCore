package org.staffcore.storage.model;

/**
 * Represents a logged command executed by a player.
 */
public class CommandEntry {
    private final String command;
    private final long timestamp;
    private final String location;

    public CommandEntry(String command, long timestamp, String location) {
        this.command = command;
        this.timestamp = timestamp;
        this.location = location;
    }

    public String getCommand() {
        return command;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }
}
