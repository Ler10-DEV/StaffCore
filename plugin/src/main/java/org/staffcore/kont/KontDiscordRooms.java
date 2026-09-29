package org.staffcore.kont;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.staffcore.StaffCorePlugin;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class KontDiscordRooms {
    private final StaffCorePlugin plugin;
    private final Logger logger;

    public record ActiveRooms(Category category, VoiceChannel voiceChannel, TextChannel textChannel) {}

    public KontDiscordRooms(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public ActiveRooms createRooms(String staffName, String targetPlayerName, String targetDiscordId) {
        try {
            Guild guild = plugin.getDiscordBot().getJda() != null && plugin.getConfigManager().getDiscordGuildId() != null
                    ? plugin.getDiscordBot().getJda().getGuildById(plugin.getConfigManager().getDiscordGuildId())
                    : null;

            if (guild == null) return null;

            String staffRoleId = plugin.getConfigManager().getDiscordStaffRoleId();
            Role staffRole = (staffRoleId != null && !staffRoleId.isEmpty()) ? guild.getRoleById(staffRoleId) : null;

            Category category = guild.createCategory("kont-temp-" + targetPlayerName).complete();
            category.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)).queue();

            if (staffRole != null) {
                category.getManager().putRolePermissionOverride(staffRole.getIdLong(),
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK), null).queue();
            }

            if (targetDiscordId != null && !targetDiscordId.isEmpty()) {
                Member member = guild.getMemberById(targetDiscordId);
                if (member != null) {
                    category.getManager().putMemberPermissionOverride(member.getIdLong(),
                            EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK), null).queue();
                }
            }

            VoiceChannel voice = category.createVoiceChannel("[Kont] " + staffName + " - " + targetPlayerName).complete();
            TextChannel text = category.createTextChannel("[Kont-Chat] " + targetPlayerName).complete();

            text.sendMessage("🔍 **Kontrol Başlatıldı!**\n" +
                    "Şüpheli: **" + targetPlayerName + "**\n" +
                    "Yetkili: **" + staffName + "**\n" +
                    "Lütfen ses kanalına katılınız.").queue();

            return new ActiveRooms(category, voice, text);
        } catch (Exception e) {
            logger.warning("Failed to create temporary Discord Kont rooms: " + e.getMessage());
            return null;
        }
    }

    public void scheduleCleanup(ActiveRooms rooms, int delaySeconds) {
        if (rooms == null) return;
        plugin.getDiscordBot().getAsyncExecutor().schedule(() -> {
            try {
                if (rooms.voiceChannel() != null) rooms.voiceChannel().delete().queue(null, err -> {});
                if (rooms.textChannel() != null) rooms.textChannel().delete().queue(null, err -> {});
                if (rooms.category() != null) rooms.category().delete().queue(null, err -> {});
                logger.info("Temporary Kont Discord rooms cleaned up for " + rooms.category().getName());
            } catch (Exception e) {
                logger.warning("Error cleaning up Kont rooms: " + e.getMessage());
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }
}
