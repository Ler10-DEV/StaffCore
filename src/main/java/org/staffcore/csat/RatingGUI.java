package org.staffcore.csat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

public class RatingGUI implements InventoryHolder, Listener {
    private final StaffCorePlugin plugin;
    private final String staffName;
    private final Integer ticketId; 
    private final NamespacedKey starKey;
    private Inventory inventory;

    public RatingGUI(StaffCorePlugin plugin, String staffName, Integer ticketId) {
        this.plugin = plugin;
        this.staffName = staffName;
        this.ticketId = ticketId;
        this.starKey = new NamespacedKey(plugin, "rating_stars");
    }

    public void open(Player player) {
        String titleText = staffName != null && !staffName.isEmpty()
                ? "⭐ Yetkiliyi Puanla: " + staffName
                : "⭐ Destek Hizmetini Puanla";

        this.inventory = Bukkit.createInventory(this, 27, Component.text(titleText, NamedTextColor.DARK_BLUE));

ItemStack bg = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), null, -1);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, bg);
        }

inventory.setItem(4, createGuiItem(Material.WRITABLE_BOOK,
                Component.text("⭐ Hizmet Değerlendirmesi", NamedTextColor.GOLD, TextDecoration.BOLD),
                List.of(
                        Component.text("Yetkili: ", NamedTextColor.GRAY).append(Component.text(staffName != null ? staffName : "Genel", NamedTextColor.AQUA)),
                        Component.text("Puanlayan: ", NamedTextColor.GRAY).append(Component.text(player.getName(), NamedTextColor.WHITE)),
                        Component.empty(),
                        Component.text("Lütfen aldığınız hizmete uygun", NamedTextColor.YELLOW),
                        Component.text("yıldızı aşağıdaki menüden seçiniz.", NamedTextColor.YELLOW)
                ), -1));

inventory.setItem(11, createGuiItem(Material.RED_CONCRETE,
                Component.text("★☆☆☆☆ (1 Yıldız)", NamedTextColor.RED, TextDecoration.BOLD),
                List.of(
                        Component.text("Derece: ", NamedTextColor.GRAY).append(Component.text("Çok Kötü", NamedTextColor.RED)),
                        Component.text("Hizmetten hiç memnun kalmadım.", NamedTextColor.DARK_GRAY),
                        Component.empty(),
                        Component.text("» Oylamak için tıkla!", NamedTextColor.YELLOW)
                ), 1));

inventory.setItem(12, createGuiItem(Material.ORANGE_CONCRETE,
                Component.text("★★☆☆☆ (2 Yıldız)", NamedTextColor.GOLD, TextDecoration.BOLD),
                List.of(
                        Component.text("Derece: ", NamedTextColor.GRAY).append(Component.text("Yetersiz / Kötü", NamedTextColor.GOLD)),
                        Component.text("Sorunum yeterince iyi çözülemedi.", NamedTextColor.DARK_GRAY),
                        Component.empty(),
                        Component.text("» Oylamak için tıkla!", NamedTextColor.YELLOW)
                ), 2));

inventory.setItem(13, createGuiItem(Material.YELLOW_CONCRETE,
                Component.text("★★★☆☆ (3 Yıldız)", NamedTextColor.YELLOW, TextDecoration.BOLD),
                List.of(
                        Component.text("Derece: ", NamedTextColor.GRAY).append(Component.text("Orta / Standart", NamedTextColor.YELLOW)),
                        Component.text("Ortalama bir destek deneyimiydi.", NamedTextColor.DARK_GRAY),
                        Component.empty(),
                        Component.text("» Oylamak için tıkla!", NamedTextColor.YELLOW)
                ), 3));

inventory.setItem(14, createGuiItem(Material.LIME_CONCRETE,
                Component.text("★★★★☆ (4 Yıldız)", NamedTextColor.GREEN, TextDecoration.BOLD),
                List.of(
                        Component.text("Derece: ", NamedTextColor.GRAY).append(Component.text("İyi & İlgili", NamedTextColor.GREEN)),
                        Component.text("Yetkili hızlı ve yardımcı oldu.", NamedTextColor.DARK_GRAY),
                        Component.empty(),
                        Component.text("» Oylamak için tıkla!", NamedTextColor.YELLOW)
                ), 4));

inventory.setItem(15, createGuiItem(Material.EMERALD,
                Component.text("★★★★★ (5 Yıldız)", NamedTextColor.AQUA, TextDecoration.BOLD),
                List.of(
                        Component.text("Derece: ", NamedTextColor.GRAY).append(Component.text("Mükemmel & Kusursuz", NamedTextColor.AQUA)),
                        Component.text("Harika bir ilgi ve anında çözüm!", NamedTextColor.DARK_GRAY),
                        Component.empty(),
                        Component.text("» Oylamak için tıkla!", NamedTextColor.YELLOW)
                ), 5));

inventory.setItem(22, createGuiItem(Material.BARRIER,
                Component.text("İptal Et & Kapat", NamedTextColor.DARK_RED, TextDecoration.BOLD),
                List.of(Component.text("Puanlama yapmadan pencereyi kapatır.", NamedTextColor.GRAY)), 0));

        player.openInventory(inventory);
    }

    private ItemStack createGuiItem(Material mat, Component name, List<Component> lore, int stars) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (lore != null) meta.lore(lore);
            if (stars >= 0) {
                meta.getPersistentDataContainer().set(starKey, PersistentDataType.INTEGER, stars);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getStaffName() {
        return staffName;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public NamespacedKey getStarKey() {
        return starKey;
    }
}
