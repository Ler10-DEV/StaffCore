package org.staffcore.storage.model;

import java.util.UUID;

/**
 * Represents a historical record of a Kont (forensic inspection) session.
 */
public class KontRecord {
    private final String id;
    private final UUID targetUuid;
    private final String targetName;
    private final UUID staffUuid;
    private final String staffName;
    private final String outcome; // CLEAN, HACKS_BANNED, CONFESSION, COMBAT_QUIT
    private final long startedAt;
    private final long endedAt;
    private final int durationSeconds;
    private final String notes;

    public KontRecord(String id, UUID targetUuid, String targetName, UUID staffUuid, String staffName,
                      String outcome, long startedAt, long endedAt, int durationSeconds, String notes) {
        this.id = id;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.staffUuid = staffUuid;
        this.staffName = staffName;
        this.outcome = outcome;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getOutcome() {
        return outcome;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndedAt() {
        return endedAt;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getNotes() {
        return notes;
    }
}
