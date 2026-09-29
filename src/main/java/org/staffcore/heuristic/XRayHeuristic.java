package org.staffcore.heuristic;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.staffcore.StaffCorePlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class XRayHeuristic implements Listener {
    private final StaffCorePlugin plugin;
    private final AlarmDispatcher alarmDispatcher;
    private final Map<UUID, Deque<Long>> playerOreMinedTimestamps = new ConcurrentHashMap<>();

    private static final Set<Material> WATCHED_ORES = Set.of(
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.ANCIENT_DEBRIS,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE
    );

    public XRayHeuristic(StaffCorePlugin plugin, AlarmDispatcher alarmDispatcher) {
        this.plugin = plugin;
        this.alarmDispatcher = alarmDispatcher;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isXRayEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("staff.use") || player.isOp()) {
            return;
        }

        Block block = event.getBlock();
        if (!WATCHED_ORES.contains(block.getType())) {
            return;
        }

        String worldName = block.getWorld().getName();
        if (plugin.getConfigManager().getXRayExcludedWorlds().contains(worldName)) {
            return;
        }

        int y = block.getY();
        if (y < plugin.getConfigManager().getXRayMinY() || y > plugin.getConfigManager().getXRayMaxY()) {
            
            return;
        }

        long now = System.currentTimeMillis();
        int windowSec = plugin.getConfigManager().getXRayWindowSeconds();
        long windowMillis = windowSec * 1000L;

        Deque<Long> timestamps = playerOreMinedTimestamps.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentLinkedDeque<>());
        timestamps.addLast(now);

while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > windowMillis) {
            timestamps.pollFirst();
        }

        int count = timestamps.size();
        int threshold = plugin.getConfigManager().getXRayThreshold();

        if (count >= threshold) {
            
            Location loc = block.getLocation();
            int ping = player.getPing();
            String detail = String.format("Son %d saniyede %d değerli maden kırıldı! Eşik: %d/dk. Konum: [%s, X:%.0f Y:%d Z:%.0f] Ping: %dms",
                    windowSec, count, threshold, worldName, loc.getX(), y, loc.getZ(), ping);

            alarmDispatcher.dispatchAlarm("X-Ray Sezgisel Tespiti", player.getName(), detail);
            timestamps.clear(); 
        }
    }
}
