package org.staffcore.bootstrap;

import org.bukkit.Bukkit;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.json.JsonStorageProvider;

import java.util.logging.Logger;

public class ShutdownHook {
    private final StaffCorePlugin plugin;
    private final Logger logger;

    public ShutdownHook(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void executeShutdown() {
        logger.info("Executing graceful shutdown for StaffCore...");

if (plugin.getStorageProvider() != null) {
            plugin.getStorageProvider().flush();
            if (plugin.getStorageProvider() instanceof JsonStorageProvider jsonProv) {
                try {
                    jsonProv.getBackupService().createBackupNow();
                } catch (Exception e) {
                    logger.warning("Shutdown backup failed: " + e.getMessage());
                }
            }
            plugin.getStorageProvider().shutdown();
        }

if (plugin.getDiscordBot() != null) {
            plugin.getDiscordBot().stop();
        }

Bukkit.getScheduler().cancelTasks(plugin);

        logger.info("StaffCore shutdown completed.");
    }
}
