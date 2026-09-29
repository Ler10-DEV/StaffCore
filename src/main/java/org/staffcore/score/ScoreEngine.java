package org.staffcore.score;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.ScoreEvent;
import org.staffcore.storage.model.ScoreRecord;

import java.util.Optional;
import java.util.UUID;

public class ScoreEngine {
    private final StaffCorePlugin plugin;

    public ScoreEngine(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    public ScoreRecord getOrCreateScore(UUID staffUuid, String staffName) {
        Optional<ScoreRecord> opt = plugin.getStorageProvider().staffScores().findByStaffUuid(staffUuid);
        if (opt.isPresent()) {
            return opt.get();
        }
        int baseScore = plugin.getConfigManager().getBaseScore();
        ScoreRecord record = new ScoreRecord(
                staffUuid,
                staffName != null ? staffName : "Unknown",
                baseScore,
                0,
                0,
                0,
                0,
                0,
                0,
                System.currentTimeMillis()
        );
        plugin.getStorageProvider().staffScores().save(record);
        return record;
    }

    public synchronized void awardScore(UUID staffUuid, String staffName, String actionKey, String detail) {
        if (staffUuid == null) return;
        ScoreRecord record = getOrCreateScore(staffUuid, staffName);

        int delta = plugin.getConfigManager().getScoreDelta(actionKey, getDefaultDelta(actionKey));
        record.addScore(delta);

switch (actionKey) {
            case "report_resolved" -> record.incrementReportsResolved();
            case "kont_clean", "kont_hacks_banned", "kont_confession" -> record.incrementKontsCompleted();
            case "punishment_verified" -> record.incrementPunishmentsIssued();
            case "csat_5_stars", "csat_4_stars", "csat_3_stars", "csat_2_stars" -> record.incrementPositiveRatings();
            case "csat_1_star", "csat_bad" -> record.incrementNegativeRatings();
        }

        plugin.getStorageProvider().staffScores().save(record);

ScoreEvent event = new ScoreEvent(
                UUID.randomUUID().toString(),
                staffUuid,
                staffName != null ? staffName : record.getStaffName(),
                actionKey,
                delta,
                record.getTotalScore(),
                detail,
                System.currentTimeMillis()
        );
        plugin.getStorageProvider().staffScores().logEvent(event);
        plugin.getStorageProvider().flush();

int threshold = plugin.getConfigManager().getLowScoreThreshold();
        if (record.getTotalScore() < threshold) {
            Player p = Bukkit.getPlayer(staffUuid);
            if (p != null) {
                p.sendMessage("§c⚠️ UYARI: Yetkili skorunuz kritik seviyenin altında (" + record.getTotalScore() + ")!");
            }
        }
    }

    private int getDefaultDelta(String actionKey) {
        return switch (actionKey) {
            case "report_resolved" -> 5;
            case "kont_clean" -> 15;
            case "kont_hacks_banned" -> 25;
            case "kont_confession" -> 10;
            case "punishment_verified" -> 5;
            case "csat_5_stars" -> 5;
            case "csat_4_stars" -> 3;
            case "csat_3_stars" -> 1;
            case "csat_2_stars" -> -2;
            case "csat_1_star" -> -5;
            case "unjust_punishment_penalty" -> -20;
            case "missed_kont_call" -> -10;
            default -> 0;
        };
    }
}
