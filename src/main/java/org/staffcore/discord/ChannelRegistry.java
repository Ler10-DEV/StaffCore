package org.staffcore.discord;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelRegistry {
    private final Map<String, TextChannel> channels = new ConcurrentHashMap<>();

    public void register(String key, TextChannel channel) {
        if (key != null && channel != null) {
            channels.put(key, channel);
        }
    }

    public TextChannel get(String key) {
        return channels.get(key);
    }

    public boolean has(String key) {
        return channels.containsKey(key);
    }

    public void clear() {
        channels.clear();
    }
}
