# 🛡️ Staff & Security Core (StaffCore) - v1.0.0

**StaffCore**, Minecraft Paper/Spigot sunucuları için geliştirilmiş, kurumsal düzeyde **Yetkili Yönetimi, 2FA Giriş Kalkanı, Adli Bilişim (Kont), Otomatik Kanıtlı Ceza ve Discord Entegrasyonu** sağlayan yeni nesil bir güvenlik çekirdeğidir.

Geliştirici: **[Leronify](https://github.com/Ler10-DEV)**

---

## 🌟 Öne Çıkan Özellikler

1. **Pluggable JSON + RAM Depolama:** Varsayılan olarak sıfır veritabanı bağımlılığı ile ultra hızlı JSON flat-file + RAM cache mimarisi.
2. **Pluggable Database Desteği:** İleride tek satır konfigürasyon ile **Redis / H2 / MySQL / PostgreSQL** sürücülerine geçiş imkanı.
3. **Evrensel Hesap Eşleme:** `/hesap-eşle` ile anlık 4 haneli tek kullanımlık kod ve Discord slash komutu `/esle kod:`.
4. **Yetkili 2FA Giriş Kalkanı:** Yetkili girişinde körlük, yavaşlık, zıplama yasağı ve hareket kilidi; Discord onay butonu (`2fa:approve:{uuid}`).
5. **27 Slot Raporlama Arayüzü:** `/rapor <oyuncu>` ile kategorize edilmiş şikayet sistemi ve `#🚨-raporlar` Discord sarı embed bildirimi.
6. **Maskelenmiş Komut Loglama:** Şifre, pin ve gizli parametreleri regex ile `*******` olarak maskeleyen oyuncu başına 54 komutluk ring buffer.
7. **Kanıtlı Ceza Sistemi:** `#CZ-XXXX` sıralı ceza kimliği, Discord kanıt yükleme modalı ve yetki kilidi.
8. **Adli Bilişim Kont Sistemi:** `/kont baslat <oyuncu>` ile otomatik freeze, Discord geçici ses/metin odaları ve Combat-Quit koruması.
9. **Yetkili Skor Motoru:** 10+ eylemi puanlayan matris, denetim kaydı ve haftalık liderlik tablosu.
10. **Önleyici Güvenlik Sezgisi:** X-Ray 60s kayan pencere tespiti ve reklam/küfür engelleyici sohbet bekçisi.
11. **Oyuncu Memnuniyet Sistemi (CSAT):** `/puanla` ve 5 yıldızlı sandık menüsü (GUI) ile yetkili değerlendirme.

---

## 📁 Dosya ve Depolama Mimarisi

```
plugins/StaffCore/
├── data/
│   ├── linked_accounts.json        (Hesap eşleşmeleri)
│   ├── punishments.json            (Ceza kayıtları & kanıt durumları)
│   ├── kont_records.json           (Adli bilişim kont geçmişi)
│   ├── staff_scores.json           (Yetkili puanları)
│   ├── score_events.jsonl          (Denetim günlüğü)
│   └── command_logs/               (Oyuncu komut geçmişi)
│       └── {uuid}.json
├── config.yml
├── secrets.yml
└── messages.yml
```

---

## 🚀 Kurulum

1. **`StaffCore.jar`** dosyasını indirin ve sunucunuzun `plugins/` klasörüne atın.
2. Sunucunuzu başlatıp kapatarak varsayılan konfigürasyon dosyalarının oluşmasını sağlayın.
3. `plugins/StaffCore/secrets.yml` veya `config.yml` dosyasını açıp Discord Bot tokeninizi ve sunucu ID'lerinizi girin:
   ```yaml
   discord:
     enabled: true
     bot_token: "YOUR_DISCORD_BOT_TOKEN_HERE"
     guild_id: "YOUR_GUILD_ID"
     staff_role_id: "YOUR_STAFF_ROLE_ID"
   ```
4. Sunucuyu başlatın (`/staffcore reload` veya sunucu restart).

---

## ⌨️ Komutlar ve Yetkiler

| Komut | Yetki | Açıklama |
|---|---|---|
| `/hesap-eşle` (veya `/esle`) | Herkes | Discord eşleme kodu üretir (300s TTL) |
| `/rapor <oyuncu>` | Herkes | 27 slotlu raporlama GUI'sini açar |
| `/puanla` | Herkes | 5 yıldızlı sandık GUI'si ile yetkili puanlama |
| `/komutlog <oyuncu>` | `staff.commandlog` | Oyuncunun son 54 komutunu GUI'de açar |
| `/ceza <oyuncu> <sebep>` | `staff.punish` | Resmi ceza uygular ve Discord kanıt modalı oluşturur |
| `/kont baslat <oyuncu>` | `staff.kont` | Şüpheliyi dondurur ve Discord odalarını kurar |
| `/kont bitir <oyuncu> <temiz\|hile\|itiraf>` | `staff.kont` | Kontrolü sonuçlandırır ve puan verir |
| `/kont uzat <oyuncu> [dakika]` | `staff.kont` | Kontrol süresini uzatır |
| `/staffscore [oyuncu]` | `staff.score.view` | Yetkili puanını ve karne durumunu gösterir |
| `/staffcore reload` | `staff.admin` | Yapılandırmayı ve mesajları yeniler |
| `/staffcore backup` | `staff.admin` | Anlık veri yedeği oluşturur |
| `/staffcore stats` | `staff.admin` | Sistem ve depolama metriklerini görüntüler |
