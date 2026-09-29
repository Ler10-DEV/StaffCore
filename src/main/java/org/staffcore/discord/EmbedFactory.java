package org.staffcore.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.staffcore.storage.model.KontRecord;
import org.staffcore.storage.model.Punishment;

import java.awt.Color;
import java.time.Instant;
import java.util.UUID;

public class EmbedFactory {

    public static MessageEmbed create2FAPrompt(String playerName, UUID uuid, String ip) {
        return new EmbedBuilder()
                .setTitle("🔐 Yetkili 2FA Giriş Doğrulaması")
                .setDescription(String.format("**%s** adlı yetkili sunucuya giriş yapmak istiyor.\n\n" +
                        "**IP:** `%s`\n" +
                        "**UUID:** `%s`\n" +
                        "**Zaman:** <t:%d:R>\n\n" +
                        "Bu siz misiniz? Lütfen aşağıdaki butonlardan onaylayın veya reddedin.",
                        playerName, ip, uuid.toString(), Instant.now().getEpochSecond()))
                .setColor(new Color(255, 170, 0))
                .setFooter("StaffCore • by Leronify | 2FA Gatekeeper", null)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed create2FAResult(String playerName, boolean approved) {
        return new EmbedBuilder()
                .setTitle(approved ? "✅ 2FA Giriş Onaylandı" : "❌ 2FA Giriş Reddedildi")
                .setDescription(String.format("**%s** adlı yetkilinin giriş talebi %s.",
                        playerName, approved ? "onaylandı ve oyuna giriş yaptı" : "reddedildi/zaman aşımına uğradı"))
                .setColor(approved ? new Color(0, 200, 80) : new Color(220, 40, 40))
                .setFooter("StaffCore • by Leronify", null)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createReportEmbed(String reporterName, String targetName, String category, String location) {
        return new EmbedBuilder()
                .setTitle("🚨 Yeni Oyuncu Raporu")
                .addField("Rapor Eden", reporterName, true)
                .addField("Şüpheli Oyuncu", targetName, true)
                .addField("Kategori", category, true)
                .addField("Konum", location != null ? location : "Bilinmiyor", false)
                .setColor(new Color(255, 200, 0))
                .setFooter("StaffCore • by Leronify | Report Engine", null)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createPunishmentEmbed(Punishment p) {
        boolean verified = p.getStatus() != null && p.getStatus().name().equals("VERIFIED");
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🔨 Ceza Kaydı: " + p.getId())
                .addField("Hedef", p.getTargetName(), true)
                .addField("Yetkili", p.getStaffName(), true)
                .addField("Tür", p.getType(), true)
                .addField("Sebep", p.getReason(), false)
                .addField("Durum", p.getStatus().name(), true)
                .setColor(verified ? new Color(0, 200, 80) : new Color(255, 120, 0))
                .setFooter("StaffCore • by Leronify | Punishment System", null)
                .setTimestamp(Instant.ofEpochMilli(p.getTimestamp()));

        if (p.getProofUrl() != null && !p.getProofUrl().isEmpty()) {
            eb.addField("Kanıt Linki", p.getProofUrl(), false);
        }
        if (p.getReviewNote() != null && !p.getReviewNote().isEmpty()) {
            eb.addField("İnceleme Notu", p.getReviewNote(), false);
        }
        return eb.build();
    }

    public static MessageEmbed createKontStartEmbed(String staffName, String targetName, int durationSeconds) {
        return new EmbedBuilder()
                .setTitle("🔍 Kontrole Alındı (Forensic Kont)")
                .addField("Şüpheli", targetName, true)
                .addField("Yetkili", staffName, true)
                .addField("Süre", durationSeconds + " saniye", true)
                .setDescription("Oyuncu donduruldu, özel sesli ve metin odaları oluşturuldu.")
                .setColor(new Color(0, 150, 255))
                .setFooter("StaffCore • by Leronify | Kontrol Sistemi", null)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createKontEndEmbed(KontRecord r) {
        Color color = switch (r.getOutcome()) {
            case "CLEAN" -> new Color(0, 200, 80);
            case "HACKS_BANNED" -> new Color(220, 40, 40);
            case "CONFESSION" -> new Color(255, 140, 0);
            case "COMBAT_QUIT" -> new Color(150, 0, 0);
            default -> Color.GRAY;
        };

        return new EmbedBuilder()
                .setTitle("🔍 Kontrol Sonuçlandı: " + r.getTargetName())
                .addField("Yetkili", r.getStaffName(), true)
                .addField("Sonuç", r.getOutcome(), true)
                .addField("Süre", r.getDurationSeconds() + " saniye", true)
                .addField("Notlar", r.getNotes() != null ? r.getNotes() : "-", false)
                .setColor(color)
                .setFooter("StaffCore • by Leronify | Kontrol Denetimi", null)
                .setTimestamp(Instant.ofEpochMilli(r.getEndedAt()))
                .build();
    }

    public static MessageEmbed createSecurityAlarmEmbed(String type, String player, String detail) {
        return new EmbedBuilder()
                .setTitle("⚠️ Güvenlik Alarmı: " + type)
                .addField("Oyuncu", player, true)
                .addField("Detay", detail, false)
                .setColor(new Color(255, 60, 60))
                .setFooter("StaffCore • by Leronify | Heuristic Watchdog", null)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createRatingEmbed(String playerName, String staffName, int stars, String comment, int newScore) {
        String starDisplay = "★".repeat(Math.max(1, Math.min(5, stars))) + "☆".repeat(Math.max(0, 5 - stars));
        Color color = switch (stars) {
            case 5 -> new Color(0, 220, 100);
            case 4 -> new Color(80, 200, 120);
            case 3 -> new Color(255, 200, 0);
            case 2 -> new Color(255, 120, 0);
            default -> new Color(220, 40, 40);
        };

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("⭐ Yeni Yetkili Değerlendirmesi")
                .addField("Puanlayan Oyuncu", playerName, true)
                .addField("Değerlendirilen Yetkili", staffName, true)
                .addField("Verilen Puan", starDisplay + " (" + stars + "/5 Yıldız)", false)
                .setColor(color)
                .setFooter("StaffCore • by Leronify | CSAT Engine • Güncel Skor: " + newScore, null)
                .setTimestamp(Instant.now());

        if (comment != null && !comment.trim().isEmpty()) {
            eb.addField("Oyuncu Yorumu", "💬 " + comment, false);
        }

        return eb.build();
    }
}
