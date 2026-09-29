package org.staffcore.report;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.staffcore.StaffCorePlugin;

import java.util.Map;

public class ReportCommand implements CommandExecutor {
    private final StaffCorePlugin plugin;

    public ReportCommand(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLocaleManager().getPrefixed("player_only", "&cBu komut yalnızca oyuncular içindir.", null));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cKullanım: /rapor <oyuncu>");
            return true;
        }

        String targetName = args[0];
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage("§cKendinizi raporlayamazsınız!");
            return true;
        }

        if (plugin.getReportTicketService().isOnCooldown(player.getUniqueId())) {
            long rem = plugin.getReportTicketService().getRemainingCooldown(player.getUniqueId());
            player.sendMessage(plugin.getLocaleManager().getPrefixed("report.cooldown",
                    "&cLütfen tekrar rapor göndermek için &e{seconds} &csaniye bekleyin.",
                    Map.of("seconds", String.valueOf(rem))));
            return true;
        }

        ReportGUI gui = new ReportGUI(plugin, targetName);
        gui.open(player);
        return true;
    }
}
