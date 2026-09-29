package org.staffcore.link;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.LinkRecord;

import java.util.Map;
import java.util.Optional;

public class LinkCommand implements CommandExecutor {
    private final StaffCorePlugin plugin;

    public LinkCommand(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        java.util.UUID targetUuid;
        String targetName;
        Player onlinePlayer = null;

        if (sender instanceof Player p) {
            targetUuid = p.getUniqueId();
            targetName = p.getName();
            onlinePlayer = p;
        } else if (args.length > 0) {
            targetName = args[0];
            onlinePlayer = org.bukkit.Bukkit.getPlayer(targetName);
            if (onlinePlayer != null) {
                targetUuid = onlinePlayer.getUniqueId();
            } else {
                org.bukkit.OfflinePlayer offline = org.bukkit.Bukkit.getOfflinePlayer(targetName);
                targetUuid = offline.getUniqueId();
            }
        } else {
            sender.sendMessage("§cKullanım (Konsol): /hesap-eşle <oyuncu>");
            return true;
        }

        Optional<LinkRecord> link = plugin.getAccountLinkService().getLinkRecord(targetUuid);
        if (link.isPresent()) {
            sender.sendMessage("§cBu hesap (" + targetName + ") zaten Discord ile eşlenmiş!");
            return true;
        }

        String code = plugin.getAccountLinkService().generateLinkCode(targetUuid);
        int ttl = plugin.getConfigManager().getLinkCodeTtlSeconds();

        if (onlinePlayer != null) {
            onlinePlayer.sendMessage(plugin.getLocaleManager().getPrefixed("link.code_generated",
                    "&aEşleme kodunuz: &e{code}&a. Discord üzerinden &b/esle {code} &ayazarak eşleyebilirsiniz. ({ttl}s)",
                    Map.of("code", code, "ttl", String.valueOf(ttl))));
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§a[" + targetName + "] Eşleme Kodu: §e" + code + " §7(Discord: /esle " + code + ")");
        }

        return true;
    }
}
