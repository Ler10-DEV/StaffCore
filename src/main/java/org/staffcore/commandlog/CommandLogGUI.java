package org.staffcore.commandlog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.CommandEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CommandLogGUI implements InventoryHolder {
    private final StaffCorePlugin plugin;
    private final String targetPlayerName;
    private final UUID targetUuid;
    private Inventory inventory;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public CommandLogGUI(StaffCorePlugin plugin, String targetPlayerName, UUID targetUuid) {
        this.plugin = plugin;
        this.targetPlayerName = targetPlayerName;
        this.targetUuid = targetUuid;
    }

    public void open(Player staff) {
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Komut Geçmişi: " + targetPlayerName));
        List<CommandEntry> logs = plugin.getStorageProvider().commandLogs().getRecentCommands(targetUuid, 54);

        if (logs.isEmpty()) {
            ItemStack emptyItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = emptyItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("Kayıtlı Komut Yok", NamedTextColor.RED));
                emptyItem.setItemMeta(meta);
            }
            inventory.setItem(22, emptyItem);
        } else {
            for (int i = 0; i < logs.size() && i < 54; i++) {
                CommandEntry entry = logs.get(i);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text(entry.getCommand(), NamedTextColor.YELLOW));
                    meta.lore(List.of(
                            Component.text("Zaman: " + DATE_FORMAT.format(new Date(entry.getTimestamp())), NamedTextColor.GRAY),
                            Component.text("Konum: " + entry.getLocation(), NamedTextColor.DARK_GRAY)
                    ));
                    paper.setItemMeta(meta);
                }
                inventory.setItem(i, paper);
            }
        }

        staff.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
