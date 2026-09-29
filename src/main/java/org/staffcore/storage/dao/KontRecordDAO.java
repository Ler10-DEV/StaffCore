package org.staffcore.storage.dao;

import org.staffcore.storage.model.KontRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KontRecordDAO {
    void addRecord(KontRecord record);
    List<KontRecord> getRecentRecords(int limit);
    List<KontRecord> findByTarget(UUID targetUuid);
    List<KontRecord> findByStaff(UUID staffUuid);
    Optional<KontRecord> findById(String id);
}
