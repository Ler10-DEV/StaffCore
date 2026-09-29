package org.staffcore.report;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.staffcore.StaffCorePlugin;
import org.staffcore.discord.EmbedFactory;

public class ReportNotifier {
    private final StaffCorePlugin plugin;

    public ReportNotifier(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void notifyStaff(String reporter, String target, String category, Location targetLoc) {
        String locStr = targetLoc != null
                ? String.format("%s, X:%.0f Y:%.0f Z:%.0f", targetLoc.getWorld().getName(), targetLoc.getX(), targetLoc.getY(), targetLoc.getZ())
                : "Bilinmiyor";

        Component alert = Component.text("🚨 [RAPOR] ", NamedTextColor.RED)
                .append(Component.text(reporter, NamedTextColor.YELLOW))
                .append(Component.text(" -> ", NamedTextColor.GRAY))
                .append(Component.text(target, NamedTextColor.GOLD))
                .append(Component.text(" (" + category + ") ", NamedTextColor.AQUA))
                .append(Component.text("[İNCELE]", NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/komutlog " + target)));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("staff.report.view") || p.hasPermission("staff.use")) {
                p.sendMessage(alert);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.8f);
            }
        }

        // Notify Discord
        var channel = plugin.getChannelRegistry().get("raporlar");
        if (channel != null) {
            channel.sendMessageEmbeds(EmbedFactory.createReportEmbed(reporter, target, category, locStr)).queue();
        }
    }
}
