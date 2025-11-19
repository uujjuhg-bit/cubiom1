# Build Fix - DuelGame Compilation Error

## Problem

Maven build fejlede med følgende errors:
```
[ERROR] /src/main/java/com/cubiom/game/duel/DuelGame.java:[146,20] cannot find symbol
  symbol:   method getUniqueId()
  location: variable player1 of type java.util.UUID
```

## Root Cause

I `DuelGame.java` forsøgte koden at kalde `.getUniqueId()` og `.getName()` på `player1` og `player2` variabler, men disse er allerede UUID objekter, ikke Player objekter.

```java
// Fejl: player1 og player2 er UUID, ikke Player
plugin.getSupabaseManager().saveChallengeHistory(
    player1.getUniqueId().toString(),  // ❌ UUID har ikke getUniqueId()
    player1.getName(),                  // ❌ UUID har ikke getName()
    ...
);
```

## Solution

Rettede koden til først at hente Player objekterne fra UUID'erne:

```java
// Fix: Hent Player objekterne først
Player p1 = Bukkit.getPlayer(player1);
Player p2 = Bukkit.getPlayer(player2);

if (p1 != null && p2 != null) {
    plugin.getSupabaseManager().saveChallengeHistory(
        player1.toString(),      // ✅ UUID.toString()
        p1.getName(),            // ✅ Player.getName()
        player2.toString(),      // ✅ UUID.toString()
        p2.getName(),            // ✅ Player.getName()
        kit.getName().toLowerCase(),
        "completed",
        winner.getUniqueId().toString()
    );
}
```

## Changes Made

**File:** `src/main/java/com/cubiom/game/duel/DuelGame.java`

**Location:** Line 145-158 (in `endDuel()` method)

**Before:**
```java
plugin.getSupabaseManager().saveChallengeHistory(
    player1.getUniqueId().toString(),
    player1.getName(),
    player2.getUniqueId().toString(),
    player2.getName(),
    kit.getName().toLowerCase(),
    "completed",
    winner.getUniqueId().toString()
);
```

**After:**
```java
Player p1 = Bukkit.getPlayer(player1);
Player p2 = Bukkit.getPlayer(player2);

if (p1 != null && p2 != null) {
    plugin.getSupabaseManager().saveChallengeHistory(
        player1.toString(),
        p1.getName(),
        player2.toString(),
        p2.getName(),
        kit.getName().toLowerCase(),
        "completed",
        winner.getUniqueId().toString()
    );
}
```

## Verification

The fix ensures:
1. ✅ Player objects are retrieved from UUIDs using `Bukkit.getPlayer()`
2. ✅ Null check prevents NPE if players are offline
3. ✅ UUID is converted to String using `.toString()`
4. ✅ Player names are retrieved from Player objects, not UUIDs
5. ✅ Challenge history only saved if both players are still online

## Build Command

After this fix, the project should build successfully:

```bash
mvn clean package
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: X.XXX s
```

The JAR file will be available at:
```
target/Cubiom.jar
```

## Additional Notes

This fix maintains the original functionality while correcting the type mismatch. The challenge history will now properly record:
- Both player UUIDs (as strings)
- Both player names (retrieved from Player objects)
- Kit type used
- Completion status
- Winner UUID

The null check ensures that if either player has disconnected by the time the duel ends, the challenge history won't be saved (preventing incomplete records).

---

**Fix Applied:** November 19, 2025
**Status:** ✅ Ready to build
