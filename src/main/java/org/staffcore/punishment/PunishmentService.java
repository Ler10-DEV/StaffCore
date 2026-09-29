package org.staffcore.punishment;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.staffcore.StaffCorePlugin;
import org.staffcore.discord.EmbedFactory;
import org.staffcore.storage.model.Punishment;
import org.staffcore.storage.model.PunishmentStatus;

import java.util.Optional;
import java.util.UUID;

public class PunishmentService {
    private final StaffCorePlugin plugin;

    public PunishmentService(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    public Punishment issuePunishment(OfflinePlayer target, Player staff, String reason, String type, long durationMillis) {
        String id = plugin.getStorageProvider().punishments().generateNextId();
        UUID staffUuid = staff != null ? staff.getUniqueId() : UUID.nameUUIDFromBytes("CONSOLE".getBytes());
        String staffName = staff != null ? staff.getName() : "CONSOLE";

        Punishment punishment = new Punishment(
                id,
                target.getUniqueId(),
                target.getName() != null ? target.getName() : "Unknown",
                staffUuid,
                staffName,
                reason,
                type,
                System.currentTimeMillis(),
                durationMillis,
                PunishmentStatus.PENDING_PROOF,
                null,
                null,
                0
        );

        plugin.getStorageProvider().punishments().save(punishment);
        plugin.getStorageProvider().flush();

if (target.isOnline()) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                onlineTarget.kick(Component.text("§cSunucudan Uzaklaştırıldınız!\n§7Sebep: §e" + reason + "\n§7Ceza No: §b" + id));
            }
        }

TextChannel channel = plugin.getChannelRegistry().get("ceza_log");
        if (channel != null) {
            String customId = "proof:add:" + id + ":" + staffUuid;
            channel.sendMessageEmbeds(EmbedFactory.createPunishmentEmbed(punishment))
                    .setActionRow(
                            Button.primary(customId, "📎 Kanıt Ekle (Add Proof)")
                    ).queue();
        }

String broadcast = "§8[§bStaffCore§8] §c" + target.getName() + " §7adlı oyuncuya ceza uygulandı: §e" + reason + " §8(ID: " + id + ")";
        Bukkit.broadcast(Component.text(broadcast), "staff.report.view");

        return punishment;
    }

    public Optional<Punishment> getActiveBan(UUID targetUuid) {
        return plugin.getStorageProvider().punishments().findByTarget(targetUuid).stream()
                .filter(p -> "BAN".equalsIgnoreCase(p.getType()) && !p.isExpired() && p.getStatus() != PunishmentStatus.REVOKED)
                .findFirst();
    }
}
