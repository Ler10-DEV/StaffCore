package org.staffcore.storage.dao;

import org.staffcore.storage.model.ScoreEvent;
import org.staffcore.storage.model.ScoreRecord;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffScoreDAO {
    Optional<ScoreRecord> findByStaffUuid(UUID staffUuid);
    Collection<ScoreRecord> getAll();
    void save(ScoreRecord record);
    List<ScoreRecord> getLeaderboard(boolean weeklyOnly, int limit);
    void resetWeeklyScores();
    void logEvent(ScoreEvent event);
    List<ScoreEvent> getEventsForStaff(UUID staffUuid, int limit);
}
