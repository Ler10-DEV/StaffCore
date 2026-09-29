package org.staffcore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.staffcore.storage.StorageType;

import java.nio.file.Path;
import java.util.List;

public class ConfigManager {
    private final Plugin plugin;
    private FileConfiguration config;
    private final SecretsManager secretsManager;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.secretsManager = new SecretsManager(plugin);
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        this.secretsManager.loadSecrets();
    }

    public SecretsManager getSecretsManager() {
        return secretsManager;
    }

    // Storage Settings
    public StorageType getStorageType() {
        String typeStr = config.getString("storage.type", "json");
        return StorageType.fromString(typeStr);
    }

    public int getFlushIntervalSeconds() {
        return config.getInt("storage.flush_interval_seconds", 30);
    }

    public boolean isBackupEnabled() {
        return config.getBoolean("storage.backup.enabled", true);
    }

    public int getBackupIntervalHours() {
        return config.getInt("storage.backup.interval_hours", 24);
    }

    public int getBackupRetentionDays() {
        return config.getInt("storage.backup.retention_days", 7);
    }

    public Path getDataDir() {
        // Allow environment variable override for test isolation
        String envDir = System.getenv("STAFFCORE_DATA_DIR");
        String configuredDir = config.getString("storage.json.data_dir", "data");
        String finalDir = (envDir != null && !envDir.trim().isEmpty()) ? envDir : configuredDir;
        return plugin.getDataFolder().toPath().resolve(finalDir);
    }

    public Path getBackupDir() {
        String dest = config.getString("storage.backup.destination", "backups");
        return plugin.getDataFolder().toPath().resolve(dest);
    }

    public boolean isPrettyPrintJson() {
        return config.getBoolean("storage.json.pretty_print", false);
    }

    // Discord Settings
    public boolean isDiscordEnabled() {
        return config.getBoolean("discord.enabled", true);
    }

    public String getDiscordGuildId() {
        return config.getString("discord.guild_id", "");
    }

    public String getDiscordStaffRoleId() {
        return config.getString("discord.staff_role_id", "");
    }

    public String getDiscordChannel(String key) {
        return config.getString("discord.channels." + key, "auto");
    }

    // Auth & 2FA Settings
    public boolean isTwoFactorEnabled() {
        return config.getBoolean("auth.two_factor_enabled", true);
    }

    public int getTwoFactorTimeoutSeconds() {
        return config.getInt("auth.timeout_seconds", 60);
    }

    // Report Settings
    public int getConfigManagerReportCooldown() {
        return config.getInt("report.cooldown_seconds", 60);
    }

    // Link Settings
    public int getLinkCodeTtlSeconds() {
        return config.getInt("link.code_ttl_seconds", 300);
    }

    public String getLinkCodePrefix() {
        return config.getString("link.code_prefix", "MC-");
    }

    // Kont Settings
    public int getKontDurationSeconds() {
        return config.getInt("kont.duration_seconds", 300);
    }

    public int getKontExtensionMaxCount() {
        return config.getInt("kont.extension_max_count", 15);
    }

    public boolean isCombatQuitBanEnabled() {
        return config.getBoolean("kont.combat_quit_ban", true);
    }

    // Heuristics
    public boolean isXRayEnabled() {
        return config.getBoolean("heuristic.xray.enabled", true);
    }

    public int getXRayWindowSeconds() {
        return config.getInt("heuristic.xray.window_seconds", 60);
    }

    public int getXRayThreshold() {
        return config.getInt("heuristic.xray.threshold", 12);
    }

    public int getXRayMinY() {
        return config.getInt("heuristic.xray.min_y", 5);
    }

    public int getXRayMaxY() {
        return config.getInt("heuristic.xray.max_y", 60);
    }

    public List<String> getXRayExcludedWorlds() {
        return config.getStringList("heuristic.xray.excluded_worlds");
    }

    public boolean isChatFilterEnabled() {
        return config.getBoolean("heuristic.chat_filter.enabled", true);
    }

    public List<String> getChatFilterPatterns() {
        return config.getStringList("heuristic.chat_filter.patterns");
    }

    public List<String> getCommandMaskPatterns() {
        return config.getStringList("command_logger.mask_patterns");
    }

    // Score
    public int getBaseScore() {
        return config.getInt("score.base_score", 100);
    }

    public int getLowScoreThreshold() {
        return config.getInt("score.low_score_threshold", 50);
    }

    public int getScoreDelta(String actionKey, int fallback) {
        return config.getInt("score.matrix." + actionKey, fallback);
    }

    public boolean isCsatEnabled() {
        return config.getBoolean("csat.enabled", true);
    }
}
