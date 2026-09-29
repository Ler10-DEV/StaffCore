package org.staffcore.discord.listeners;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.staffcore.StaffCorePlugin;
import org.staffcore.discord.EmbedFactory;
import org.staffcore.storage.model.LinkRecord;
import org.staffcore.storage.model.Punishment;
import org.staffcore.storage.model.PunishmentStatus;

import java.util.Optional;
import java.util.UUID;

public class DiscordEventListener extends ListenerAdapter {
    private final StaffCorePlugin plugin;

    public DiscordEventListener(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("esle".equals(event.getName())) {
            var option = event.getOption("kod");
            if (option == null) {
                event.reply("❌ Lütfen eşleme kodunuzu giriniz!").setEphemeral(true).queue();
                return;
            }
            String code = option.getAsString().trim().toUpperCase();
            Optional<UUID> targetUuid = plugin.getAccountLinkService().consumeCode(code);

            if (targetUuid.isEmpty()) {
                event.reply("❌ Eşleme kodu geçersiz veya süresi dolmuş!").setEphemeral(true).queue();
                return;
            }

            UUID uuid = targetUuid.get();
            String discordId = event.getUser().getId();
            String discordTag = event.getUser().getName();

Optional<LinkRecord> existingDiscord = plugin.getStorageProvider().linkedAccounts().findByDiscordId(discordId);
            if (existingDiscord.isPresent() && !existingDiscord.get().getUuid().equals(uuid)) {
                event.reply("❌ Bu Discord hesabı zaten başka bir Minecraft hesabına bağlı!").setEphemeral(true).queue();
                return;
            }

            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            if (playerName == null) playerName = "Player";

            boolean isStaff = false;
            Player online = Bukkit.getPlayer(uuid);
            if (online != null && online.hasPermission("staff.use")) {
                isStaff = true;
            }

            LinkRecord record = new LinkRecord(uuid, playerName, discordId, discordTag, isStaff, System.currentTimeMillis());
            plugin.getStorageProvider().linkedAccounts().save(record);
            plugin.getStorageProvider().flush();

            event.reply("✅ Başarılı! Minecraft hesabınız (**" + playerName + "**) Discord hesabınızla eşlendi.").setEphemeral(true).queue();

            if (online != null) {
                online.sendMessage(plugin.getLocaleManager().getPrefixed("link.success", "&aHesabınız başarıyla eşlendi!", null));
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String customId = event.getComponentId();

        if (customId.startsWith("2fa:approve:")) {
            String uuidStr = customId.substring("2fa:approve:".length());
            try {
                UUID uuid = UUID.fromString(uuidStr);
                plugin.getStaffGatekeeper().approve(uuid, event.getUser().getName());
                event.editMessageEmbeds(EmbedFactory.create2FAResult(event.getUser().getName(), true))
                        .setComponents()
                        .queue();
            } catch (Exception e) {
                event.reply("İşlem gerçekleştirilemedi: " + e.getMessage()).setEphemeral(true).queue();
            }
        } else if (customId.startsWith("2fa:deny:")) {
            String uuidStr = customId.substring("2fa:deny:".length());
            try {
                UUID uuid = UUID.fromString(uuidStr);
                plugin.getStaffGatekeeper().deny(uuid);
                event.editMessageEmbeds(EmbedFactory.create2FAResult(event.getUser().getName(), false))
                        .setComponents()
                        .queue();
            } catch (Exception e) {
                event.reply("İşlem gerçekleştirilemedi: " + e.getMessage()).setEphemeral(true).queue();
            }
        } else if (customId.startsWith("proof:add:")) {
            
            String[] parts = customId.split(":");
            if (parts.length >= 4) {
                String punishmentId = parts[2];
                String staffUuidStr = parts[3];

try {
                    UUID staffUuid = UUID.fromString(staffUuidStr);
                    Optional<LinkRecord> link = plugin.getStorageProvider().linkedAccounts().findByUuid(staffUuid);
                    if (link.isPresent() && !link.get().getDiscordId().equals(event.getUser().getId())) {
                        event.reply("❌ Bu cezaya yalnızca cezayı uygulayan yetkili kanıt ekleyebilir!").setEphemeral(true).queue();
                        return;
                    }
                } catch (Exception ignored) {}

                TextInput proofInput = TextInput.create("proof_url", "Kanıt URL / Linki", TextInputStyle.SHORT)
                        .setPlaceholder("https://imgur.com/... veya https://youtube.com/...")
                        .setRequired(true)
                        .build();

                TextInput noteInput = TextInput.create("review_note", "Yetkili İnceleme Notu", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Örn: 0:45 saniyede bariz KillAura tespiti yapıldı.")
                        .setRequired(false)
                        .build();

                Modal modal = Modal.create("proof_modal:" + punishmentId, "Kanıt Yükle: " + punishmentId)
                        .addActionRow(proofInput)
                        .addActionRow(noteInput)
                        .build();

                event.replyModal(modal).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (modalId.startsWith("proof_modal:")) {
            String punishmentId = modalId.substring("proof_modal:".length());
            String proofUrl = event.getValue("proof_url") != null ? event.getValue("proof_url").getAsString() : "";
            String reviewNote = event.getValue("review_note") != null ? event.getValue("review_note").getAsString() : "";

            Optional<Punishment> optP = plugin.getStorageProvider().punishments().findById(punishmentId);
            if (optP.isPresent()) {
                Punishment p = optP.get();
                p.setProofUrl(proofUrl);
                p.setReviewNote(reviewNote);
                p.setStatus(PunishmentStatus.VERIFIED);
                p.setVerifiedAt(System.currentTimeMillis());

                plugin.getStorageProvider().punishments().save(p);
                plugin.getStorageProvider().flush();

if (p.getStaffUuid() != null) {
                    plugin.getScoreEngine().awardScore(p.getStaffUuid(), p.getStaffName(), "punishment_verified", "Ceza " + p.getId() + " kanıtı doğrulandı");
                }

                event.reply("✅ Ceza (**" + punishmentId + "**) için kanıt başarıyla kaydedildi ve onaylandı!").setEphemeral(true).queue();

var channel = plugin.getChannelRegistry().get("ceza_log");
                if (channel != null) {
                    channel.sendMessageEmbeds(EmbedFactory.createPunishmentEmbed(p)).queue();
                }
            } else {
                event.reply("❌ Ceza kaydı bulunamadı: " + punishmentId).setEphemeral(true).queue();
            }
        }
    }
}
