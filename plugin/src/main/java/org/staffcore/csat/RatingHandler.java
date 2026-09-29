package org.staffcore.csat;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.staffcore.StaffCorePlugin;
import org.staffcore.discord.EmbedFactory;
import org.staffcore.storage.model.ScoreRecord;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RatingHandler {
    private final StaffCorePlugin plugin;
    private final Map<Integer, PendingCsat> pendingMap = new ConcurrentHashMap<>();
    private final Set<Integer> completedTickets = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> directRateCooldowns = new ConcurrentHashMap<>();

    public record PendingCsat(int ticketId, UUID reporterUuid, String staffName, long createdAt) {
        public boolean isExpired() {
            return (System.currentTimeMillis() - createdAt) > (15 * 60 * 1000L);
        }
    }

    public RatingHandler(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerPending(int ticketId, UUID reporterUuid, String staffName) {
        pendingMap.put(ticketId, new PendingCsat(ticketId, reporterUuid, staffName, System.currentTimeMillis()));
    }

    public boolean submitTicketVote(Player player, int ticketId, int stars, String comment) {
        if (completedTickets.contains(ticketId)) {
            player.sendMessage(plugin.getLocaleManager().getPrefixed("csat.already_voted", "&cBu bildirim için zaten oy kullandınız.", null));
            return false;
        }

        PendingCsat pending = pendingMap.get(ticketId);
        if (pending == null) {
            player.sendMessage(plugin.getLocaleManager().getPrefixed("csat.expired", "&cBu geri bildirimin oylama süresi dolmuş veya geçersiz.", null));
            return false;
        }

        if (pending.isExpired()) {
            pendingMap.remove(ticketId);
            player.sendMessage(plugin.getLocaleManager().getPrefixed("csat.expired", "&cBu geri bildirimin oylama süresi dolmuş.", null));
            return false;
        }

        if (!pending.reporterUuid().equals(player.getUniqueId())) {
            player.sendMessage("§cBu geri bildirim yalnızca rapor sahibi tarafından oylanabilir!");
            return false;
        }

        completedTickets.add(ticketId);
        pendingMap.remove(ticketId);

        return processRating(player, pending.staffName(), stars, comment, "Ticket #" + ticketId);
    }

    public boolean submitDirectStaffVote(Player player, String staffName, int stars, String comment) {
        if (player != null) {
            if (player.getName().equalsIgnoreCase(staffName)) {
                player.sendMessage("§cKendinize puan veremezsiniz!");
                return false;
            }

            OfflinePlayer targetStaff = Bukkit.getOfflinePlayer(staffName);
            if (targetStaff.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("§cKendinize puan veremezsiniz!");
                return false;
            }

            // Cooldown check (e.g. 15 minutes per staff)
            String cooldownKey = player.getUniqueId() + ":" + staffName.toLowerCase();
            Long lastTime = directRateCooldowns.get(cooldownKey);
            long now = System.currentTimeMillis();
            long cooldownMillis = 15 * 60 * 1000L; // 15 mins

            if (lastTime != null && (now - lastTime) < cooldownMillis) {
                long remainingMins = ((lastTime + cooldownMillis) - now) / 60000L + 1;
                player.sendMessage("§cBu yetkiliyi tekrar puanlamak için lütfen §e" + remainingMins + " dakika §cbekleyin.");
                return false;
            }

            directRateCooldowns.put(cooldownKey, now);
        }

        return processRating(player, staffName, stars, comment, "Doğrudan Değerlendirme");
    }

    private boolean processRating(Player player, String staffName, int stars, String comment, String source) {
        stars = Math.max(1, Math.min(5, stars));
        String actionKey = "csat_" + stars + (stars == 1 ? "_star" : "_stars");

        OfflinePlayer staffOffline = Bukkit.getOfflinePlayer(staffName);
        UUID staffUuid = staffOffline.getUniqueId();

        String detail = String.format("%s (%d Yıldız)%s", source, stars,
                (comment != null && !comment.trim().isEmpty()) ? ": " + comment.trim() : "");

        plugin.getScoreEngine().awardScore(staffUuid, staffName, actionKey, detail);
        ScoreRecord scoreRecord = plugin.getScoreEngine().getOrCreateScore(staffUuid, staffName);

        String starSymbols = "★".repeat(stars) + "☆".repeat(5 - stars);
        String playerName = player != null ? player.getName() : "CONSOLE";

        // Feedback sound & message to player
        if (player != null) {
            if (stars >= 4) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            }

            player.sendMessage("§a[StaffCore] §7Değerlendirmeniz kaydedildi: §e" + starSymbols + " §8(" + stars + "/5 Yıldız)");
            if (comment != null && !comment.trim().isEmpty()) {
                player.sendMessage("§7Notunuz: §f" + comment);
            }
        }

        // Notify staff if online
        Player onlineStaff = Bukkit.getPlayer(staffUuid);
        if (onlineStaff != null && onlineStaff.isOnline()) {
            onlineStaff.sendMessage(String.format("§b[StaffCore] §7Bir oyuncu (%s) size §e%s §7puan verdi! (Güncel Skorunuz: §e%d§7)",
                    playerName, starSymbols, scoreRecord.getTotalScore()));
        }

        // Discord Notification
        try {
            var channel = plugin.getChannelRegistry().get("haftalik_karne");
            if (channel == null) channel = plugin.getChannelRegistry().get("raporlar");
            if (channel != null) {
                channel.sendMessageEmbeds(EmbedFactory.createRatingEmbed(playerName, staffName, stars, comment, scoreRecord.getTotalScore())).queue();
            }
        } catch (Exception ignored) {}

        return true;
    }

    public boolean submitVote(Player player, int ticketId, int stars) {
        return submitTicketVote(player, ticketId, stars, null);
    }
}
