package org.staffcore.punishment;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.Punishment;

import java.util.Arrays;

public class PunishCommand implements CommandExecutor {
    private final StaffCorePlugin plugin;

    public PunishCommand(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("staff.punish") && !sender.isOp()) {
            sender.sendMessage(plugin.getLocaleManager().getPrefixed("no_permission", "&cBu komutu kullanmak için yetkiniz yok!", null));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cKullanım: /ceza <oyuncu> <sebep>");
            return true;
        }

        String targetName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        Player staff = sender instanceof Player p ? p : null;

        Punishment p = plugin.getPunishmentService().issuePunishment(target, staff, reason, "BAN", -1);
        sender.sendMessage("§aCeza uygulandı: §b" + p.getId() + " §7- Lütfen Discord kanalından kanıt ekleyiniz.");
        return true;
    }
}
