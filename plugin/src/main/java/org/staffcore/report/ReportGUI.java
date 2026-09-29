package org.staffcore.report;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.staffcore.StaffCorePlugin;

import java.util.List;

public class ReportGUI implements InventoryHolder, Listener {
    private final StaffCorePlugin plugin;
    private final String targetPlayerName;
    private final NamespacedKey categoryKey;
    private Inventory inventory;

    public ReportGUI(StaffCorePlugin plugin, String targetPlayerName) {
        this.plugin = plugin;
        this.targetPlayerName = targetPlayerName;
        this.categoryKey = new NamespacedKey(plugin, "report_category");
    }

    public void open(Player player) {
        this.inventory = Bukkit.createInventory(this, 27, Component.text("Rapor: " + targetPlayerName));

        // Fill background with gray stained glass panes
        ItemStack bg = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), null, null);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, bg);
        }

        // Slot 11: Hile (KillAura, Speed, Fly vb.)
        inventory.setItem(11, createGuiItem(Material.DIAMOND_SWORD,
                Component.text("Hile & Yetkisiz Yazılım", NamedTextColor.RED, TextDecoration.BOLD),
                List.of(Component.text("KillAura, Fly, Speed, AutoClicker vb.", NamedTextColor.GRAY)), "Hile"));

        // Slot 12: Sohbet (Küfür, Hakaret, Reklam)
        inventory.setItem(12, createGuiItem(Material.WRITABLE_BOOK,
                Component.text("Sohbet İhlali", NamedTextColor.YELLOW, TextDecoration.BOLD),
                List.of(Component.text("Küfür, argo, hakaret, reklam.", NamedTextColor.GRAY)), "Sohbet"));

        // Slot 14: X-Ray & Maden Hilesi
        inventory.setItem(14, createGuiItem(Material.DIAMOND_PICKAXE,
                Component.text("X-Ray & Maden Hilesi", NamedTextColor.AQUA, TextDecoration.BOLD),
                List.of(Component.text("Şüpheli maden kazımı ve hilesi.", NamedTextColor.GRAY)), "X-Ray"));

        // Slot 15: Dupe & Bug Abuse
        inventory.setItem(15, createGuiItem(Material.CHEST,
                Component.text("Dupe & Oyun Hatası", NamedTextColor.GOLD, TextDecoration.BOLD),
                List.of(Component.text("Eşya kopyalama veya sunucu açığı kullanımı.", NamedTextColor.GRAY)), "Dupe"));

        // Slot 16: Diğer
        inventory.setItem(16, createGuiItem(Material.COMPASS,
                Component.text("Diğer Şüpheli Davranışlar", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD),
                List.of(Component.text("Diğer tüm kural ihlalleri.", NamedTextColor.GRAY)), "Diğer"));

        player.openInventory(inventory);
    }

    private ItemStack createGuiItem(Material mat, Component name, List<Component> lore, String category) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (lore != null) meta.lore(lore);
            if (category != null) {
                meta.getPersistentDataContainer().set(categoryKey, PersistentDataType.STRING, category);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    public NamespacedKey getCategoryKey() {
        return categoryKey;
    }
}
