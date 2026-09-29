package org.staffcore.storage.dao;

import org.staffcore.storage.model.Punishment;
import org.staffcore.storage.model.PunishmentStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PunishmentDAO {
    Optional<Punishment> findById(String id);
    Collection<Punishment> getAll();
    List<Punishment> findByTarget(UUID targetUuid);
    List<Punishment> findByStaff(UUID staffUuid);
    List<Punishment> findByStatus(PunishmentStatus status);
    void save(Punishment punishment);
    void delete(String id);
    String generateNextId();
}
