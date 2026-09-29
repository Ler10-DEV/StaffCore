package org.staffcore.kont;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.staffcore.StaffCorePlugin;

public class KontCommandHandler implements CommandExecutor {
    private final StaffCorePlugin plugin;

    public KontCommandHandler(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("staff.kont") && !sender.isOp()) {
            sender.sendMessage(plugin.getLocaleManager().getPrefixed("no_permission", "&cBu komutu kullanmak için yetkiniz yok!", null));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cKullanım: /kont <baslat|bitir|uzat> <oyuncu> [parametre]");
            return true;
        }

        String sub = args[0].toLowerCase();
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if ("baslat".equals(sub)) {
            if (!(sender instanceof Player staff)) {
                sender.sendMessage("§cBu işlem yalnızca yetkili oyuncu tarafından yapılabilir.");
                return true;
            }
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§cOyuncu bulunamadı veya çevrimdışı: " + targetName);
                return true;
            }
            if (plugin.getKontManager().startKont(staff, target)) {
                sender.sendMessage("§a" + target.getName() + " kontrol altına alındı.");
            } else {
                sender.sendMessage("§cBu oyuncu zaten kontrol altında!");
            }
            return true;
        }

        if ("bitir".equals(sub)) {
            if (args.length < 3) {
                sender.sendMessage("§cKullanım: /kont bitir <oyuncu> <temiz|hile|itiraf>");
                return true;
            }
            String outcome = args[2].toLowerCase();
            if (target == null && !plugin.getKontManager().isInKont(Bukkit.getOfflinePlayer(targetName).getUniqueId())) {
                sender.sendMessage("§cBu oyuncu aktif bir kontrolde değil!");
                return true;
            }
            var uuid = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
            plugin.getKontManager().finishKont(uuid, outcome, "Yetkili kararı: " + outcome);
            sender.sendMessage("§aKontrol başarıyla sonuçlandırıldı: " + outcome);
            return true;
        }

        if ("uzat".equals(sub)) {
            int extraMins = 5;
            if (args.length >= 3) {
                try {
                    extraMins = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {}
            }
            if (target == null) {
                sender.sendMessage("§cOyuncu çevrimdışı!");
                return true;
            }
            if (plugin.getKontManager().extendKont(target.getUniqueId(), extraMins)) {
                sender.sendMessage("§aKontrol süresi " + extraMins + " dakika uzatıldı.");
            } else {
                sender.sendMessage("§cKontrol süresi uzatılamadı (maksimum sınıra ulaşılmış olabilir).");
            }
            return true;
        }

        return true;
    }
}
