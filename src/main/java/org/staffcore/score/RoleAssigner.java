package org.staffcore.score;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.staffcore.StaffCorePlugin;
import org.staffcore.storage.model.LinkRecord;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class RoleAssigner {
    private final StaffCorePlugin plugin;
    private final Logger logger;

    public RoleAssigner(StaffCorePlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void assignStaffOfTheWeek(UUID staffUuid) {
        if (plugin.getDiscordBot().getJda() == null) return;
        String guildId = plugin.getConfigManager().getDiscordGuildId();
        if (guildId == null || guildId.isEmpty()) return;

        Guild guild = plugin.getDiscordBot().getJda().getGuildById(guildId);
        if (guild == null) return;

        Optional<LinkRecord> link = plugin.getStorageProvider().linkedAccounts().findByUuid(staffUuid);
        if (link.isEmpty()) return;

        Role role = guild.getRolesByName("Haftanın Yetkilisi", true).stream().findFirst().orElse(null);
        if (role == null) {
            try {
                role = guild.createRole().setName("Haftanın Yetkilisi").setColor(java.awt.Color.ORANGE).complete();
            } catch (Exception e) {
                logger.warning("Failed to create Haftanın Yetkilisi role: " + e.getMessage());
                return;
            }
        }

for (Member m : guild.getMembersWithRoles(role)) {
            guild.removeRoleFromMember(m, role).queue();
        }

Member targetMember = guild.getMemberById(link.get().getDiscordId());
        if (targetMember != null && role != null) {
            guild.addRoleToMember(targetMember, role).queue();
            logger.info("Assigned 'Haftanın Yetkilisi' role to " + targetMember.getEffectiveName());
        }
    }
}
