# Cubiom Website API Integration Guide

This guide explains how to integrate the Cubiom Minecraft server statistics and leaderboards into a website using Supabase's built-in REST API and database functions.

## Table of Contents

1. [Overview](#overview)
2. [Database Setup](#database-setup)
3. [API Endpoints](#api-endpoints)
4. [Frontend Implementation](#frontend-implementation)
5. [Example Code](#example-code)
6. [Security Considerations](#security-considerations)

---

## Overview

The Cubiom plugin stores all player statistics, match history, and leaderboards in a Supabase database. The website can directly query this data using Supabase's REST API with the anonymous key, which has read-only access to public data.

### Key Features Available

- **Player Profiles**: Complete stats for Survival Games (solo/team) and Duels (all kits)
- **Leaderboards**: Real-time rankings for SG and Duels
- **Match History**: Recent games and player-specific match records
- **Server Statistics**: Total players, games played, popular arenas
- **Player Search**: Find players by username
- **Achievements**: Track player milestones and accomplishments

---

## Database Setup

### Available Tables

#### Players Table
- `uuid` - Player UUID
- `username` - Player name
- `language` - Selected language (en_US, da_DK, de_DE, es_ES)
- `created_at` - First join date
- `last_seen` - Last online timestamp

#### SG Stats Table
- `player_uuid` - Reference to player
- `player_name` - Player username (for quick lookups)
- `wins`, `kills`, `deaths`, `games_played` - Total stats
- `solo_wins`, `solo_kills`, `solo_deaths`, `solo_games_played` - Solo mode stats
- `team_wins`, `team_kills`, `team_deaths`, `team_games_played` - Team mode stats

#### Duel Stats Table
- `player_uuid` - Reference to player
- `player_name` - Player username
- `kit_type` - Kit name (nodebuff, debuff, classic, builduhc, combo, sg)
- `elo` - ELO rating
- `wins`, `losses`, `games_played`
- `win_streak`, `best_win_streak`

#### Match History Table
- `game_type` - 'sg' or 'duel'
- `arena_name` - Arena where match was played
- `winner_uuid`, `winner_name` - Match winner
- `participants` - JSON array of all players
- `duration_seconds` - Match duration
- `stats_json` - Additional game statistics
- `ended_at` - Match end timestamp

### Database Functions

The following PostgreSQL functions are available for complex queries:

#### `get_player_profile(uuid text)`
Returns complete player profile with all stats, recent matches, and achievements.

#### `get_player_rank(uuid text, stat_type text, kit text)`
Returns player's leaderboard position for a specific stat type.

#### `get_leaderboard(game_type text, stat_type text, limit int, offset int, kit text)`
Returns paginated leaderboard data.

#### `get_server_statistics()`
Returns aggregated server statistics including total players, games, and popular arenas.

---

## API Endpoints

All endpoints are accessed through Supabase's REST API at:
```
https://YOUR_PROJECT.supabase.co/rest/v1/
```

### Authentication

Use the `VITE_SUPABASE_ANON_KEY` from your `.env` file:

```javascript
const headers = {
  'apikey': 'YOUR_ANON_KEY',
  'Authorization': 'Bearer YOUR_ANON_KEY'
}
```

### Available Endpoints

#### 1. Get Player Profile

```http
POST /rest/v1/rpc/get_player_profile
Content-Type: application/json

{
  "p_uuid": "player-uuid-here"
}
```

**Response:**
```json
{
  "player": {
    "uuid": "...",
    "username": "PlayerName",
    "language": "en_US",
    "created_at": "2024-01-01T00:00:00Z",
    "last_seen": "2024-01-15T12:00:00Z"
  },
  "sg_stats": {
    "total": {
      "wins": 150,
      "kills": 500,
      "deaths": 200,
      "games_played": 300,
      "kd_ratio": 2.5
    },
    "solo": {
      "wins": 100,
      "kills": 350,
      "deaths": 150,
      "games_played": 200,
      "kd_ratio": 2.33
    },
    "team": {
      "wins": 50,
      "kills": 150,
      "deaths": 50,
      "games_played": 100,
      "kd_ratio": 3.0
    }
  },
  "duel_stats": [
    {
      "kit_type": "nodebuff",
      "elo": 1500,
      "wins": 75,
      "losses": 25,
      "games_played": 100,
      "win_streak": 5,
      "best_win_streak": 12,
      "win_rate": 75.0
    }
  ],
  "recent_matches": [...],
  "achievements": [...]
}
```

#### 2. Search Player by Username

```http
GET /rest/v1/players?username=ilike.Player*
```

#### 3. Get Leaderboard

```http
POST /rest/v1/rpc/get_leaderboard
Content-Type: application/json

{
  "game_type": "sg",
  "stat_type": "wins",
  "page_limit": 10,
  "page_offset": 0
}
```

For duel leaderboards:
```json
{
  "game_type": "duel",
  "stat_type": "elo",
  "page_limit": 10,
  "page_offset": 0,
  "kit": "nodebuff"
}
```

#### 4. Get Server Statistics

```http
POST /rest/v1/rpc/get_server_statistics
Content-Type: application/json

{}
```

#### 5. Get Recent Matches

```http
GET /rest/v1/match_history?order=ended_at.desc&limit=10
```

#### 6. Get Player Rank

```http
POST /rest/v1/rpc/get_player_rank
Content-Type: application/json

{
  "p_uuid": "player-uuid",
  "stat_type": "sg_wins"
}
```

For duel ranks:
```json
{
  "p_uuid": "player-uuid",
  "stat_type": "duel_elo",
  "kit": "nodebuff"
}
```

---

## Frontend Implementation

### Recommended Tech Stack

- **Framework**: Next.js 14+ with App Router
- **Language**: TypeScript
- **Styling**: TailwindCSS
- **Database Client**: @supabase/supabase-js
- **Charts**: Recharts or Chart.js
- **State Management**: React Context or Zustand

### Project Structure

```
website/
├── app/
│   ├── page.tsx                    # Homepage
│   ├── player/
│   │   └── [uuid]/
│   │       └── page.tsx           # Player profile
│   ├── leaderboards/
│   │   ├── survival-games/
│   │   │   └── page.tsx           # SG leaderboard
│   │   └── duels/
│   │       └── [kit]/
│   │           └── page.tsx       # Duel leaderboard by kit
│   └── api/
│       ├── player/
│       │   └── [uuid]/
│       │       └── route.ts       # API route for player data
│       └── leaderboard/
│           └── route.ts           # API route for leaderboards
├── components/
│   ├── PlayerCard.tsx             # Player profile component
│   ├── LeaderboardTable.tsx       # Leaderboard display
│   ├── StatCard.tsx               # Individual stat display
│   ├── MatchHistory.tsx           # Match history list
│   └── ServerStats.tsx            # Server statistics widget
├── lib/
│   ├── supabase.ts                # Supabase client setup
│   └── types.ts                   # TypeScript types
└── public/
    └── images/                     # Kit icons, flags, etc.
```

### Supabase Client Setup

```typescript
// lib/supabase.ts
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL!
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
```

### TypeScript Types

```typescript
// lib/types.ts
export interface Player {
  uuid: string
  username: string
  language: string
  created_at: string
  last_seen: string
}

export interface SGStats {
  total: {
    wins: number
    kills: number
    deaths: number
    games_played: number
    kd_ratio: number
  }
  solo: {
    wins: number
    kills: number
    deaths: number
    games_played: number
    kd_ratio: number
  }
  team: {
    wins: number
    kills: number
    deaths: number
    games_played: number
    kd_ratio: number
  }
}

export interface DuelStats {
  kit_type: string
  elo: number
  wins: number
  losses: number
  games_played: number
  win_streak: number
  best_win_streak: number
  win_rate: number
}

export interface PlayerProfile {
  player: Player
  sg_stats: SGStats
  duel_stats: DuelStats[]
  recent_matches: Match[]
  achievements: Achievement[]
}

export interface Match {
  game_type: string
  arena_name: string
  winner_name: string
  duration_seconds: number
  ended_at: string
}

export interface LeaderboardEntry {
  rank: number
  player_uuid: string
  player_name: string
  wins?: number
  kills?: number
  deaths?: number
  elo?: number
  games_played: number
  kd_ratio?: number
  win_rate?: number
}
```

---

## Example Code

### Fetching Player Profile

```typescript
// app/player/[uuid]/page.tsx
import { supabase } from '@/lib/supabase'
import { PlayerProfile } from '@/lib/types'

export default async function PlayerPage({
  params
}: {
  params: { uuid: string }
}) {
  const { data, error } = await supabase.rpc('get_player_profile', {
    p_uuid: params.uuid
  })

  if (error || !data) {
    return <div>Player not found</div>
  }

  const profile: PlayerProfile = data

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold mb-8">
        {profile.player.username}'s Profile
      </h1>

      {/* Survival Games Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <StatCard
          title="Total SG Stats"
          stats={profile.sg_stats.total}
        />
        <StatCard
          title="Solo SG Stats"
          stats={profile.sg_stats.solo}
        />
        <StatCard
          title="Team SG Stats"
          stats={profile.sg_stats.team}
        />
      </div>

      {/* Duel Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        {profile.duel_stats.map(kit => (
          <DuelStatCard key={kit.kit_type} kit={kit} />
        ))}
      </div>

      {/* Recent Matches */}
      <MatchHistory matches={profile.recent_matches} />
    </div>
  )
}
```

### Fetching Leaderboard

```typescript
// app/leaderboards/survival-games/page.tsx
import { supabase } from '@/lib/supabase'
import { LeaderboardEntry } from '@/lib/types'

export default async function SGLeaderboard() {
  const { data, error } = await supabase.rpc('get_leaderboard', {
    game_type: 'sg',
    stat_type: 'wins',
    page_limit: 100,
    page_offset: 0
  })

  if (error || !data) {
    return <div>Failed to load leaderboard</div>
  }

  const leaderboard: LeaderboardEntry[] = data

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold mb-8">
        Survival Games Leaderboard
      </h1>

      <table className="w-full">
        <thead>
          <tr>
            <th>Rank</th>
            <th>Player</th>
            <th>Wins</th>
            <th>Kills</th>
            <th>K/D</th>
            <th>Games</th>
          </tr>
        </thead>
        <tbody>
          {leaderboard.map(entry => (
            <tr key={entry.player_uuid}>
              <td>{entry.rank}</td>
              <td>
                <a href={`/player/${entry.player_uuid}`}>
                  {entry.player_name}
                </a>
              </td>
              <td>{entry.wins}</td>
              <td>{entry.kills}</td>
              <td>{entry.kd_ratio}</td>
              <td>{entry.games_played}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
```

### Real-time Updates (Optional)

For live leaderboard updates:

```typescript
import { useEffect, useState } from 'react'
import { supabase } from '@/lib/supabase'

export function useRealtimeLeaderboard() {
  const [data, setData] = useState([])

  useEffect(() => {
    // Subscribe to changes
    const channel = supabase
      .channel('leaderboard-changes')
      .on(
        'postgres_changes',
        {
          event: '*',
          schema: 'public',
          table: 'sg_stats'
        },
        () => {
          // Refetch leaderboard when stats change
          fetchLeaderboard()
        }
      )
      .subscribe()

    return () => {
      supabase.removeChannel(channel)
    }
  }, [])

  return data
}
```

### Player Search Component

```typescript
'use client'

import { useState } from 'react'
import { supabase } from '@/lib/supabase'
import { useRouter } from 'next/navigation'

export function PlayerSearch() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const router = useRouter()

  const searchPlayers = async (searchTerm: string) => {
    if (searchTerm.length < 2) {
      setResults([])
      return
    }

    const { data } = await supabase
      .from('players')
      .select('uuid, username')
      .ilike('username', `${searchTerm}%`)
      .limit(5)

    setResults(data || [])
  }

  return (
    <div className="relative">
      <input
        type="text"
        value={query}
        onChange={(e) => {
          setQuery(e.target.value)
          searchPlayers(e.target.value)
        }}
        placeholder="Search players..."
        className="px-4 py-2 border rounded-lg w-64"
      />

      {results.length > 0 && (
        <div className="absolute mt-2 w-64 bg-white border rounded-lg shadow-lg">
          {results.map((player) => (
            <button
              key={player.uuid}
              onClick={() => router.push(`/player/${player.uuid}`)}
              className="block w-full text-left px-4 py-2 hover:bg-gray-100"
            >
              {player.username}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
```

---

## Security Considerations

### Row Level Security (RLS)

The database has RLS enabled with policies that:
- Allow anonymous (unauthenticated) users to **read** all public data
- Restrict **write** operations to authenticated server connections only

### API Key Protection

- The `VITE_SUPABASE_ANON_KEY` is safe to expose in client-side code
- It only grants read access to public tables
- Write operations require the service role key (never exposed to clients)

### Rate Limiting

Consider implementing rate limiting on your Next.js API routes:

```typescript
// lib/rate-limit.ts
import { LRUCache } from 'lru-cache'

const rateLimit = new LRUCache({
  max: 500,
  ttl: 60000, // 1 minute
})

export function rateLimiter(identifier: string) {
  const tokenCount = (rateLimit.get(identifier) as number) || 0

  if (tokenCount > 100) {
    return false
  }

  rateLimit.set(identifier, tokenCount + 1)
  return true
}
```

### Caching

Implement caching to reduce database load:

```typescript
// Use Next.js built-in caching
export const revalidate = 60 // Revalidate every 60 seconds

// Or use Redis/Vercel KV for more control
import { kv } from '@vercel/kv'

async function getCachedLeaderboard() {
  const cached = await kv.get('leaderboard:sg:wins')

  if (cached) return cached

  const fresh = await fetchLeaderboard()
  await kv.set('leaderboard:sg:wins', fresh, { ex: 60 })

  return fresh
}
```

---

## Deployment

### Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_SUPABASE_URL=https://dubuyzhrvhpezzbdenbo.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key-here
```

### Vercel Deployment

```bash
npm install
npm run build
vercel --prod
```

### Docker Deployment

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

---

## Additional Features to Implement

### Suggested Enhancements

1. **Player Comparison**: Compare two players side-by-side
2. **Statistics Graphs**: Visualize player performance over time
3. **Achievement Showcase**: Display unlocked achievements with progress bars
4. **Live Match Viewer**: Show currently active games
5. **Arena Statistics**: Popular arenas and map performance
6. **Kit Meta Analysis**: Most played and successful kits
7. **Historical Data**: Track player progress over weeks/months
8. **Social Features**: Player profiles, avatars from Minecraft skins
9. **Leaderboard Filters**: By time period, game mode, region
10. **Notifications**: Subscribe to player updates, new achievements

---

## Support and Resources

- **Supabase Documentation**: https://supabase.com/docs
- **Next.js Documentation**: https://nextjs.org/docs
- **Cubiom Plugin Repository**: Link to your GitHub repo
- **Discord Support**: Your Discord server link

For questions or issues, please contact the development team or open an issue on GitHub.

---

## License

This integration guide is provided as-is for use with the Cubiom Minecraft server plugin.
