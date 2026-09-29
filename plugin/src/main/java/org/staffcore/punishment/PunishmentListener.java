package org.staffcore.punishment;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.Punishment;

import java.util.Optional;

public class PunishmentListener implements Listener {
    private final StaffCorePlugin plugin;

    public PunishmentListener(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        Optional<Punishment> activeBan = plugin.getPunishmentService().getActiveBan(event.getUniqueId());
        if (activeBan.isPresent()) {
            Punishment p = activeBan.get();
            String msg = "§cSunucudan Uzaklaştırıldınız!\n§7Sebep: §e" + p.getReason() + "\n§7Ceza No: §b" + p.getId() + "\n§7İtiraz: §fdiscord.gg/sunucu";
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text(msg));
        }
    }
}
