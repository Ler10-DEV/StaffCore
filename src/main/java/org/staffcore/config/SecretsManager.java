package org.staffcore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages sensitive credentials and tokens with resolution order:
 * 1. System Environment Variables (ENV:VAR_NAME)
 * 2. secrets.yml file
 * 3. config.yml default values
 *
 * Also provides high-security masking of sensitive values for log outputs.
 */
public class SecretsManager {
    private final Plugin plugin;
    private FileConfiguration secretsConfig;
    private static final Pattern MASK_PATTERN = Pattern.compile("(?i)(token|password|secret|pass|key)\\s*[:=]\\s*['\"]?([^'\"\\s]+)['\"]?");

    public SecretsManager(Plugin plugin) {
        this.plugin = plugin;
        loadSecrets();
    }

    public void loadSecrets() {
        File secretsFile = new File(plugin.getDataFolder(), "secrets.yml");
        if (secretsFile.exists()) {
            this.secretsConfig = YamlConfiguration.loadConfiguration(secretsFile);
        } else {
            this.secretsConfig = new YamlConfiguration();
        }
    }

    /**
     * Resolves a configuration value checking for ENV prefix or secrets.yml overrides.
     */
    public String resolveSecret(String path, String defaultValue) {
        // 1. Check secrets.yml
        String val = secretsConfig != null ? secretsConfig.getString(path) : null;

        // 2. Check config.yml if not found
        if (val == null || val.trim().isEmpty()) {
            val = plugin.getConfig().getString(path, defaultValue);
        }

        if (val == null) return defaultValue;

        // 3. Resolve ENV:VAR_NAME pattern
        if (val.startsWith("ENV:")) {
            String envVar = val.substring(4).trim();
            String envVal = System.getenv(envVar);
            if (envVal != null && !envVal.trim().isEmpty()) {
                return envVal;
            }
            return defaultValue;
        }

        return val;
    }

    public String getDiscordBotToken() {
        return resolveSecret("discord.bot_token", "");
    }

    public String getRedisPassword() {
        return resolveSecret("storage.redis_password", "");
    }

    public String getDatabasePassword() {
        return resolveSecret("storage.mysql_password", "");
    }

    /**
     * Replaces any credential matching token/secret/password with asterisks for safe logging.
     */
    public static String maskSecrets(String input) {
        if (input == null) return null;
        Matcher matcher = MASK_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            matcher.appendReplacement(sb, key + "=********");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
