package org.staffcore.storage.model;

import java.util.UUID;

public class ScoreEvent {
    private final String eventId;
    private final UUID staffUuid;
    private final String staffName;
    private final String action;
    private final int delta;
    private final int resultingScore;
    private final String detail;
    private final long timestamp;

    public ScoreEvent(String eventId, UUID staffUuid, String staffName, String action,
                      int delta, int resultingScore, String detail, long timestamp) {
        this.eventId = eventId;
        this.staffUuid = staffUuid;
        this.staffName = staffName;
        this.action = action;
        this.delta = delta;
        this.resultingScore = resultingScore;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getAction() {
        return action;
    }

    public int getDelta() {
        return delta;
    }

    public int getResultingScore() {
        return resultingScore;
    }

    public String getDetail() {
        return detail;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
