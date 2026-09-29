package org.staffcore.punishment;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.Punishment;
import org.staffcore.storage.model.PunishmentStatus;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProofReminderTask implements Runnable {
    private final StaffCorePlugin plugin;
    private final Logger logger;
    private static final long REMINDER_THRESHOLD_MILLIS = TimeUnit.MINUTES.toMillis(15);

    public ProofReminderTask(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @Override
    public void run() {
        try {
            List<Punishment> pending = plugin.getStorageProvider().punishments().findByStatus(PunishmentStatus.PENDING_PROOF);
            long now = System.currentTimeMillis();

            for (Punishment p : pending) {
                if (now - p.getTimestamp() > REMINDER_THRESHOLD_MILLIS) {
                    TextChannel channel = plugin.getChannelRegistry().get("ceza_log");
                    if (channel != null) {
                        channel.sendMessage("⚠️ **UYARI:** `" + p.getId() + "` numaralı ceza için henüz kanıt yüklenmedi! Yetkili: **" + p.getStaffName() + "**").queue();
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Error running ProofReminderTask: " + e.getMessage());
        }
    }
}
