# Minecraft Server & Testing Knowledge Rule

## 🌐 MangooHost Test Server Information
- **Panel & Web Console:** `https://mangoohost.org/server/44fa6b0d/console`
- **Server Address:** `tr-1.mangoohost.org:26513` (`zfvcxs.mangoohost.live`)
- **Server Environment:** Paper 26.3-42 (Minecraft 26.3, Protocol 777), GraalVM / Java 25
- **SFTP Connection Details:**
  - Host: `tr-1.mangoohost.org`
  - Port: `2022`
  - User: `dezery7glqwm2.44fa6b0d`
  - Pass: `Ler@n:;58`
  - Target Plugin File: `plugins/StaffCore.jar`
  - Data Directory: `plugins/StaffCore/data/`

## 🤖 Minecraft Protocol 777 (26.3) Test Bots
- Paper 26.3 requires `configuration` phase handling with empty registry sync (`minecraft:known_packs` and ignore zero keepalive `[0,0]`).
- Pre-made test bot scripts (interactive link test, multi-bot stress test, packet monitor, log inspector) are archived at:
  `C:\Users\muham\.gemini\antigravity-ide\brain\639358cd-1288-4844-95e1-e95717612f07\scratch\minecraft_test_scripts.zip`

## 🔨 Fast Plugin Build & Deploy Workflow
1. Recompile: `mvn -f ./plugin compile jar:jar shade:shade -DskipTests`
2. Fast SFTP upload using `ssh2-sftp-client` with algorithms `diffie-hellman-group1-sha1`, `aes128-ctr`, `rsa-sha2-512`.
3. Server Reload / Restart via MangooHost Console button or `/staffcore reload`.
