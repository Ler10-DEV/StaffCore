package org.staffcore;

import org.bukkit.plugin.java.JavaPlugin;
import org.staffcore.auth.StaffGatekeeper;
import org.staffcore.bootstrap.ModuleLoader;
import org.staffcore.bootstrap.ShutdownHook;
import org.staffcore.commandlog.CommandLogger;
import org.staffcore.config.ConfigManager;
import org.staffcore.config.LocaleManager;
import org.staffcore.csat.CsatListener;
import org.staffcore.discord.ChannelRegistry;
import org.staffcore.discord.DiscordBot;
import org.staffcore.heuristic.AlarmDispatcher;
import org.staffcore.heuristic.ChatWatchdog;
import org.staffcore.heuristic.XRayHeuristic;
import org.staffcore.kont.KontManager;
import org.staffcore.link.AccountLinkService;
import org.staffcore.link.StaffFlagService;
import org.staffcore.punishment.PunishmentService;
import org.staffcore.report.ReportTicketService;
import org.staffcore.score.RoleAssigner;
import org.staffcore.score.ScoreEngine;
import org.staffcore.storage.StorageException;
import org.staffcore.storage.StorageFactory;
import org.staffcore.storage.StorageProvider;

import java.nio.file.Path;
import java.util.logging.Level;

public class StaffCorePlugin extends JavaPlugin {
    private static StaffCorePlugin instance;

    private ConfigManager configManager;
    private LocaleManager localeManager;
    private StorageProvider storageProvider;
    private DiscordBot discordBot;

    private StaffFlagService staffFlagService;
    private AccountLinkService accountLinkService;
    private StaffGatekeeper staffGatekeeper;
    private ReportTicketService reportTicketService;
    private CommandLogger commandLogger;
    private PunishmentService punishmentService;
    private KontManager kontManager;
    private ScoreEngine scoreEngine;
    private RoleAssigner roleAssigner;
    private ChatWatchdog chatWatchdog;
    private XRayHeuristic xRayHeuristic;
    private AlarmDispatcher alarmDispatcher;
    private CsatListener csatListener;

    private ModuleLoader moduleLoader;
    private ShutdownHook shutdownHook;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("==================================================");
        getLogger().info("          StaffCore - v" + getDescription().getVersion());
        getLogger().info("    Signature: Engineered with pride by Leronify");
        getLogger().info("    GitHub: https://github.com/Ler10-DEV");
        getLogger().info("    PaperMC 1.20.4+ | JSON Engine | JDA 5");
        getLogger().info("==================================================");

        // 1. Config & Locale
        this.configManager = new ConfigManager(this);
        this.localeManager = new LocaleManager(this);

        // 2. Initialize Pluggable Storage Engine
        try {
            Path dataDir = configManager.getDataDir();
            Path backupDir = configManager.getBackupDir();
            this.storageProvider = StorageFactory.createProvider(
                    configManager.getStorageType(),
                    dataDir,
                    backupDir,
                    configManager.isPrettyPrintJson(),
                    getLogger()
            );
            this.storageProvider.initialize();
        } catch (StorageException e) {
            getLogger().log(Level.SEVERE, "CRITICAL: Failed to initialize Storage Provider: " + e.getMessage(), e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 3. Initialize Discord Bot
        this.discordBot = new DiscordBot(this);
        this.discordBot.start();

        // 4. Gatekeeper (2FA)
        this.staffGatekeeper = new StaffGatekeeper(this);

        // 5. Load Modules
        this.moduleLoader = new ModuleLoader(this);
        this.moduleLoader.loadAll();

        this.shutdownHook = new ShutdownHook(this);

        getLogger().info("StaffCore enabled successfully and ready for operations!");
    }

    @Override
    public void onDisable() {
        if (shutdownHook != null) {
            shutdownHook.executeShutdown();
        }
    }

    public static StaffCorePlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public ChannelRegistry getChannelRegistry() {
        return discordBot != null ? discordBot.getChannelRegistry() : new ChannelRegistry();
    }

    public StaffFlagService getStaffFlagService() {
        return staffFlagService;
    }

    public void setStaffFlagService(StaffFlagService staffFlagService) {
        this.staffFlagService = staffFlagService;
    }

    public AccountLinkService getAccountLinkService() {
        return accountLinkService;
    }

    public void setAccountLinkService(AccountLinkService accountLinkService) {
        this.accountLinkService = accountLinkService;
    }

    public StaffGatekeeper getStaffGatekeeper() {
        return staffGatekeeper;
    }

    public ReportTicketService getReportTicketService() {
        return reportTicketService;
    }

    public void setReportTicketService(ReportTicketService reportTicketService) {
        this.reportTicketService = reportTicketService;
    }

    public CommandLogger getCommandLogger() {
        return commandLogger;
    }

    public void setCommandLogger(CommandLogger commandLogger) {
        this.commandLogger = commandLogger;
    }

    public PunishmentService getPunishmentService() {
        return punishmentService;
    }

    public void setPunishmentService(PunishmentService punishmentService) {
        this.punishmentService = punishmentService;
    }

    public KontManager getKontManager() {
        return kontManager;
    }

    public void setKontManager(KontManager kontManager) {
        this.kontManager = kontManager;
    }

    public ScoreEngine getScoreEngine() {
        return scoreEngine;
    }

    public void setScoreEngine(ScoreEngine scoreEngine) {
        this.scoreEngine = scoreEngine;
    }

    public RoleAssigner getRoleAssigner() {
        return roleAssigner;
    }

    public void setRoleAssigner(RoleAssigner roleAssigner) {
        this.roleAssigner = roleAssigner;
    }

    public ChatWatchdog getChatWatchdog() {
        return chatWatchdog;
    }

    public void setChatWatchdog(ChatWatchdog chatWatchdog) {
        this.chatWatchdog = chatWatchdog;
    }

    public XRayHeuristic getXRayHeuristic() {
        return xRayHeuristic;
    }

    public void setXRayHeuristic(XRayHeuristic xRayHeuristic) {
        this.xRayHeuristic = xRayHeuristic;
    }

    public AlarmDispatcher getAlarmDispatcher() {
        return alarmDispatcher;
    }

    public void setAlarmDispatcher(AlarmDispatcher alarmDispatcher) {
        this.alarmDispatcher = alarmDispatcher;
    }

    public CsatListener getCsatListener() {
        return csatListener;
    }

    public void setCsatListener(CsatListener csatListener) {
        this.csatListener = csatListener;
    }
}
