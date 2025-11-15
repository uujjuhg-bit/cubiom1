# 🎮 CUBIOM PLUGIN - COMPLETE SETUP GUIDE

## 📋 **TABLE OF CONTENTS**
1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [World Setup](#world-setup)
4. [Arena Configuration](#arena-configuration)
5. [Testing](#testing)
6. [Troubleshooting](#troubleshooting)

---

## 🔧 **PREREQUISITES**

### Required:
- **Spigot 1.8.8** server
- **Java 8** or higher
- **Maven** (for building)

### Optional but Recommended:
- **Multiverse-Core** (for world management)
- **WorldEdit** (for area selection)

---

## 📦 **INSTALLATION**

### Step 1: Build the Plugin

```bash
cd /tmp/cc-agent/60230619/project
mvn clean package
```

This creates: `target/Cubiom.jar`

### Step 2: Install on Server

```bash
# Copy JAR to plugins folder
cp target/Cubiom.jar /path/to/server/plugins/

# Start your server
cd /path/to/server
java -Xmx2G -jar spigot-1.8.8.jar
```

### Step 3: Verify Installation

When server starts, you should see:
```
[Cubiom] Enabling Cubiom v1.0...
[Cubiom] Loading configuration...
[Cubiom] Loading data files...
[Cubiom] Loading language files...
[Cubiom] Loaded language: en_US
[Cubiom] Loaded language: da_DK
[Cubiom] Loaded language: de_DE
[Cubiom] Loaded language: es_ES
[Cubiom] Cubiom enabled successfully!
```

---

## 🌍 **WORLD SETUP**

### Option A: With Multiverse-Core (Recommended)

1. **Install Multiverse-Core**
```bash
# Download latest from: https://dev.bukkit.org/projects/multiverse-core
# Place in plugins/ folder
```

2. **Create SG World**
```
/mv create sg_world NORMAL
/mv tp sg_world
```

3. **Configure World (Automatic)**
The plugin will automatically detect Multiverse and set:
- PVP: Enabled
- Monsters: Disabled
- Animals: Disabled
- Weather: Disabled
- Difficulty: Normal

### Option B: Without Multiverse (Manual)

1. **Create world folder**
```bash
mkdir server/sg_world
```

2. **Restart server** to generate world

3. **Set gamerules manually**
```
/gamerule doMobSpawning false
/gamerule mobGriefing false
/gamerule doFireTick false
/gamerule doDaylightCycle false
/gamerule doWeatherCycle false
/gamerule showDeathMessages false
```

### World Configuration Checklist

For **Survival Games World**:
- ✅ PVP enabled
- ✅ No monsters
- ✅ No fire spread
- ✅ No day/night cycle
- ✅ No weather changes
- ✅ Mob griefing disabled

For **Duel World**:
- ✅ All of above PLUS:
- ✅ Natural regeneration disabled
- ✅ Hunger disabled (if using Multiverse)

---

## 🏟️ **ARENA CONFIGURATION**

### Survival Games Arena

#### Method 1: In-Game Commands (Recommended)

1. **Go to your SG world**
```
/mv tp sg_world
```

2. **Create arena**
```
/sg create arena1
```

3. **Set lobby spawn**
Stand where players should spawn in lobby:
```
/sg setlobby
```

4. **Add player spawns** (at least 8+)
Stand at each spawn location:
```
/sg addspawn
/sg addspawn
/sg addspawn
... (repeat for all spawn points)
```

5. **Add Tier 1 chests**
Look at each chest and run:
```
/sg addtier1
```

6. **Add Tier 2 chests** (optional)
Look at each chest and run:
```
/sg addtier2
```

7. **Set deathmatch spawn**
Stand at deathmatch center:
```
/sg setdm
```

8. **Configure player limits**
```
/sg setmin 8
/sg setmax 24
```

9. **Enable arena**
```
/sg enable
```

10. **Verify**
```
/sg info arena1
```

#### Method 2: Manual JSON Configuration

Edit `plugins/Cubiom/data/arenas.json`:

```json
{
  "arena1": {
    "name": "arena1",
    "worldName": "sg_world",
    "lobbySpawn": {
      "world": "sg_world",
      "x": 0.5,
      "y": 100.0,
      "z": 0.5,
      "yaw": 0.0,
      "pitch": 0.0
    },
    "spawnPoints": [
      {
        "world": "sg_world",
        "x": 10.5,
        "y": 100.0,
        "z": 10.5,
        "yaw": 45.0,
        "pitch": 0.0
      }
    ],
    "tier1Chests": [
      {
        "world": "sg_world",
        "x": 5,
        "y": 100,
        "z": 5
      }
    ],
    "tier2Chests": [],
    "deathmatchSpawn": {
      "world": "sg_world",
      "x": 0.5,
      "y": 100.0,
      "z": 0.5,
      "yaw": 0.0,
      "pitch": 0.0
    },
    "minPlayers": 8,
    "maxPlayers": 24,
    "enabled": true
  }
}
```

After editing, reload:
```
/cubiom reload
```

### Duel Arena

#### In-Game Setup:

1. **Create arena**
```
/duel create duel1
```

2. **Mark corners** (define arena boundaries)
Stand at corner 1:
```
/duel setpos1
```

Stand at opposite corner:
```
/duel setpos2
```

3. **Set player spawns**
Stand at player 1 spawn:
```
/duel setspawn1
```

Stand at player 2 spawn:
```
/duel setspawn2
```

4. **Enable**
```
/duel enable
```

5. **Verify**
```
/duel info duel1
```

---

## 🧪 **TESTING**

### Test Checklist

#### Lobby System
- [ ] Player joins server
- [ ] Receives 5 hotbar items automatically
- [ ] Right-click Diamond Sword opens SG menu
- [ ] Right-click Iron Sword opens Duels menu
- [ ] Right-click Book opens stats
- [ ] Right-click Emerald opens leaderboards
- [ ] Right-click Name Tag opens language menu

#### Survival Games
- [ ] `/sg join` joins game
- [ ] Countdown starts with 8+ players
- [ ] Game starts, players teleport to spawns
- [ ] Grace period prevents PvP (60 seconds)
- [ ] After grace period, PvP enabled
- [ ] Player death → spectator mode
- [ ] Last player alive wins
- [ ] Stats update correctly
- [ ] World resets perfectly after game

#### Duels
- [ ] `/duel join` enters queue
- [ ] Two players in queue → match starts
- [ ] 5-second countdown
- [ ] PvP works
- [ ] Death ends duel
- [ ] ELO updates correctly
- [ ] Stats update
- [ ] Arena becomes available again

#### Languages
- [ ] `/lang set da_DK` switches to Danish
- [ ] Hotbar items update to new language
- [ ] GUI menus show in new language
- [ ] Messages show in new language

#### Admin Commands
- [ ] `/sg create test` works
- [ ] Can configure full arena
- [ ] `/sg enable` saves arena
- [ ] `/sg list` shows arenas
- [ ] `/duel create test` works
- [ ] Can configure duel arena

---

## 🔍 **TROUBLESHOOTING**

### Problem: "No arenas available" when joining SG

**Solution:**
1. Check arena exists: `/sg list`
2. Check arena is enabled: `/sg info arena1`
3. Verify arena is valid (has all required components)
4. Check world is loaded

### Problem: World not resetting after game

**Solution:**
1. Verify WorldSnapshot is capturing correctly
2. Check console for errors
3. Ensure chunks are loaded
4. Try manually: `/cubiom reload`

### Problem: Players can't join game

**Solution:**
1. Check minimum players setting
2. Verify arena is enabled
3. Check if game already running
4. Ensure world exists

### Problem: Chests not refilling

**Solution:**
1. Verify chests were added correctly: `/sg info arena1`
2. Check chest locations are valid
3. Ensure LootManager is working (check console)

### Problem: Multiverse not detected

**Solution:**
1. Verify Multiverse-Core is installed and enabled
2. Check plugin load order in `bukkit.yml`:
```yaml
plugin-load-order:
  startup:
    - Multiverse-Core
  default:
    - Cubiom
```

### Problem: Stats not saving

**Solution:**
1. Check `plugins/Cubiom/data/` folder exists
2. Verify write permissions
3. Check console for save errors
4. Use `/cubiom reload` to force save

### Problem: Language not changing

**Solution:**
1. Verify language code: `/lang list`
2. Check language files exist in `plugins/Cubiom/languages/`
3. Try: `/cubiom reload`
4. Log out and back in

---

## 📊 **DEFAULT CONFIGURATION**

### config.yml
```yaml
cubiom:
  prefix: "&b&lCUBIOM &8»&r "
  default-language: en_US

survival-games:
  min-players: 8
  max-players: 24
  grace-period: 60      # seconds - no PvP
  refill-time: 180      # seconds - chest refill
  deathmatch-time: 600  # seconds - until deathmatch
  countdown: 10         # seconds - pre-game countdown

duels:
  min-elo: 0
  default-elo: 1000
  elo-k-factor: 32
  duel-timeout: 600

hub:
  world: world
  spawn:
    x: 0
    y: 100
    z: 0

stats:
  save-interval: 300    # seconds - auto-save
  auto-backup: true
```

---

## 🎯 **QUICK START SUMMARY**

### Absolute Minimum Setup (5 minutes):

1. **Build & Install**
```bash
mvn clean package
cp target/Cubiom.jar server/plugins/
```

2. **Start Server**
```bash
cd server && java -jar spigot-1.8.8.jar
```

3. **Create World**
```
/mv create sg_world NORMAL
/mv tp sg_world
```

4. **Create Arena**
```
/sg create arena1
/sg setlobby
/sg addspawn (repeat 8 times in different locations)
/sg addtier1 (look at chests, repeat 10+ times)
/sg setdm
/sg setmin 2
/sg setmax 24
/sg enable
```

5. **Test**
```
/sg join
```

**Done!** Players can now use the lobby hotbar and join games!

---

## 📞 **SUPPORT**

### Useful Commands

```bash
# Plugin info
/cubiom version

# Reload plugin
/cubiom reload

# List all arenas
/sg list
/duel list

# Arena info
/sg info <name>
/duel info <name>

# Player stats
/sg stats
/duel stats

# Language
/lang set en_US
```

### File Locations

```
plugins/Cubiom/
├── config.yml                    # Main config
├── data/
│   ├── arenas.json              # SG arenas
│   ├── duel-arenas.json         # Duel arenas
│   ├── kits.json                # Kit definitions
│   ├── player-stats.json        # All player stats
│   └── player-languages.json    # Language prefs
└── languages/
    ├── en_US.yml                # English
    ├── da_DK.yml                # Danish
    ├── de_DE.yml                # German
    └── es_ES.yml                # Spanish
```

---

## ✅ **SETUP COMPLETE!**

Your Cubiom PvP server is now ready for players!

**Next Steps:**
- Invite players to test
- Adjust config values as needed
- Create more arenas
- Monitor stats and gameplay

**Enjoy your professional Minecraft PvP server!** 🎮🚀
