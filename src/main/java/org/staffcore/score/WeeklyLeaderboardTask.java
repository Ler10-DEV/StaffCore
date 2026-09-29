package org.staffcore.score;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.ScoreRecord;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

public class WeeklyLeaderboardTask implements Runnable {
    private final StaffCorePlugin plugin;
    private final Logger logger;

    public WeeklyLeaderboardTask(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @Override
    public void run() {
        try {
            List<ScoreRecord> leaders = plugin.getStorageProvider().staffScores().getLeaderboard(true, 10);
            if (leaders.isEmpty()) return;

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("📊 Haftalık Yetkili Karnesi & Liderlik Tablosu")
                    .setDescription("Bu haftanın en aktif yetkilileri ve puanları:")
                    .setColor(new Color(255, 180, 0))
                    .setTimestamp(Instant.now());

            int rank = 1;
            for (ScoreRecord r : leaders) {
                String medal = switch (rank) {
                    case 1 -> "🥇";
                    case 2 -> "🥈";
                    case 3 -> "🥉";
                    default -> "🔹";
                };
                eb.addField(medal + " #" + rank + " " + r.getStaffName(),
                        "Haftalık: `+" + r.getWeeklyScore() + "` | Toplam: `" + r.getTotalScore() + "` | Çözülen Rapor: `" + r.getReportsResolved() + "`", false);
                rank++;
            }

            TextChannel channel = plugin.getChannelRegistry().get("haftalik_karne");
            if (channel != null) {
                channel.sendMessageEmbeds(eb.build()).queue();
            }

ScoreRecord winner = leaders.get(0);
            if (winner.getWeeklyScore() > 0) {
                plugin.getRoleAssigner().assignStaffOfTheWeek(winner.getStaffUuid());
            }

plugin.getStorageProvider().staffScores().resetWeeklyScores();
            plugin.getStorageProvider().flush();
            logger.info("Weekly leaderboard published and weekly scores reset.");
        } catch (Exception e) {
            logger.warning("Error running WeeklyLeaderboardTask: " + e.getMessage());
        }
    }
}
