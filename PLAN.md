# 🚀 StaffCore Geliştirme, Test ve Doğrulama Planı (v5)

## 📌 Genel Bakış
Bu plan, `Plugin Detaylı Tasarım ve Sistem Dokümantasyonu.md` şartnamesine uygun olarak **Staff & Security Core** Minecraft eklentisinin eksiksiz geliştirilmesi, JSON ve Pluggable depolama mimarisi ile donatılması ve 17 adet Mineflayer uçtan uca test senaryosu ile doğrulanmasını kapsar.

---

## 🧱 Aşama 1: Eklenti Geliştirme (Plugin Development)
- [x] **1.1 Maven ve Bağımlılık Yapısı:**
  - `plugin/pom.xml` (PaperMC 1.20.4-R0.1-SNAPSHOT, Java 17, JDA 5.0.0-beta.20, Gson 2.10.1, Maven Shade + Relocations)
  - `plugin.yml` (Tüm komutlar, izinler ve aliases tanımları)
- [x] **1.2 Depolama (Storage) Katmanı & Abstraction:**
  - `StorageType`, `StorageProvider`, `StorageFactory`, `StorageException`, `MigrationRunner`
  - DAO Arayüzleri: `LinkedAccountDAO`, `PunishmentDAO`, `KontRecordDAO`, `StaffScoreDAO`, `CommandLogDAO`
  - JSON Motoru: `JsonStore<T>` (Thread-safe `ReentrantReadWriteLock`, RAM Cache `ConcurrentHashMap`, `.tmp` -> `ATOMIC_MOVE`, `.corrupt-{ts}` kurtarma)
  - JSON DAO İmplementasyonları: `JsonLinkedAccountDAO`, `JsonPunishmentDAO`, `JsonKontRecordDAO`, `JsonStaffScoreDAO`, `JsonCommandLogDAO`
  - Otomatik Yedekleme Servisi: `JsonBackupService` (Periyodik Zip + Retention + Elle tetikleme)
  - Pluggable Stub Sürücüler: `RedisStorageProvider`, `H2StorageProvider`, `MySQLStorageProvider`, `PostgresStorageProvider`
- [x] **1.3 Konfigürasyon ve Güvenlik:**
  - `ConfigManager`, `SecretsManager` (ENV > secrets.yml > config.yml, `maskSecrets()` regex filtresi), `LocaleManager` (`messages.yml`)
  - `config.yml`, `secrets.yml.example`, `messages.yml`
- [x] **1.4 11 Çekirdek Modülün Geliştirilmesi:**
  1. **Discord Entegrasyonu & Bot:** `DiscordBot`, `GuildInitializer`, `ChannelRegistry`, `EmbedFactory`, `RateLimitGuard`, Slash Commands & Listeners
  2. **Evrensel Hesap Eşleme:** `AccountLinkService`, `LinkCodeCache` (4 haneli kod, 300s TTL), `/hesap-eşle`, `/esle`
  3. **Yetkili 2FA Giriş Kalkanı:** `StaffGatekeeper`, `QuarantineState`, `LoginApprovalFlow` (Körlük, yavaşlık, zıplama engeli, hasarsızlık, Discord Onay/Red butonları, 60s timeout)
  4. **Oyuncu Raporlama Sistemi:** `ReportGUI` (27 slot GUI), `ReportNotifier`, `ReportTicketService`, Discord Sarı Embed & Sesli/ActionBar Bildirim
  5. **Komut Loglama ve Maskeleme:** `CommandLogger`, `MaskFilter` (Şifre/Pin regex maskeleme `*******`), 54 komut Ring Buffer, `command_logs/{uuid}.json`, `CommandLogGUI`
  6. **Kanıtlı Ceza Sistemi:** `PunishmentService` (`#CZ-XXXX` sıralı ID), `PunishmentModalHandler`, `ProofReminderTask`, `PunishmentListener`, Discord Kanıt Yükleme Modalı, Yetki Kilidi
  7. **Adli Bilişim Kont Sistemi:** `KontManager`, `KontDiscordRooms` (Geçici kategori/ses/metin kanalları), `KontFreezeTask`, `CombatQuitGuard` (Kont sırasında çıkışta otomatik ban), `/kont baslat|bitir|uzat`
  8. **Yetkili Skor Motoru:** `ScoreEngine` (10+ eylem puanlama matrisi, `score_events.jsonl` append-only audit), `WeeklyLeaderboardTask` (Pazar 23:59 haftanın yetkilisi), `RoleAssigner`, Düşük Skor Uyarısı
  9. **Önleyici Güvenlik Sezgisi:** `ChatWatchdog` (Reklam & Küfür regex filtresi), `XRayHeuristic` (Elmas, netherite, zümrüt 60s kayan pencere, Y seviyesi filtresi, Discord alarmı)
  10. **Oyuncu Memnuniyet Sistemi (CSAT):** `CsatListener`, `RatingHandler`, 1-3 yıldız interaktif sohbet butonları, skor entegrasyonu, yetkili DM bildirimi
  11. **Admin & Bakım:** `AdminCommand` (`/staffcore reload|backup|migrate`), `BackupCommand`
- [x] **1.5 Derleme ve Paketleme:**
  - `mvn clean package` derlemesi, shaded jar üretimi ve validasyon.

---

## 🧪 Aşama 2: Mineflayer Doğrulama Testleri (Test Suite)
- [x] **2.1 Test Altyapısı ve Ortamı:**
  - `package.json` (Node 20, vitest, mineflayer, mineflayer-pathfinder, pino)
  - Test yardımcıları: `setup.mjs`, `botFactory.mjs`, `awaitChat.mjs`, `awaitTitle.mjs`, `awaitWindow.mjs`, `discordAssert.mjs`, `jsonAssert.mjs`, `restartServer.mjs`, `teardown.mjs`
  - `data-test/` izolasyonu (`STAFFCORE_DATA_DIR=data-test`)
- [x] **2.2 17 Uçtan Uca Test Senaryosu:**
  - `t01-connection.test.mjs` (T1: Bağlantı ve karşılama)
  - `t02-account-link.test.mjs` (T2: `/hesap-eşle` kod üretimi)
  - `t03-report-gui.test.mjs` (T3: `/rapor` 27 slot GUI)
  - `t04-permission-guard.test.mjs` (T4: Yetkisiz komut engeli)
  - `t05-chat-filter.test.mjs` (T5: Chat filtreleme / reklam engeli)
  - `t06-kont-start.test.mjs` (T6: `/kont baslat` Title uyarısı)
  - `t07-combat-quit.test.mjs` (T7: Kont sırasında çıkış ve Combat-Quit banı)
  - `t08-code-ttl.test.mjs` (T8: Kod 300s TTL zaman aşımı)
  - `t09-command-masking.test.mjs` (T9: `/login` şifre maskeleme)
  - `t10-csat-flow.test.mjs` (T10: CSAT 1-3 yıldız oylama akışı)
  - `t11-staff-2fa.test.mjs` (T11: 2FA karantina efektleri)
  - `t12-kont-discord-rooms.test.mjs` (T12: Kont geçici Discord odaları)
  - `t13-punishment-modal.test.mjs` (T13: Ceza kanıt modalı)
  - `t14-score-update.test.mjs` (T14: Skor güncelleme ve audit log)
  - `t15-json-atomic-flush.test.mjs` (T15: Atomik JSON flush ve crash kurtarma)
  - `t16-backup-zip.test.mjs` (T16: Otomatik ve manuel zip yedekleme)
  - `t17-migration-dryrun.test.mjs` (T17: `/staffcore migrate h2 --dry` taşıma simülasyonu)
- [x] **2.3 Raporlama ve Flaky Yönetimi:**
  - Test raporu oluşturucu (`TEST_REPORT.md`) ve hata yakalama dump mekanizması.

---

## 🔗 Aşama 3: Orkestrasyon, Dokümantasyon ve Teslimat
- [x] **3.1 Pipeline Scriptleri:** `npm run build`, `npm run test`, `npm run pipeline`
- [x] **3.2 Kapsamlı Dokümantasyon:**
  - `README.md` (Kurulum, config rehberi, DB'ye geçiş rehberi, komutlar & izinler)
  - `ASSUMPTIONS.md` (Kabul edilen mimari kararlar ve varsayımlar)
  - `TEST_REPORT.md` (Test sonuç tablosu ve metrikler)
  - `pipeline.log` (Aşama tamamlama kayıtları)
