package org.staffcore.storage.dao;

import org.staffcore.storage.model.KontRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for Kont (forensic inspection) history.
 * Implements rolling retention (e.g. keeping the last 1000 records).
 */
public interface KontRecordDAO {
    void addRecord(KontRecord record);
    List<KontRecord> getRecentRecords(int limit);
    List<KontRecord> findByTarget(UUID targetUuid);
    List<KontRecord> findByStaff(UUID staffUuid);
    Optional<KontRecord> findById(String id);
}
