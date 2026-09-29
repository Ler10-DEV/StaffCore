package org.staffcore.link;

import org.bukkit.entity.Player;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.LinkRecord;

import java.util.Optional;
import java.util.UUID;

public class StaffFlagService {
    private final StaffCorePlugin plugin;

    public StaffFlagService(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isStaff(Player player) {
        if (player == null) return false;
        if (player.hasPermission("staff.use") || player.hasPermission("staff.admin") || player.isOp()) {
            return true;
        }
        return isStaff(player.getUniqueId());
    }

    public boolean isStaff(UUID uuid) {
        if (uuid == null) return false;
        Optional<LinkRecord> link = plugin.getStorageProvider().linkedAccounts().findByUuid(uuid);
        return link.isPresent() && link.get().isStaff();
    }
}
