package org.staffcore.kont;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.staffcore.StaffCorePlugin;

import java.util.UUID;

public class CombatQuitGuard implements Listener {
    private final StaffCorePlugin plugin;

    public CombatQuitGuard(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.getKontManager().isInKont(uuid)) {
            plugin.getLogger().warning("Player " + player.getName() + " quit during active Kont! Applying Combat-Quit punishment.");
            plugin.getKontManager().handleCombatQuit(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player p && plugin.getKontManager().isInKont(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p && plugin.getKontManager().isInKont(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getKontManager().isInKont(player.getUniqueId())) {
            
            event.setCancelled(true);
            String format = "§8[§cKONT-CHAT§8] §e" + player.getName() + ": §f" + event.getMessage();
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("staff.kont") || p.hasPermission("staff.use"))
                    .forEach(p -> p.sendMessage(format));
            player.sendMessage(format);

            var channel = plugin.getChannelRegistry().get("kont_log");
            if (channel != null) {
                channel.sendMessage("💬 `[Kont-Chat]` **" + player.getName() + "**: " + event.getMessage()).queue();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (plugin.getKontManager().isInKont(player.getUniqueId())) {
            
            if (!event.getMessage().startsWith("/kont") && !event.getMessage().startsWith("/r ")) {
                event.setCancelled(true);
                player.sendMessage("§cKontrol altındayken komut kullanamazsınız!");
            }
        }
    }
}
