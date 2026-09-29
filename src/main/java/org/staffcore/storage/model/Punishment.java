package org.staffcore.storage.model;

import java.util.Objects;
import java.util.UUID;

public class Punishment {
    private final String id; 
    private final UUID targetUuid;
    private final String targetName;
    private final UUID staffUuid;
    private final String staffName;
    private final String reason;
    private final String type; 
    private final long timestamp;
    private final long durationMillis; 
    private PunishmentStatus status;
    private String proofUrl;
    private String reviewNote;
    private long verifiedAt;

    public Punishment(String id, UUID targetUuid, String targetName, UUID staffUuid, String staffName,
                      String reason, String type, long timestamp, long durationMillis,
                      PunishmentStatus status, String proofUrl, String reviewNote, long verifiedAt) {
        this.id = id;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.staffUuid = staffUuid;
        this.staffName = staffName;
        this.reason = reason;
        this.type = type;
        this.timestamp = timestamp;
        this.durationMillis = durationMillis;
        this.status = status != null ? status : PunishmentStatus.PENDING_PROOF;
        this.proofUrl = proofUrl;
        this.reviewNote = reviewNote;
        this.verifiedAt = verifiedAt;
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

    public String getReason() {
        return reason;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public PunishmentStatus getStatus() {
        return status;
    }

    public void setStatus(PunishmentStatus status) {
        this.status = status;
    }

    public String getProofUrl() {
        return proofUrl;
    }

    public void setProofUrl(String proofUrl) {
        this.proofUrl = proofUrl;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public long getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(long verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public boolean isPermanent() {
        return durationMillis <= 0;
    }

    public boolean isExpired() {
        if (isPermanent()) return false;
        return System.currentTimeMillis() > (timestamp + durationMillis);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Punishment that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
