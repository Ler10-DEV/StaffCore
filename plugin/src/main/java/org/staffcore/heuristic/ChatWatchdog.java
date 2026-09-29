package org.staffcore.heuristic;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.staffcore.StaffCorePlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatWatchdog implements Listener {
    private final StaffCorePlugin plugin;
    private final AlarmDispatcher alarmDispatcher;
    private final List<Pattern> patterns = new ArrayList<>();

    public ChatWatchdog(StaffCorePlugin plugin, AlarmDispatcher alarmDispatcher) {
        this.plugin = plugin;
        this.alarmDispatcher = alarmDispatcher;
        loadPatterns();
    }

    public void loadPatterns() {
        patterns.clear();
        List<String> raw = plugin.getConfigManager().getChatFilterPatterns();
        if (raw != null) {
            for (String r : raw) {
                try {
                    patterns.add(Pattern.compile(r));
                } catch (Exception ignored) {}
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfigManager().isChatFilterEnabled()) return;
        Player player = event.getPlayer();
        if (player.hasPermission("staff.chatfilter.bypass") || player.isOp()) return;

        String msg = event.getMessage();
        for (Pattern p : patterns) {
            if (p.matcher(msg).find()) {
                event.setCancelled(true);
                player.sendMessage("§cMesajınız güvenlik filtresine takıldı ve engellendi!");
                alarmDispatcher.dispatchAlarm("Sohbet İhlali / Reklam", player.getName(), "Filtrelenen mesaj: `" + msg + "`");
                break;
            }
        }
    }
}
