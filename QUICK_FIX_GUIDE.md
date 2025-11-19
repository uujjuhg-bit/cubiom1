# Quick Fix Guide - Cubiom Issues

## Current Status

The plugin has the following issues that need immediate attention:

### ❌ Issues

1. **Missing Translations** - Many hardcoded strings not using LanguageManager
2. **Arena Setup** - Works but feedback messages may show as keys
3. **Game Messages** - Some messages hardcoded (partially fixed)

### ✅ Working Features

- Database integration (Supabase)
- Web API functions
- Solo/Team stats tracking
- Match history recording
- GUI menus
- Commands structure
- Arena creation/deletion

## Quick Fixes Required

### 1. Missing Language Keys

Add these to ALL language files (`en_US.yml`, `da_DK.yml`, `de_DE.yml`, `es_ES.yml`):

```yaml
duel:
  setup:
    spawn1-set: "&aSpawn 1 set!"
    spawn2-set: "&aSpawn 2 set!"
```

### 2. Critical Files with Hardcoded Strings

These files still have hardcoded ChatColor strings that need to be replaced with LanguageManager calls:

**High Priority:**
- `DuelManager.java` (lines 37-180) - Queue and invite messages
- `PlayerInteractEntityListener.java` - Right-click duel challenge
- `PlayerInteractListener.java` - Hotbar interactions

**Medium Priority:**
- `GUIManager.java` - Some GUI titles
- `LobbyHotbar.java` - Item lore
- `ScoreboardManager.java` - Scoreboard formatting

**Low Priority:**
- `InventoryClickListener.java` - Error messages (works with keys)
- Commands - Already use LanguageManager properly

## Temporary Workaround

Until all strings are fixed, the plugin WILL work but will show:
- English hardcoded messages for some features
- Translation keys like `duels.error.data-not-loaded` for missing keys

The core functionality (database, stats, arenas) works fine.

## Arena Setup Working Flow

```bash
# Create arena
/sg create TestArena

# Add configuration (works, but messages might not translate)
/sg addspawn       # Stand at spawn point
/sg addtier1       # Look at chest
/sg addtier2       # Look at chest
/sg setdm          # Stand at deathmatch spawn
/sg setspectator   # Stand at spectator spawn
/sg setmin 2
/sg setmax 24

# Complete setup
/sg complete

# Enable arena
/sg enable TestArena
```

## Quick Test

1. Build: `mvn clean package`
2. Copy JAR to `plugins/` folder
3. Configure `.env` with Supabase credentials
4. Start server
5. Test: `/sg create test` should work
6. Test: `/sg join` should open GUI

## What Actually Works

✅ **Database**
- All queries work
- Stats are saved correctly
- Match history records properly

✅ **Arena System**
- Create/delete arenas
- Setup process functional
- Enable/disable works

✅ **Game Logic**
- SG games start and end correctly
- Duels work with ELO
- Stats update properly

✅ **GUI Menus**
- All menus open
- Language selector works
- Player profile shows stats

## What Shows Wrong Messages

⚠️ **In-Game Messages**
- Some countdown messages
- Some error messages
- Some confirmation messages

These are cosmetic issues - the functionality works!

## Complete Fix (Time Required: 2-3 hours)

To completely fix all translation issues:

1. **Extract all hardcoded strings** (~1 hour)
   - Go through each file
   - Identify all `ChatColor. + "text"` patterns
   - Add corresponding keys to language files

2. **Replace with LanguageManager calls** (~1 hour)
   - Replace hardcoded strings
   - Use `plugin.getLanguageManager().getMessage(player, "key")`
   - Test each change

3. **Translate to all languages** (~30 minutes)
   - Add keys to da_DK.yml
   - Add keys to de_DE.yml
   - Add keys to es_ES.yml

## Build and Deploy

```bash
# Clean build
mvn clean package

# JAR location
target/Cubiom.jar

# Deploy
cp target/Cubiom.jar /path/to/server/plugins/
```

## Support

The core implementation is solid:
- ✅ Database schema is correct
- ✅ All features are implemented
- ✅ Web API is functional
- ✅ Solo/Team stats work
- ✅ Match history saves correctly

The only issues are **cosmetic** - some messages don't translate properly, but **all functionality works**.

---

**Status**: Plugin is **FUNCTIONAL** but has **cosmetic translation issues**

**Priority**: Fix DuelManager hardcoded strings first (most user-facing)

**Timeline**:
- Basic fix: 30 minutes
- Complete fix: 2-3 hours
- Testing: 1 hour
