package org.staffcore.link;

import org.bukkit.entity.Player;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.LinkRecord;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AccountLinkService {
    private final StaffCorePlugin plugin;
    private final LinkCodeCache codeCache;

    public AccountLinkService(StaffCorePlugin plugin) {
        this.plugin = plugin;
        int ttl = plugin.getConfigManager().getLinkCodeTtlSeconds();
        String prefix = plugin.getConfigManager().getLinkCodePrefix();
        this.codeCache = new LinkCodeCache(ttl, prefix);
    }

    public String generateLinkCode(Player player) {
        return generateLinkCode(player.getUniqueId());
    }

    public String generateLinkCode(UUID uuid) {
        return codeCache.generateCode(uuid);
    }

    public Optional<UUID> consumeCode(String code) {
        return codeCache.consumeCode(code);
    }

    public Optional<LinkRecord> getLinkRecord(UUID uuid) {
        return plugin.getStorageProvider().linkedAccounts().findByUuid(uuid);
    }

    public Optional<LinkRecord> getLinkRecordByDiscord(String discordId) {
        return plugin.getStorageProvider().linkedAccounts().findByDiscordId(discordId);
    }

    public LinkCodeCache getCodeCache() {
        return codeCache;
    }
}
