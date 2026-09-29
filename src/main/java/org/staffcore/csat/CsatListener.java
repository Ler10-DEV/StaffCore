package org.staffcore.csat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.staffcore.StaffCorePlugin;

import java.util.UUID;

public class CsatListener implements Listener {
    private final StaffCorePlugin plugin;
    private final RatingHandler ratingHandler;

    public CsatListener(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.ratingHandler = new RatingHandler(plugin);
    }

    public void sendCsatPrompt(UUID reporterUuid, int ticketId, String staffName) {
        Player player = Bukkit.getPlayer(reporterUuid);
        if (player == null || !player.isOnline()) return;

        ratingHandler.registerPending(ticketId, reporterUuid, staffName);

        Component prompt = Component.text("§b[StaffCore] §7Talebiniz sonuçlandı! Yetkili (", NamedTextColor.GRAY)
                .append(Component.text(staffName, NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(") hizmetini puanlamak için:\n", NamedTextColor.GRAY))
                .append(Component.text("  [ ⭐ YILDIZ MENÜSÜNÜ AÇ ] ", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("5 Yıldızlı Puanlama Menüsünü Açar", NamedTextColor.YELLOW)))
                        .clickEvent(ClickEvent.runCommand("/puanla " + ticketId)))
                .append(Component.text("\n  veya hızlı tıkla: ", NamedTextColor.DARK_GRAY))
                .append(Component.text("[★1] ", NamedTextColor.RED).hoverEvent(HoverEvent.showText(Component.text("1 Yıldız Ver (Çok Kötü)"))).clickEvent(ClickEvent.runCommand("/puanla " + ticketId + " 1")))
                .append(Component.text("[★2] ", NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(Component.text("2 Yıldız Ver (Kötü)"))).clickEvent(ClickEvent.runCommand("/puanla " + ticketId + " 2")))
                .append(Component.text("[★3] ", NamedTextColor.YELLOW).hoverEvent(HoverEvent.showText(Component.text("3 Yıldız Ver (Orta)"))).clickEvent(ClickEvent.runCommand("/puanla " + ticketId + " 3")))
                .append(Component.text("[★4] ", NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(Component.text("4 Yıldız Ver (İyi)"))).clickEvent(ClickEvent.runCommand("/puanla " + ticketId + " 4")))
                .append(Component.text("[★5]", NamedTextColor.AQUA).hoverEvent(HoverEvent.showText(Component.text("5 Yıldız Ver (Mükemmel)"))).clickEvent(ClickEvent.runCommand("/puanla " + ticketId + " 5")));

        player.sendMessage(prompt);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RatingGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        Integer stars = item.getItemMeta().getPersistentDataContainer().get(gui.getStarKey(), PersistentDataType.INTEGER);
        if (stars == null) return;

        player.closeInventory();

        if (stars <= 0) {
            player.sendMessage("§e[StaffCore] §7Puanlama iptal edildi.");
            return;
        }

        if (gui.getTicketId() != null) {
            ratingHandler.submitTicketVote(player, gui.getTicketId(), stars, null);
        } else if (gui.getStaffName() != null) {
            ratingHandler.submitDirectStaffVote(player, gui.getStaffName(), stars, null);
        }
    }

    public RatingHandler getRatingHandler() {
        return ratingHandler;
    }
}
