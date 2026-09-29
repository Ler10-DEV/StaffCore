package org.staffcore.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.MigrationRunner;
import org.staffcore.storage.StorageFactory;
import org.staffcore.storage.StorageProvider;
import org.staffcore.storage.StorageType;
import org.staffcore.storage.json.JsonStorageProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {
    private final StaffCorePlugin plugin;

    public AdminCommand(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("staff.admin") && !sender.isOp()) {
            sender.sendMessage(plugin.getLocaleManager().getPrefixed("no_permission", "&cBu komutu kullanmak için yetkiniz yok!", null));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§6§l[Leronify] §b=== StaffCore Güvenlik & Yetkili Yönetimi ===");
            sender.sendMessage("§e/staffcore reload §7- Yapılandırmayı ve mesajları yeniler.");
            sender.sendMessage("§e/staffcore backup §7- Veritabanı anlık zip yedeği oluşturur.");
            sender.sendMessage("§e/staffcore migrate <target> [--dry] §7- Veri göçü çalıştırır.");
            sender.sendMessage("§e/staffcore stats §7- Depolama ve sistem istatistiklerini görüntüler.");
            sender.sendMessage("§8» Developed with excellence by Leronify");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                plugin.getConfigManager().loadConfig();
                plugin.getLocaleManager().loadMessages();
                plugin.getChatWatchdog().loadPatterns();
                sender.sendMessage(plugin.getLocaleManager().getPrefixed("admin.reloaded", "&aStaffCore yapılandırması başarıyla yeniden yüklendi.", null));
            }
            case "backup" -> {
                if (plugin.getStorageProvider() instanceof JsonStorageProvider jsonProv) {
                    try {
                        Path zip = jsonProv.getBackupService().createBackupNow();
                        sender.sendMessage("§aVeri yedeği başarıyla oluşturuldu: §e" + zip.getFileName());
                    } catch (Exception e) {
                        sender.sendMessage("§cYedekleme sırasında hata oluştu: " + e.getMessage());
                    }
                } else {
                    sender.sendMessage("§eYedekleme servisi yalnızca JSON depolama motorunda doğrudan dosya zipi oluşturur.");
                }
            }
            case "migrate" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cKullanım: /staffcore migrate <json|redis|h2|mysql|postgresql> [--dry]");
                    return true;
                }
                String targetTypeStr = args[1];
                StorageType targetType = StorageType.fromString(targetTypeStr);
                boolean dryRun = args.length >= 3 && "--dry".equalsIgnoreCase(args[2]);

                try {
                    Path dataDir = plugin.getConfigManager().getDataDir();
                    Path backupDir = plugin.getConfigManager().getBackupDir();
                    StorageProvider targetProvider = StorageFactory.createProvider(
                            targetType, dataDir, backupDir, plugin.getConfigManager().isPrettyPrintJson(), plugin.getLogger()
                    );

                    MigrationRunner runner = new MigrationRunner(plugin.getStorageProvider(), plugin.getLogger());
                    MigrationRunner.MigrationReport report = runner.migrate(targetProvider, dryRun, dataDir);

                    if (report.dryRun()) {
                        sender.sendMessage("§e[DRY-RUN] " + report.message());
                    } else {
                        sender.sendMessage("§a[MIGRATE] " + report.message());
                    }
                } catch (UnsupportedOperationException e) {
                    sender.sendMessage("§cHedef sürücü henüz aktif değil: " + e.getMessage());
                } catch (Exception e) {
                    sender.sendMessage("§cGöç sırasında hata: " + e.getMessage());
                }
            }
            case "stats" -> {
                sender.sendMessage("§b=== StaffCore Sistem & Depolama Durumu ===");
                sender.sendMessage("§7Aktif Depolama Sürücüsü: §a" + plugin.getStorageProvider().getType());
                sender.sendMessage("§7Sağlık Durumu: " + (plugin.getStorageProvider().isHealthy() ? "§aSAĞLIKLI" : "§cKUSURLU"));
                sender.sendMessage("§7Eşli Hesaplar: §e" + plugin.getStorageProvider().linkedAccounts().getAll().size());
                sender.sendMessage("§7Kayıtlı Cezalar: §e" + plugin.getStorageProvider().punishments().getAll().size());
                sender.sendMessage("§7Kont Kayıtları: §e" + plugin.getStorageProvider().kontRecords().getRecentRecords(1000).size());
                sender.sendMessage("§7Yetkili Puan Kayıtları: §e" + plugin.getStorageProvider().staffScores().getAll().size());
            }
            default -> sender.sendMessage("§cBilinmeyen alt komut: " + sub);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload", "backup", "migrate", "stats");
        }
        if (args.length == 2 && "migrate".equalsIgnoreCase(args[0])) {
            return List.of("json", "redis", "h2", "mysql", "postgresql");
        }
        if (args.length == 3 && "migrate".equalsIgnoreCase(args[0])) {
            return List.of("--dry");
        }
        return new ArrayList<>();
    }
}
