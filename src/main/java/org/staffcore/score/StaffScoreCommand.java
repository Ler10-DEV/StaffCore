package org.staffcore.score;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.ScoreRecord;

import java.util.Map;
import java.util.Optional;

public class StaffScoreCommand implements CommandExecutor {
    private final StaffCorePlugin plugin;

    public StaffScoreCommand(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("staff.score.view") && !sender.isOp()) {
            sender.sendMessage(plugin.getLocaleManager().getPrefixed("no_permission", "&cBu komutu kullanmak için yetkiniz yok!", null));
            return true;
        }

        OfflinePlayer target;
        if (args.length > 0) {
            target = Bukkit.getOfflinePlayer(args[0]);
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("§cKonsol için kullanım: /staffscore <oyuncu>");
            return true;
        }

        Optional<ScoreRecord> opt = plugin.getStorageProvider().staffScores().findByStaffUuid(target.getUniqueId());
        if (opt.isPresent()) {
            ScoreRecord r = opt.get();
            sender.sendMessage(plugin.getLocaleManager().getPrefixed("score.view",
                    "&b{player} &7Mevcut Yetkili Skoru: &e{score} &8(Haftalık: +{weekly})",
                    Map.of("player", r.getStaffName(), "score", String.valueOf(r.getTotalScore()), "weekly", String.valueOf(r.getWeeklyScore()))));
        } else {
            sender.sendMessage("§e" + target.getName() + " §7adlı yetkiliye ait puan kaydı bulunamadı (Varsayılan: 100).");
        }
        return true;
    }
}
