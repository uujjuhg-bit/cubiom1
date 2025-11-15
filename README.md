# Cubiom Minecraft Plugin

**Minecraft 1.8.8 PvP Plugin** - Survival Games & Duels

---

## Quick Start

### Build
```bash
mvn clean package
```

### Install
Place `target/Cubiom.jar` in your server's `plugins/` folder.

---

## Features

- **Survival Games** (MCSG-style)
- **Duels System** (1v1 PvP with ELO)
- **Multi-Language** (English, Danish, German, Spanish)
- **Smart Lobby Hotbar** (5 interactive items)
- **World Rollback** (automatic arena restoration)
- **Stats Tracking** (wins, kills, deaths, KDR, ELO)

---

## Configuration

See **SETUP_GUIDE.md** for complete setup instructions.

---

## Commands

### Players
```
/sg join              - Join Survival Games
/duel join            - Join duel queue
/lang set <code>      - Change language (en_US, da_DK, de_DE, es_ES)
```

### Admins
```
/sg create <name>     - Create new SG arena
/duel create <name>   - Create new duel arena
/cubiom reload        - Reload plugin
```

Full command list in **SETUP_GUIDE.md**

---

## Documentation

- **SETUP_GUIDE.md** - Complete setup and configuration guide
- **README.md** - This file

---

**Version:** 1.0
**Platform:** Spigot 1.8.8
**License:** Private
