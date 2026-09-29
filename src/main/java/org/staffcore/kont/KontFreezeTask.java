package org.staffcore.kont;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.staffcore.StaffCorePlugin;

import java.time.Duration;
import java.util.UUID;

public class KontFreezeTask extends BukkitRunnable {
    private final StaffCorePlugin plugin;
    private final UUID targetUuid;
    private final Location freezeLoc;
    private int remainingSeconds;

    public KontFreezeTask(StaffCorePlugin plugin, UUID targetUuid, Location freezeLoc, int totalSeconds) {
        this.plugin = plugin;
        this.targetUuid = targetUuid;
        this.freezeLoc = freezeLoc;
        this.remainingSeconds = totalSeconds;
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(targetUuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        if (remainingSeconds <= 0) {
            cancel();
            return;
        }

        remainingSeconds--;

        // Keep player frozen at location
        if (player.getLocation().distanceSquared(freezeLoc) > 0.5) {
            player.teleport(freezeLoc);
        }

        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);
        player.setAllowFlight(false);

        // Update Title & ActionBar every second
        Title title = Title.title(
                Component.text("⚠️ KONTROLE ALINDINIZ ⚠️", NamedTextColor.RED),
                Component.text("Discord sesli odasına bağlanın ve ekrandan ayrılmayın!", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(2), Duration.ofMillis(500))
        );
        player.showTitle(title);

        int min = remainingSeconds / 60;
        int sec = remainingSeconds % 60;
        String formattedTime = String.format("%02d:%02d", min, sec);
        player.sendActionBar(Component.text("Kalan Süre: " + formattedTime, NamedTextColor.GOLD));
    }

    public void extendTime(int extraSeconds) {
        this.remainingSeconds += extraSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}
