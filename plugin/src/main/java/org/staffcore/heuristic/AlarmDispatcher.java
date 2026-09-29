package org.staffcore.heuristic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.staffcore.StaffCorePlugin;
import org.staffcore.discord.EmbedFactory;

public class AlarmDispatcher {
    private final StaffCorePlugin plugin;

    public AlarmDispatcher(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void dispatchAlarm(String type, String playerName, String detail) {
        // In-game staff broadcast
        Component alert = Component.text("⚠️ [GÜVENLİK ALARMI] ", NamedTextColor.RED)
                .append(Component.text(type + " - ", NamedTextColor.GOLD))
                .append(Component.text(playerName, NamedTextColor.YELLOW))
                .append(Component.text(": " + detail, NamedTextColor.GRAY));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("staff.use") || p.hasPermission("staff.report.view") || p.isOp()) {
                p.sendMessage(alert);
            }
        }

        // Discord embed
        var channel = plugin.getChannelRegistry().get("guvenlik_alarmlari");
        if (channel != null) {
            channel.sendMessageEmbeds(EmbedFactory.createSecurityAlarmEmbed(type, playerName, detail)).queue();
        }
    }
}
