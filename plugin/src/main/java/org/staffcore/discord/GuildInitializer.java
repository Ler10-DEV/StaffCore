package org.staffcore.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.staffcore.config.ConfigManager;

import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Logger;

public class GuildInitializer {
    private final ConfigManager configManager;
    private final ChannelRegistry channelRegistry;
    private final Logger logger;

    private static final Map<String, String> REQUIRED_CHANNELS = Map.of(
            "yetkili_onay", "🔐-yetkili-onay",
            "komut_log", "💻-komut-log",
            "raporlar", "🚨-raporlar",
            "ceza_log", "🔨-ceza-log",
            "kont_log", "🔍-kont-log",
            "guvenlik_alarmlari", "⚠️-guvenlik-alarmlari",
            "haftalik_karne", "📊-haftalik-karne",
            "hesap_esle", "🔗-hesap-esle"
    );

    public GuildInitializer(ConfigManager configManager, ChannelRegistry channelRegistry, Logger logger) {
        this.configManager = configManager;
        this.channelRegistry = channelRegistry;
        this.logger = logger;
    }

    public void initializeGuild(Guild guild) {
        if (guild == null) return;
        logger.info("Initializing StaffCore channel tree for Guild: " + guild.getName());

        String staffRoleId = configManager.getDiscordStaffRoleId();
        Role staffRole = (staffRoleId != null && !staffRoleId.isEmpty()) ? guild.getRoleById(staffRoleId) : null;

        Category staffCategory = guild.getCategoriesByName("STAFFCORE", true).stream().findFirst()
                .orElseGet(() -> guild.createCategory("STAFFCORE").complete());

        for (Map.Entry<String, String> entry : REQUIRED_CHANNELS.entrySet()) {
            String key = entry.getKey();
            String name = entry.getValue();
            String configured = configManager.getDiscordChannel(key);

            TextChannel channel = null;
            if (!"auto".equalsIgnoreCase(configured) && configured != null && !configured.isEmpty()) {
                channel = guild.getTextChannelById(configured);
            }

            if (channel == null) {
                channel = guild.getTextChannelsByName(name, true).stream().findFirst().orElse(null);
            }

            if (channel == null) {
                var action = staffCategory.createTextChannel(name);
                if (staffRole != null && !key.equals("hesap_esle")) {
                    // Make private to staff
                    action = action.addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                            .addRolePermissionOverride(staffRole.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null);
                }
                channel = action.complete();
                logger.info("Created Discord channel: #" + name);
            }

            channelRegistry.register(key, channel);
        }

        logger.info("StaffCore Discord channel tree verified.");
    }
}
