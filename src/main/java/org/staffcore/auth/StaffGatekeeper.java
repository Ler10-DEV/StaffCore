package org.staffcore.auth;


import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.staffcore.StaffCorePlugin;
import org.staffcore.discord.EmbedFactory;
import org.staffcore.storage.model.LinkRecord;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StaffGatekeeper implements Listener {
    private final StaffCorePlugin plugin;
    private final Map<UUID, QuarantineState> activeQuarantines = new ConcurrentHashMap<>();

    public StaffGatekeeper(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isQuarantined(UUID uuid) {
        return activeQuarantines.containsKey(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getConfigManager().isTwoFactorEnabled()) return;

        // Check if player is a linked staff member or has staff permissions
        boolean hasPerm = player.hasPermission("staff.use") || player.hasPermission("staff.admin");
        Optional<LinkRecord> link = plugin.getStorageProvider().linkedAccounts().findByUuid(player.getUniqueId());

        if (hasPerm || (link.isPresent() && link.get().isStaff())) {
            startQuarantine(player, link.orElse(null));
        }
    }

    private void startQuarantine(Player player, LinkRecord link) {
        UUID uuid = player.getUniqueId();
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "127.0.0.1";
        QuarantineState state = new QuarantineState(uuid, player.getName(), ip, player.getLocation(), System.currentTimeMillis());

        activeQuarantines.put(uuid, state);

        // Apply visual & movement lock
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200, false, false, false));
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);
        player.setInvulnerable(true);
        player.setCollidable(false);

        // Send screen title
        Title title = Title.title(
                Component.text("§c§lYETKİLİ GİRİŞ KALKANI"),
                Component.text("§eLütfen Discord üzerinden 2FA girişinizi onaylayın."),
                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(60), Duration.ofSeconds(1))
        );
        player.showTitle(title);
        player.sendMessage(plugin.getLocaleManager().getPrefixed("auth.quarantine_subtitle", "&eLütfen Discord üzerinden 2FA girişinizi onaylayın.", null));

        // Schedule timeout fallback (60s)
        int timeoutSec = plugin.getConfigManager().getTwoFactorTimeoutSeconds();
        ScheduledFuture<?> timeoutTask = plugin.getDiscordBot().getAsyncExecutor().schedule(() -> {
            deny(uuid);
        }, timeoutSec, TimeUnit.SECONDS);
        state.setTimeoutTask(timeoutTask);

        // Send Discord notification to channel
        TextChannel channel = plugin.getChannelRegistry().get("yetkili_onay");
        if (channel != null) {
            channel.sendMessageEmbeds(EmbedFactory.create2FAPrompt(player.getName(), uuid, ip))
                    .setActionRow(
                            Button.success("2fa:approve:" + uuid, "Onayla (Approve)"),
                            Button.danger("2fa:deny:" + uuid, "Reddet (Deny)")
                    ).queue();
        }

        // Send direct message to linked Discord user if available
        if (link != null && plugin.getDiscordBot().getJda() != null) {
            plugin.getDiscordBot().getJda().retrieveUserById(link.getDiscordId()).queue(user -> {
                user.openPrivateChannel().queue(dm -> {
                    dm.sendMessageEmbeds(EmbedFactory.create2FAPrompt(player.getName(), uuid, ip))
                            .setActionRow(
                                    Button.success("2fa:approve:" + uuid, "Onayla"),
                                    Button.danger("2fa:deny:" + uuid, "Reddet")
                            ).queue();
                });
            }, err -> {});
        }
    }

    public void approve(UUID uuid, String approvedBy) {
        QuarantineState state = activeQuarantines.remove(uuid);
        if (state != null && state.getTimeoutTask() != null) {
            state.getTimeoutTask().cancel(false);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                player.setInvulnerable(false);
                player.setCollidable(true);
                player.clearTitle();

                player.sendMessage(plugin.getLocaleManager().getPrefixed("auth.approved", "&a2FA Doğrulandı! Hoş geldiniz.", null));
            }
        });
    }

    public void deny(UUID uuid) {
        QuarantineState state = activeQuarantines.remove(uuid);
        if (state != null && state.getTimeoutTask() != null) {
            state.getTimeoutTask().cancel(false);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.kick(Component.text("StaffCore: 2FA Doğrulaması Başarısız veya Zaman Aşımına Uğradı!"));
            }
        });
    }

    // Cancellation Event Handlers
    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (isQuarantined(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (isQuarantined(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLocaleManager().getPrefixed("auth.quarantine_subtitle", "&eLütfen önce 2FA doğrulamasını tamamlayın.", null));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (isQuarantined(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLocaleManager().getPrefixed("auth.quarantine_subtitle", "&eLütfen önce 2FA doğrulamasını tamamlayın.", null));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player p && isQuarantined(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        if (isQuarantined(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event) {
        if (isQuarantined(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (isQuarantined(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        QuarantineState state = activeQuarantines.remove(event.getPlayer().getUniqueId());
        if (state != null && state.getTimeoutTask() != null) {
            state.getTimeoutTask().cancel(false);
        }
    }
}
