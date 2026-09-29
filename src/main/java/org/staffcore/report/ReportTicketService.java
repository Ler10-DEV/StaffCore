package org.staffcore.report;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.staffcore.StaffCorePlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportTicketService implements Listener {
    private final StaffCorePlugin plugin;
    private final ReportNotifier notifier;
    private final Map<UUID, Long> reportCooldowns = new ConcurrentHashMap<>();
    private final AtomicInteger ticketSequence = new AtomicInteger(100);

    public record ReportTicket(int ticketId, UUID reporterUuid, String reporterName, String targetName, String category, long timestamp) {}
    private final Map<Integer, ReportTicket> activeTickets = new ConcurrentHashMap<>();

    public ReportTicketService(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.notifier = new ReportNotifier(plugin);
    }

    public boolean isOnCooldown(UUID playerUuid) {
        Long last = reportCooldowns.get(playerUuid);
        if (last == null) return false;
        long elapsedSec = (System.currentTimeMillis() - last) / 1000;
        return elapsedSec < plugin.getConfigManager().getConfigManagerReportCooldown();
    }

    public long getRemainingCooldown(UUID playerUuid) {
        Long last = reportCooldowns.get(playerUuid);
        if (last == null) return 0;
        long elapsedSec = (System.currentTimeMillis() - last) / 1000;
        return Math.max(0, plugin.getConfigManager().getConfigManagerReportCooldown() - elapsedSec);
    }

    public int createTicket(Player reporter, String targetName, String category, Location targetLoc) {
        reportCooldowns.put(reporter.getUniqueId(), System.currentTimeMillis());
        int id = ticketSequence.incrementAndGet();
        ReportTicket ticket = new ReportTicket(id, reporter.getUniqueId(), reporter.getName(), targetName, category, System.currentTimeMillis());
        activeTickets.put(id, ticket);

        notifier.notifyStaff(reporter.getName(), targetName, category, targetLoc);
        return id;
    }

    public ReportTicket getTicket(int ticketId) {
        return activeTickets.get(ticketId);
    }

    public void closeTicket(int ticketId, Player staff) {
        ReportTicket ticket = activeTickets.remove(ticketId);
        if (ticket != null) {
            
            if (staff != null) {
                plugin.getScoreEngine().awardScore(staff.getUniqueId(), staff.getName(), "report_resolved", "Rapor #" + ticketId + " çözüldü");
            }
            
            if (plugin.getConfigManager().isCsatEnabled()) {
                plugin.getCsatListener().sendCsatPrompt(ticket.reporterUuid(), ticketId, staff != null ? staff.getName() : "Yetkili");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ReportGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String category = clicked.getItemMeta().getPersistentDataContainer().get(gui.getCategoryKey(), PersistentDataType.STRING);
        if (category == null) return;

        Player target = org.bukkit.Bukkit.getPlayer(gui.getTargetPlayerName());
        Location targetLoc = target != null ? target.getLocation() : player.getLocation();

        player.closeInventory();
        int ticketId = createTicket(player, gui.getTargetPlayerName(), category, targetLoc);

        player.sendMessage(plugin.getLocaleManager().getPrefixed("report.created",
                "&a{player} hakkındaki raporunuz yetkililere iletildi (Takip No: #{ticketId}). Teşekkürler!",
                Map.of("player", gui.getTargetPlayerName(), "ticketId", String.valueOf(ticketId))));
    }
}
