package org.staffcore.commandlog;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.staffcore.StaffCorePlugin;

import java.util.Map;

public class CommandLogCommand implements CommandExecutor {
    private final StaffCorePlugin plugin;

    public CommandLogCommand(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("staff.commandlog") && !sender.hasPermission("staff.use") && !sender.isOp()) {
            sender.sendMessage(plugin.getLocaleManager().getPrefixed("no_permission", "&cBu komutu kullanmak için yetkiniz yok!", null));
            return true;
        }

        if (!(sender instanceof Player staff)) {
            sender.sendMessage(plugin.getLocaleManager().getPrefixed("player_only", "&cBu komut yalnızca oyuncular içindir.", null));
            return true;
        }

        if (args.length < 1) {
            staff.sendMessage("§cKullanım: /komutlog <oyuncu>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        CommandLogGUI gui = new CommandLogGUI(plugin, targetName, target.getUniqueId());
        gui.open(staff);
        return true;
    }
}
