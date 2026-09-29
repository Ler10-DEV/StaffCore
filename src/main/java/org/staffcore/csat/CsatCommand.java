package org.staffcore.csat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.staffcore.StaffCorePlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsatCommand implements CommandExecutor, TabCompleter {
    private final StaffCorePlugin plugin;

    public CsatCommand(StaffCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length >= 2) {
                String targetStaff = args[0];
                try {
                    int stars = Integer.parseInt(args[1]);
                    String comment = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Konsol Oylaması";
                    plugin.getCsatListener().getRatingHandler().submitDirectStaffVote(null, targetStaff, stars, comment);
                    sender.sendMessage("§a[StaffCore] Yetkiliye puan verildi: " + targetStaff + " -> " + stars + " Yıldız");
                } catch (Exception e) {
                    sender.sendMessage("§cKullanım (Konsol): /puanla <yetkili> <1-5> [yorum]");
                }
            } else {
                sender.sendMessage("§cKullanım (Konsol): /puanla <yetkili> <1-5> [yorum]");
            }
            return true;
        }

        if (args.length == 0) {
            
            RatingGUI gui = new RatingGUI(plugin, null, null);
            gui.open(player);
            return true;
        }

        String firstArg = args[0];

Integer ticketId = null;
        try {
            ticketId = Integer.parseInt(firstArg);
        } catch (NumberFormatException ignored) {}

        if (ticketId != null) {
            if (args.length == 1) {
                
                RatingGUI gui = new RatingGUI(plugin, null, ticketId);
                gui.open(player);
                return true;
            }

try {
                int stars = Integer.parseInt(args[1]);
                String comment = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;
                plugin.getCsatListener().getRatingHandler().submitTicketVote(player, ticketId, stars, comment);
            } catch (NumberFormatException e) {
                player.sendMessage("§cLütfen geçerli bir yıldız sayısı girin (1-5)!");
            }
            return true;
        }

String staffName = firstArg;
        if (args.length == 1) {
            
            RatingGUI gui = new RatingGUI(plugin, staffName, null);
            gui.open(player);
            return true;
        }

        try {
            int stars = Integer.parseInt(args[1]);
            String comment = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;
            plugin.getCsatListener().getRatingHandler().submitDirectStaffVote(player, staffName, stars, comment);
        } catch (NumberFormatException e) {
            player.sendMessage("§cLütfen 1 ile 5 arasında geçerli bir yıldız sayısı giriniz! (Örn: /puanla " + staffName + " 5 Teşekkürler)");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("staff.use") || p.isOp()) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        if (args.length == 2) {
            return List.of("1", "2", "3", "4", "5");
        }
        return new ArrayList<>();
    }
}
