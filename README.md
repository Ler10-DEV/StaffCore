# 🛡️ Staff & Security Core (StaffCore) - v1.0.0

**StaffCore**, Minecraft Paper/Spigot (1.20.4) sunucuları için geliştirilmiş, kurumsal düzeyde **Yetkili Yönetimi, 2FA Giriş Kalkanı, Adli Bilişim (Kont), Otomatik Kanıtlı Ceza ve Discord Entegrasyonu** sağlayan yeni nesil bir güvenlik çekirdeğidir.

---

## 🌟 Öne Çıkan Özellikler

1. **Pluggable JSON + RAM Depolama:** Varsayılan olarak sıfır veritabanı bağımlılığı ile ultra hızlı JSON flat-file + RAM cache mimarisi.
2. **Pluggable Database Mimarisi:** İleride tek satır konfigürasyon ile **Redis / H2 / MySQL / PostgreSQL** sürücülerine geçiş imkanı.
3. **Evrensel Hesap Eşleme:** `/hesap-eşle` ile anlık 4 haneli tek kullanımlık kod ve Discord slash komutu `/esle kod:`.
4. **Yetkili 2FA Giriş Kalkanı:** Yetkili girişinde körlük, yavaşlık, zıplama yasağı ve hareket kilidi; Discord onay butonu (`2fa:approve:{uuid}`).
5. **27 Slot Raporlama Arayüzü:** `/rapor <oyuncu>` ile kategorize edilmiş şikayet sistemi ve `#🚨-raporlar` Discord sarı embed bildirimi.
6. **Maskelenmiş Komut Loglama:** Şifre, pin ve gizli parametreleri regex ile `*******` olarak maskeleyen oyuncu başına 54 komutluk ring buffer.
7. **Kanıtlı Ceza Sistemi:** `#CZ-XXXX` sıralı ceza kimliği, Discord kanıt yükleme modalı ve yetki kilidi.
8. **Adli Bilişim Kont Sistemi:** `/kont baslat <oyuncu>` ile otomatik freeze, Discord geçici ses/metin odaları ve Combat-Quit koruması.
9. **Yetkili Skor Motoru:** 10+ eylemi puanlayan matris, `score_events.jsonl` denetim kaydı ve haftalık liderlik tablosu.
10. **Önleyici Güvenlik Sezgisi:** X-Ray 60s kayan pencere tespiti ve reklam/küfür engelleyici sohbet bekçisi.
11. **Oyuncu Memnuniyet Sistemi (CSAT):** Rapor çözüldüğünde 1-3 yıldız interaktif geri bildirim butonları.

---

## 📁 Dosya ve Depolama Mimarisi

```
plugins/StaffCore/
├── data/
│   ├── linked_accounts.json        (Map<UUID, LinkRecord>)
│   ├── punishments.json            (Map<punishment_id, Punishment>)
│   ├── kont_records.json           (List<KontRecord>, son 1000 kayıt — rolling)
│   ├── staff_scores.json           (Map<UUID, ScoreRecord>)
│   ├── score_events.jsonl          (append-only audit log)
│   ├── staffcore.meta.json         (sıralı ceza ID sayacı)
│   └── command_logs/               (oyuncu başına son 54 komut)
│       └── {uuid}.json
├── backups/
│   └── data-20260926-184500.zip
├── config.yml
├── secrets.yml
└── messages.yml
```

---

## 🚀 Kurulum ve İlk Çalıştırma

1. `target/staffcore-1.0.0-shaded.jar` dosyasını sunucunuzun `plugins/` klasörüne yerleştirin.
2. Sunucunuzu başlatıp kapatarak varsayılan dosyaların oluşmasını sağlayın.
3. `plugins/StaffCore/secrets.yml` dosyasını açıp Discord Bot tokeninizi girin:
   ```yaml
   discord:
     bot_token: "YOUR_DISCORD_BOT_TOKEN_HERE"
   ```
4. `plugins/StaffCore/config.yml` dosyasında `guild_id` ve `staff_role_id` alanlarını doldurun.
5. Sunucuyu yeniden başlatın (`/staffcore reload` veya restart).

---

## 🗄️ Veritabanına Geçiş Rehberi (Migration Guide)

StaffCore'un DAO mimarisi sayesinde JSON'dan SQL veya NoSQL veritabanlarına geçiş son derece basittir:

### 1. Hazır Stub Sürücüler
- `org.staffcore.storage.redis.RedisStorageProvider`
- `org.staffcore.storage.h2.H2StorageProvider`
- `org.staffcore.storage.mysql.MySQLStorageProvider`
- `org.staffcore.storage.postgres.PostgresStorageProvider`

### 2. Geçiş Adımları
1. `pom.xml` dosyanıza ilgili veritabanı sürücüsünü (JDBC / Jedis / HikariCP) ekleyin.
2. Hedef `StorageProvider` sınıfındaki DAO metodlarını uygulayın.
3. Sunucu konsolunda önizleme (dry-run) yapın:
   ```bash
   /staffcore migrate mysql --dry
   ```
4. Taşıma işlemini başlatın:
   ```bash
   /staffcore migrate mysql
   ```
5. Taşıma tamamlandıktan sonra `config.yml` içinde `storage.type: mysql` olarak güncelleyin. Eski JSON verileriniz güvenlik amacıyla `data/_migrated_{timestamp}/` klasörüne arşivlenecektir.

---

## ⌨️ Komutlar ve Yetkiler

| Komut | Yetki | Açıklama |
|---|---|---|
| `/hesap-eşle` (veya `/esle`) | Herkes | Discord eşleme kodu üretir (300s TTL) |
| `/rapor <oyuncu>` | Herkes | 27 slotlu raporlama GUI'sini açar |
| `/komutlog <oyuncu>` | `staff.commandlog` | Oyuncunun son 54 komutunu 54 slot GUI'de açar |
| `/ceza <oyuncu> <sebep>` | `staff.punish` | Resmi ceza uygular ve Discord kanıt butonu oluşturur |
| `/kont baslat <oyuncu>` | `staff.kont` | Şüpheliyi dondurur ve Discord odalarını kurar |
| `/kont bitir <oyuncu> <temiz\|hile\|itiraf>` | `staff.kont` | Kontrolü sonuçlandırır ve puan verir |
| `/kont uzat <oyuncu> [dakika]` | `staff.kont` | Kontrol süresini uzatır |
| `/csat <ticketId> <1\|2\|3>` | Herkes | Yetkili performansını 1-3 yıldızla oylar |
| `/staffscore [oyuncu]` | `staff.score.view` | Yetkili puanını ve haftalık skorunu gösterir |
| `/staffcore reload` | `staff.admin` | Yapılandırmayı ve mesajları yeniler |
| `/staffcore backup` | `staff.admin` | Anlık `data/` zip yedeği oluşturur |
| `/staffcore migrate <target> [--dry]` | `staff.admin` | Veri göçü simülasyonu veya çalıştırması yapar |
| `/staffcore stats` | `staff.admin` | Depolama ve sistem metriklerini görüntüler |

---

## 🧪 Testleri Çalıştırma

Mineflayer ve Vitest tabanlı 17 uçtan uca senaryoyu çalıştırmak için:

```bash
npm install
npm test
```

Veya tüm derleme ve doğrulama boru hattını çalıştırmak için:
```bash
npm run pipeline
```
