package org.staffcore.kont;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.staffcore.StaffCorePlugin;
import org.staffcore.discord.EmbedFactory;
import org.staffcore.storage.model.KontRecord;
import org.staffcore.storage.model.LinkRecord;
import org.staffcore.storage.model.Punishment;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KontManager {
    private final StaffCorePlugin plugin;
    private final KontDiscordRooms discordRooms;
    private final Map<UUID, ActiveKontSession> activeSessions = new ConcurrentHashMap<>();

    public static class ActiveKontSession {
        private final UUID targetUuid;
        private final String targetName;
        private final UUID staffUuid;
        private final String staffName;
        private final long startTime;
        private final KontFreezeTask freezeTask;
        private final KontDiscordRooms.ActiveRooms discordRooms;
        private int extensionCount = 0;

        public ActiveKontSession(UUID targetUuid, String targetName, UUID staffUuid, String staffName,
                                 long startTime, KontFreezeTask freezeTask, KontDiscordRooms.ActiveRooms discordRooms) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
            this.staffUuid = staffUuid;
            this.staffName = staffName;
            this.startTime = startTime;
            this.freezeTask = freezeTask;
            this.discordRooms = discordRooms;
        }

        public UUID getTargetUuid() { return targetUuid; }
        public String getTargetName() { return targetName; }
        public UUID getStaffUuid() { return staffUuid; }
        public String getStaffName() { return staffName; }
        public long getStartTime() { return startTime; }
        public KontFreezeTask getFreezeTask() { return freezeTask; }
        public KontDiscordRooms.ActiveRooms getDiscordRooms() { return discordRooms; }
        public int getExtensionCount() { return extensionCount; }
        public void incrementExtension() { this.extensionCount++; }
    }

    public KontManager(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.discordRooms = new KontDiscordRooms(plugin);
    }

    public boolean isInKont(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }

    public ActiveKontSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    public boolean startKont(Player staff, Player target) {
        if (isInKont(target.getUniqueId())) {
            return false;
        }

        int durationSec = plugin.getConfigManager().getKontDurationSeconds();
        Location targetLoc = target.getLocation();

KontFreezeTask freezeTask = new KontFreezeTask(plugin, target.getUniqueId(), targetLoc, durationSec);
        freezeTask.runTaskTimer(plugin, 0L, 20L);

Optional<LinkRecord> targetLink = plugin.getStorageProvider().linkedAccounts().findByUuid(target.getUniqueId());
        String targetDiscordId = targetLink.map(LinkRecord::getDiscordId).orElse(null);

KontDiscordRooms.ActiveRooms rooms = discordRooms.createRooms(staff.getName(), target.getName(), targetDiscordId);

        ActiveKontSession session = new ActiveKontSession(
                target.getUniqueId(),
                target.getName(),
                staff.getUniqueId(),
                staff.getName(),
                System.currentTimeMillis(),
                freezeTask,
                rooms
        );
        activeSessions.put(target.getUniqueId(), session);

var channel = plugin.getChannelRegistry().get("kont_log");
        if (channel != null) {
            channel.sendMessageEmbeds(EmbedFactory.createKontStartEmbed(staff.getName(), target.getName(), durationSec)).queue();
        }

        return true;
    }

    public boolean extendKont(UUID targetUuid, int extraMinutes) {
        ActiveKontSession session = activeSessions.get(targetUuid);
        if (session == null) return false;

        if (session.getExtensionCount() >= plugin.getConfigManager().getKontExtensionMaxCount()) {
            return false;
        }

        session.incrementExtension();
        session.getFreezeTask().extendTime(extraMinutes * 60);

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null) {
            target.sendMessage("§eKontrol süreniz §b" + extraMinutes + " dakika §euzatıldı.");
        }
        return true;
    }

    public void finishKont(UUID targetUuid, String outcome, String notes) {
        ActiveKontSession session = activeSessions.remove(targetUuid);
        if (session == null) return;

        session.getFreezeTask().cancel();

        Player target = Bukkit.getPlayer(targetUuid);
        int durationSeconds = (int) ((System.currentTimeMillis() - session.getStartTime()) / 1000);

        KontRecord record = new KontRecord(
                UUID.randomUUID().toString().substring(0, 8),
                session.getTargetUuid(),
                session.getTargetName(),
                session.getStaffUuid(),
                session.getStaffName(),
                outcome.toUpperCase(),
                session.getStartTime(),
                System.currentTimeMillis(),
                durationSeconds,
                notes
        );

        plugin.getStorageProvider().kontRecords().addRecord(record);
        plugin.getStorageProvider().flush();

switch (outcome.toLowerCase()) {
            case "temiz":
            case "clean":
                if (target != null && target.isOnline()) {
                    target.setWalkSpeed(0.2f);
                    target.setFlySpeed(0.1f);
                    target.clearTitle();
                    target.sendMessage(plugin.getLocaleManager().getPrefixed("kont.finished_clean", "&aKontrol tamamlandı, temiz çıktınız! Sabrınız için teşekkürler.", null));
                    
                    target.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
                }
                plugin.getScoreEngine().awardScore(session.getStaffUuid(), session.getStaffName(), "kont_clean", "Kontrol temiz tamamlandı: " + session.getTargetName());
                break;

            case "hile":
            case "hacks_banned":
                plugin.getPunishmentService().issuePunishment(
                        Bukkit.getOfflinePlayer(session.getTargetUuid()),
                        Bukkit.getPlayer(session.getStaffUuid()),
                        "Hile Kullanımı (Kontrol Tespiti)",
                        "BAN",
                        -1
                );
                plugin.getScoreEngine().awardScore(session.getStaffUuid(), session.getStaffName(), "kont_hacks_banned", "Hile tespit edildi ve banlandı: " + session.getTargetName());
                break;

            case "itiraf":
            case "confession":
                
                long fifteenDaysMillis = 15L * 24 * 60 * 60 * 1000;
                plugin.getPunishmentService().issuePunishment(
                        Bukkit.getOfflinePlayer(session.getTargetUuid()),
                        Bukkit.getPlayer(session.getStaffUuid()),
                        "Hile İtirafı (İndirimli Ceza)",
                        "BAN",
                        fifteenDaysMillis
                );
                plugin.getScoreEngine().awardScore(session.getStaffUuid(), session.getStaffName(), "kont_confession", "Hile itirafı alındı: " + session.getTargetName());
                break;
        }

discordRooms.scheduleCleanup(session.getDiscordRooms(), 60);

var channel = plugin.getChannelRegistry().get("kont_log");
        if (channel != null) {
            channel.sendMessageEmbeds(EmbedFactory.createKontEndEmbed(record)).queue();
        }
    }

    public void handleCombatQuit(Player player) {
        ActiveKontSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return;

        session.getFreezeTask().cancel();
        int durationSeconds = (int) ((System.currentTimeMillis() - session.getStartTime()) / 1000);

        KontRecord record = new KontRecord(
                UUID.randomUUID().toString().substring(0, 8),
                session.getTargetUuid(),
                session.getTargetName(),
                session.getStaffUuid(),
                session.getStaffName(),
                "COMBAT_QUIT",
                session.getStartTime(),
                System.currentTimeMillis(),
                durationSeconds,
                "Kontrol sırasında sunucudan çıkış yaptı (Combat-Quit)"
        );

        plugin.getStorageProvider().kontRecords().addRecord(record);
        plugin.getStorageProvider().flush();

plugin.getPunishmentService().issuePunishment(
                player,
                Bukkit.getPlayer(session.getStaffUuid()),
                "Kontrol Sırasında Çıkış Yapıldı (Hile İtirafı / Combat-Quit)",
                "BAN",
                -1
        );

        discordRooms.scheduleCleanup(session.getDiscordRooms(), 10);

        var channel = plugin.getChannelRegistry().get("kont_log");
        if (channel != null) {
            channel.sendMessageEmbeds(EmbedFactory.createKontEndEmbed(record)).queue();
        }
    }
}
