package org.staffcore.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.staffcore.StaffCorePlugin;
import org.staffcore.admin.AdminCommand;
import org.staffcore.commandlog.CommandLogCommand;
import org.staffcore.commandlog.CommandLogger;
import org.staffcore.csat.CsatCommand;
import org.staffcore.csat.CsatListener;
import org.staffcore.heuristic.AlarmDispatcher;
import org.staffcore.heuristic.ChatWatchdog;
import org.staffcore.heuristic.XRayHeuristic;
import org.staffcore.kont.CombatQuitGuard;
import org.staffcore.kont.KontCommandHandler;
import org.staffcore.kont.KontManager;
import org.staffcore.link.AccountLinkService;
import org.staffcore.link.LinkCommand;
import org.staffcore.link.StaffFlagService;
import org.staffcore.punishment.ProofReminderTask;
import org.staffcore.punishment.PunishCommand;
import org.staffcore.punishment.PunishmentListener;
import org.staffcore.punishment.PunishmentService;
import org.staffcore.report.ReportCommand;
import org.staffcore.report.ReportTicketService;
import org.staffcore.score.RoleAssigner;
import org.staffcore.score.ScoreEngine;
import org.staffcore.score.StaffScoreCommand;
import org.staffcore.score.WeeklyLeaderboardTask;
import org.staffcore.storage.json.JsonStorageProvider;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ModuleLoader {
    private final StaffCorePlugin plugin;
    private final Logger logger;

    public ModuleLoader(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadAll() {
        logger.info("Initializing StaffCore modules...");

plugin.setStaffFlagService(new StaffFlagService(plugin));
        plugin.setAccountLinkService(new AccountLinkService(plugin));
        plugin.setScoreEngine(new ScoreEngine(plugin));
        plugin.setRoleAssigner(new RoleAssigner(plugin));
        plugin.setPunishmentService(new PunishmentService(plugin));
        plugin.setKontManager(new KontManager(plugin));
        plugin.setReportTicketService(new ReportTicketService(plugin));
        plugin.setCsatListener(new CsatListener(plugin));

        AlarmDispatcher alarmDispatcher = new AlarmDispatcher(plugin);
        plugin.setAlarmDispatcher(alarmDispatcher);
        plugin.setChatWatchdog(new ChatWatchdog(plugin, alarmDispatcher));
        plugin.setXRayHeuristic(new XRayHeuristic(plugin, alarmDispatcher));
        plugin.setCommandLogger(new CommandLogger(plugin));

PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(plugin.getStaffGatekeeper(), plugin);
        pm.registerEvents(plugin.getReportTicketService(), plugin);
        pm.registerEvents(plugin.getCsatListener(), plugin);
        pm.registerEvents(plugin.getCommandLogger(), plugin);
        pm.registerEvents(new PunishmentListener(plugin), plugin);
        pm.registerEvents(new CombatQuitGuard(plugin), plugin);
        pm.registerEvents(plugin.getChatWatchdog(), plugin);
        pm.registerEvents(plugin.getXRayHeuristic(), plugin);

registerCommand("hesap-eşle", new LinkCommand(plugin));
        registerCommand("rapor", new ReportCommand(plugin));
        registerCommand("komutlog", new CommandLogCommand(plugin));
        registerCommand("ceza", new PunishCommand(plugin));
        registerCommand("kont", new KontCommandHandler(plugin));
        registerCommand("puanla", new CsatCommand(plugin));
        registerCommand("staffscore", new StaffScoreCommand(plugin));

        AdminCommand adminCmd = new AdminCommand(plugin);
        var scCmd = plugin.getCommand("staffcore");
        if (scCmd != null) {
            scCmd.setExecutor(adminCmd);
            scCmd.setTabCompleter(adminCmd);
        }

schedulePeriodicTasks();

        logger.info("All StaffCore modules successfully loaded.");
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        var cmd = plugin.getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (executor instanceof org.bukkit.command.TabCompleter tc) {
                cmd.setTabCompleter(tc);
            }
        }
    }

    private void schedulePeriodicTasks() {
        int flushSec = plugin.getConfigManager().getFlushIntervalSeconds();

Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getStorageProvider().flush();
        }, flushSec * 20L, flushSec * 20L);

Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getAccountLinkService().getCodeCache().cleanupExpired();
        }, 600L, 600L); 

Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new ProofReminderTask(plugin), 6000L, 6000L);

if (plugin.getConfigManager().isBackupEnabled()) {
            int hours = plugin.getConfigManager().getBackupIntervalHours();
            plugin.getDiscordBot().getAsyncExecutor().scheduleAtFixedRate(() -> {
                if (plugin.getStorageProvider() instanceof JsonStorageProvider jsonProv) {
                    try {
                        jsonProv.getBackupService().createBackupNow();
                    } catch (Exception e) {
                        logger.warning("Automated backup error: " + e.getMessage());
                    }
                }
            }, hours, hours, TimeUnit.HOURS);
        }

plugin.getDiscordBot().getAsyncExecutor().scheduleAtFixedRate(
                new WeeklyLeaderboardTask(plugin), 1, 7, TimeUnit.DAYS
        );
    }
}
