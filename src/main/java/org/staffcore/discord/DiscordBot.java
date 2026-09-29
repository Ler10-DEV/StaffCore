package org.staffcore.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.staffcore.StaffCorePlugin;
import org.staffcore.config.ConfigManager;
import org.staffcore.discord.listeners.DiscordEventListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordBot {
    private final StaffCorePlugin plugin;
    private final ConfigManager configManager;
    private final Logger logger;
    private final ChannelRegistry channelRegistry = new ChannelRegistry();
    private final ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(2);
    private RateLimitGuard rateLimitGuard;
    private JDA jda;
    private GuildInitializer guildInitializer;

    public DiscordBot(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.logger = plugin.getLogger();
    }

    public void start() {
        if (!configManager.isDiscordEnabled()) {
            logger.info("Discord integration is disabled in config.yml.");
            return;
        }

        String token = configManager.getSecretsManager().getDiscordBotToken();
        if (token == null || token.trim().isEmpty() || token.startsWith("ENV:")) {
            logger.warning("Discord Bot Token is not configured. Discord features will be suspended until token is provided.");
            return;
        }

        try {
            this.rateLimitGuard = new RateLimitGuard(asyncExecutor, logger);
            this.rateLimitGuard.start(500);

            this.guildInitializer = new GuildInitializer(configManager, channelRegistry, logger);

            this.jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new DiscordEventListener(plugin))
                    .build();

            jda.awaitReady();

jda.updateCommands().addCommands(
                    Commands.slash("esle", "Minecraft hesabınızı eşleyin")
                            .addOption(OptionType.STRING, "kod", "Oyun içinden aldığınız 4 haneli kod (MC-XXXX)", true)
            ).queue();

            String guildId = configManager.getDiscordGuildId();
            if (guildId != null && !guildId.isEmpty()) {
                Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    guildInitializer.initializeGuild(guild);
                } else {
                    logger.warning("Discord Guild with ID " + guildId + " could not be found.");
                }
            }

            logger.info("Discord Bot connected as " + jda.getSelfUser().getName());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to initialize Discord Bot: " + e.getMessage());
        }
    }

    public void stop() {
        if (rateLimitGuard != null) {
            rateLimitGuard.stop();
        }
        if (jda != null) {
            try {
                jda.shutdown();
            } catch (Exception e) {
                logger.warning("Error during Discord shutdown: " + e.getMessage());
            }
        }
        asyncExecutor.shutdown();
    }

    public JDA getJda() {
        return jda;
    }

    public ChannelRegistry getChannelRegistry() {
        return channelRegistry;
    }

    public RateLimitGuard getRateLimitGuard() {
        return rateLimitGuard;
    }

    public ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }
}
