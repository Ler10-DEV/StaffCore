package org.staffcore.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Map;

public class LocaleManager {
    private final Plugin plugin;
    private FileConfiguration messages;
    private String prefix = "&8[&bStaffCore&8] &7";

    public LocaleManager(Plugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
        this.prefix = messages.getString("prefix", "&8[&bStaffCore&8] &7");
    }

    public String getRaw(String key, String def) {
        return messages.getString(key, def);
    }

    public String get(String key, String def, Map<String, String> placeholders) {
        String msg = messages.getString(key, def);
        if (msg == null) return "";
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String getPrefixed(String key, String def, Map<String, String> placeholders) {
        return ChatColor.translateAlternateColorCodes('&', prefix) + get(key, def, placeholders);
    }

    public Component getComponent(String key, String def, Map<String, String> placeholders) {
        String formatted = getPrefixed(key, def, placeholders);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(formatted);
    }
}
