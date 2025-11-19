# Cubiom Minecraft Plugin

**Minecraft 1.8.8 PvP Plugin** - Survival Games & Duels with Web Integration

---

## Quick Start

### Build
```bash
mvn clean package
```

### Install
1. Place `target/Cubiom.jar` in your server's `plugins/` folder
2. Configure Supabase credentials in `.env` file
3. Restart server
4. Setup arenas with `/sg create` and `/duel create`

---

## Features

### Core Gameplay
- **Survival Games** - MCSG-style battle royale (solo & team modes)
- **Duels System** - 1v1 PvP with ELO ranking across 6 kits
- **Multi-Language** - English, Danish, German, Spanish support
- **Smart Lobby Hotbar** - 5 interactive items for easy navigation
- **World Rollback** - Automatic arena restoration after games

### Statistics & Tracking
- **Detailed Stats** - Separate tracking for solo/team Survival Games
- **ELO System** - Per-kit ELO ratings for duels
- **Match History** - Complete game records saved to database
- **Challenge History** - Player-to-player duel tracking
- **Achievements** - Progress tracking system

### Web Integration
- **REST API** - Full Supabase integration for website access
- **Real-time Leaderboards** - Live rankings for all game modes
- **Player Profiles** - Comprehensive stats accessible via web
- **Server Statistics** - Aggregate data for website display

---

## Database

### Supabase Integration

The plugin uses Supabase for all data persistence:

- **players** - Player accounts and preferences
- **sg_stats** - Survival Games statistics (total, solo, team)
- **duel_stats** - Duel statistics per kit
- **match_history** - Complete game records
- **duel_challenges** - Challenge tracking
- **arena_statistics** - Arena usage metrics

### Environment Setup

Create `.env` file in server root:
```env
SUPABASE_URL=your_project_url
SUPABASE_ANON_KEY=your_anon_key
```

---

## Commands

### Players
```
/sg join [arena]      - Join Survival Games
/sg leave             - Leave current game
/duel join <kit>      - Join duel queue
/duel invite <player> - Challenge player to duel
/duel accept          - Accept duel invite
/duel decline         - Decline duel invite
/lang set <code>      - Change language (en_US, da_DK, de_DE, es_ES)
/stats                - View your statistics
/top [type]           - View leaderboards
```

### Admins
```
/sg create <name>     - Create new SG arena
/sg delete <name>     - Delete SG arena
/sg enable <name>     - Enable arena
/sg disable <name>    - Disable arena
/sg setmin <number>   - Set minimum players
/sg setmax <number>   - Set maximum players
/sg addspawn          - Add player spawn point
/sg addtier1          - Add tier 1 chest
/sg addtier2          - Add tier 2 chest
/sg setdm             - Set deathmatch spawn
/sg setspectator      - Set spectator spawn
/sg complete          - Complete arena setup
/sg cancel            - Cancel arena setup

/duel create <name>   - Create new duel arena
/duel delete <name>   - Delete duel arena
/duel enable <name>   - Enable arena
/duel disable <name>  - Disable arena

/cubiom reload        - Reload plugin
/cubiom setlobby      - Set lobby spawn
```

Full command list in **SETUP_GUIDE.md**

---

## Documentation

### Setup & Configuration
- **SETUP_GUIDE.md** - Complete setup and configuration guide
- **IMPLEMENTATION_SUMMARY.md** - Technical implementation details

### Web Development
- **WEBSITE_API_GUIDE.md** - Complete API reference for website integration
- **FINAL_IMPLEMENTATION_REPORT.md** - Comprehensive feature documentation

---

## Website Integration

The plugin provides a complete REST API through Supabase for building a companion website.

### Quick Start

```bash
# Create Next.js website
npx create-next-app@latest cubiom-website --typescript --tailwind
npm install @supabase/supabase-js
```

### API Examples

```typescript
// Get player profile
const { data } = await supabase.rpc('get_player_profile', {
  p_uuid: 'player-uuid'
})

// Get leaderboard
const { data } = await supabase.rpc('get_leaderboard', {
  game_type: 'sg',
  stat_type: 'wins',
  page_limit: 100,
  page_offset: 0
})
```

See **WEBSITE_API_GUIDE.md** for complete documentation.

---

## Architecture

### Tech Stack
- **Platform**: Spigot 1.8.8
- **Language**: Java 8
- **Database**: Supabase (PostgreSQL)
- **Build**: Maven

### Key Components
- **Game Managers** - SGManager, DuelManager for game flow
- **Arena System** - Dynamic arena setup and management
- **Player Manager** - State tracking and data management
- **GUI Manager** - Interactive menu system
- **Language Manager** - Multi-language support
- **Supabase Manager** - Database operations and web API

---

## Statistics

### Survival Games
- **Total Stats** - Combined solo + team performance
- **Solo Stats** - Individual gameplay statistics
- **Team Stats** - Team gameplay statistics
- Tracks: Wins, Kills, Deaths, K/D Ratio, Games Played

### Duels
- **Per-Kit Statistics** - Individual stats for each kit
- **ELO Ratings** - Skill-based matchmaking rankings
- **Win Streaks** - Current and best win streaks
- **Win Rate** - Percentage calculation
- Kits: NoDebuff, Debuff, Classic, BuildUHC, Combo, SG

---

## Development

### Building from Source

```bash
git clone <repository>
cd cubiom
mvn clean package
```

### Project Structure
```
cubiom/
├── src/main/java/com/cubiom/
│   ├── arena/          # Arena management
│   ├── commands/       # Command handlers
│   ├── core/           # Core enums and interfaces
│   ├── data/           # Data management
│   ├── database/       # Supabase integration
│   ├── game/           # Game logic (SG, Duels)
│   ├── language/       # Multi-language system
│   ├── listeners/      # Event handlers
│   ├── player/         # Player management
│   ├── ui/             # GUI and lobby system
│   └── world/          # World management
├── src/main/resources/
│   ├── languages/      # Translation files
│   ├── config.yml      # Main configuration
│   └── plugin.yml      # Plugin metadata
└── supabase/
    └── migrations/     # Database schema
```

---

## Contributing

This is a private project. For questions or support, contact the development team.

---

## License

**Private License** - All rights reserved

---

**Version:** 2.0
**Platform:** Spigot 1.8.8
**Last Updated:** November 2025

**Key Features:**
- ✅ Survival Games (Solo & Team)
- ✅ Duels with ELO System
- ✅ Multi-Language Support
- ✅ Web API Integration
- ✅ Match History Tracking
- ✅ Challenge System
- ✅ Real-time Leaderboards
