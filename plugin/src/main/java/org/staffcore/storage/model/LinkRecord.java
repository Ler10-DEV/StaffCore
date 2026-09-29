package org.staffcore.storage.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a linked account record pairing a Minecraft UUID with a Discord user ID.
 */
public class LinkRecord {
    private final UUID uuid;
    private final String playerName;
    private final String discordId;
    private final String discordTag;
    private final boolean isStaff;
    private final long linkedAt;

    public LinkRecord(UUID uuid, String playerName, String discordId, String discordTag, boolean isStaff, long linkedAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.discordId = discordId;
        this.discordTag = discordTag;
        this.isStaff = isStaff;
        this.linkedAt = linkedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDiscordId() {
        return discordId;
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public boolean isStaff() {
        return isStaff;
    }

    public long getLinkedAt() {
        return linkedAt;
    }

    public LinkRecord withStaff(boolean staff) {
        return new LinkRecord(this.uuid, this.playerName, this.discordId, this.discordTag, staff, this.linkedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkRecord that)) return false;
        return isStaff == that.isStaff && linkedAt == that.linkedAt && Objects.equals(uuid, that.uuid) && Objects.equals(playerName, that.playerName) && Objects.equals(discordId, that.discordId) && Objects.equals(discordTag, that.discordTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, playerName, discordId, discordTag, isStaff, linkedAt);
    }
}
