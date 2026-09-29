package org.staffcore.storage.model;

import java.util.UUID;

public class ScoreRecord {
    private final UUID staffUuid;
    private final String staffName;
    private int totalScore;
    private int weeklyScore;
    private int reportsResolved;
    private int kontsCompleted;
    private int punishmentsIssued;
    private int positiveRatings;
    private int negativeRatings;
    private long lastUpdated;

    public ScoreRecord(UUID staffUuid, String staffName, int totalScore, int weeklyScore,
                       int reportsResolved, int kontsCompleted, int punishmentsIssued,
                       int positiveRatings, int negativeRatings, long lastUpdated) {
        this.staffUuid = staffUuid;
        this.staffName = staffName;
        this.totalScore = totalScore;
        this.weeklyScore = weeklyScore;
        this.reportsResolved = reportsResolved;
        this.kontsCompleted = kontsCompleted;
        this.punishmentsIssued = punishmentsIssued;
        this.positiveRatings = positiveRatings;
        this.negativeRatings = negativeRatings;
        this.lastUpdated = lastUpdated;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public String getStaffName() {
        return staffName;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void addScore(int delta) {
        this.totalScore += delta;
        this.weeklyScore += delta;
        this.lastUpdated = System.currentTimeMillis();
    }

    public int getWeeklyScore() {
        return weeklyScore;
    }

    public void resetWeeklyScore() {
        this.weeklyScore = 0;
        this.lastUpdated = System.currentTimeMillis();
    }

    public int getReportsResolved() {
        return reportsResolved;
    }

    public void incrementReportsResolved() {
        this.reportsResolved++;
    }

    public int getKontsCompleted() {
        return kontsCompleted;
    }

    public void incrementKontsCompleted() {
        this.kontsCompleted++;
    }

    public int getPunishmentsIssued() {
        return punishmentsIssued;
    }

    public void incrementPunishmentsIssued() {
        this.punishmentsIssued++;
    }

    public int getPositiveRatings() {
        return positiveRatings;
    }

    public void incrementPositiveRatings() {
        this.positiveRatings++;
    }

    public int getNegativeRatings() {
        return negativeRatings;
    }

    public void incrementNegativeRatings() {
        this.negativeRatings++;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
