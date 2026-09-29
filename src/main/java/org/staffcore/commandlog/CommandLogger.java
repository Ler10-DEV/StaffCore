package org.staffcore.commandlog;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.CommandEntry;

import java.time.Instant;
import java.util.UUID;

public class CommandLogger implements Listener {
    private final StaffCorePlugin plugin;
    private final MaskFilter maskFilter;

    public CommandLogger(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.maskFilter = new MaskFilter(plugin.getConfigManager().getCommandMaskPatterns());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String rawMsg = event.getMessage();
        if (rawMsg == null || rawMsg.isEmpty()) return;

        String masked = maskFilter.maskCommand(rawMsg);
        Location loc = player.getLocation();
        String locStr = String.format("%s, X:%.0f Y:%.0f Z:%.0f", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());

        CommandEntry entry = new CommandEntry(masked, System.currentTimeMillis(), locStr);
        plugin.getStorageProvider().commandLogs().logCommand(player.getUniqueId(), entry);

        // Stream to Discord via rate limit guard
        var channel = plugin.getChannelRegistry().get("komut_log");
        if (channel != null && plugin.getDiscordBot().getRateLimitGuard() != null) {
            String logMsg = String.format("`[%s]` **%s**: `%s` *(%s)*",
                    Instant.now().toString().substring(11, 19), player.getName(), masked, locStr);
            plugin.getDiscordBot().getRateLimitGuard().queueMessage(channel, logMsg);
        }
    }

    public MaskFilter getMaskFilter() {
        return maskFilter;
    }
}
