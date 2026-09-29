package org.staffcore.auth;

import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

public class QuarantineState {
    private final UUID uuid;
    private final String playerName;
    private final String ip;
    private final Location joinLocation;
    private final long joinTime;
    private ScheduledFuture<?> timeoutTask;

    public QuarantineState(UUID uuid, String playerName, String ip, Location joinLocation, long joinTime) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.ip = ip;
        this.joinLocation = joinLocation;
        this.joinTime = joinTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getIp() {
        return ip;
    }

    public Location getJoinLocation() {
        return joinLocation;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public ScheduledFuture<?> getTimeoutTask() {
        return timeoutTask;
    }

    public void setTimeoutTask(ScheduledFuture<?> timeoutTask) {
        this.timeoutTask = timeoutTask;
    }
}
