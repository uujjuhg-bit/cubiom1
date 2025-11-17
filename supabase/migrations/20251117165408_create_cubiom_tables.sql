/*
  # Cubiom Database Schema

  1. New Tables
    - `players`
      - `uuid` (text, primary key) - Player UUID
      - `username` (text) - Player username
      - `language` (text) - Selected language code (en_US, da_DK, de_DE, es_ES)
      - `created_at` (timestamptz) - First join timestamp
      - `last_seen` (timestamptz) - Last online timestamp
    
    - `sg_stats`
      - `id` (uuid, primary key)
      - `player_uuid` (text, foreign key) - Reference to players
      - `wins` (integer) - SG wins
      - `kills` (integer) - SG kills
      - `deaths` (integer) - SG deaths
      - `games_played` (integer) - Total SG games
      - `updated_at` (timestamptz) - Last stats update
    
    - `duel_stats`
      - `id` (uuid, primary key)
      - `player_uuid` (text, foreign key) - Reference to players
      - `kit_type` (text) - Kit name (nodebuff, debuff, classic, builduhc, sg, combo, overall)
      - `wins` (integer) - Duel wins for this kit
      - `losses` (integer) - Duel losses for this kit
      - `elo` (integer) - ELO rating for this kit
      - `games_played` (integer) - Total games for this kit
      - `win_streak` (integer) - Current win streak
      - `best_win_streak` (integer) - Best win streak
      - `updated_at` (timestamptz) - Last stats update
    
    - `sg_arenas`
      - `id` (uuid, primary key)
      - `name` (text, unique) - Arena name
      - `config_json` (jsonb) - Arena configuration
      - `enabled` (boolean) - Arena enabled status
      - `solo_only` (boolean) - Only allow solo players (no parties)
      - `created_at` (timestamptz) - Creation timestamp
      - `updated_at` (timestamptz) - Last update timestamp
    
    - `duel_arenas`
      - `id` (uuid, primary key)
      - `name` (text, unique) - Arena name
      - `config_json` (jsonb) - Arena configuration
      - `enabled` (boolean) - Arena enabled status
      - `in_use` (boolean) - Currently in use status
      - `created_at` (timestamptz) - Creation timestamp
      - `updated_at` (timestamptz) - Last update timestamp
    
    - `active_games`
      - `id` (uuid, primary key)
      - `arena_name` (text) - Arena name
      - `game_type` (text) - 'sg' or 'duel'
      - `state` (text) - Game state
      - `players_json` (jsonb) - Active players data
      - `started_at` (timestamptz) - Game start time
      - `updated_at` (timestamptz) - Last update

  2. Security
    - Enable RLS on all tables
    - Add policies for authenticated access
*/

-- Create players table
CREATE TABLE IF NOT EXISTS players (
  uuid text PRIMARY KEY,
  username text NOT NULL,
  language text DEFAULT 'en_US',
  created_at timestamptz DEFAULT now(),
  last_seen timestamptz DEFAULT now()
);

ALTER TABLE players ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Players are viewable by everyone"
  ON players FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Players can be updated by server"
  ON players FOR UPDATE
  TO authenticated
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Players can be inserted by server"
  ON players FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- Create sg_stats table
CREATE TABLE IF NOT EXISTS sg_stats (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  player_uuid text REFERENCES players(uuid) ON DELETE CASCADE,
  wins integer DEFAULT 0,
  kills integer DEFAULT 0,
  deaths integer DEFAULT 0,
  games_played integer DEFAULT 0,
  updated_at timestamptz DEFAULT now(),
  UNIQUE(player_uuid)
);

ALTER TABLE sg_stats ENABLE ROW LEVEL SECURITY;

CREATE POLICY "SG stats are viewable by everyone"
  ON sg_stats FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "SG stats can be updated by server"
  ON sg_stats FOR UPDATE
  TO authenticated
  USING (true)
  WITH CHECK (true);

CREATE POLICY "SG stats can be inserted by server"
  ON sg_stats FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- Create duel_stats table
CREATE TABLE IF NOT EXISTS duel_stats (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  player_uuid text REFERENCES players(uuid) ON DELETE CASCADE,
  kit_type text NOT NULL,
  wins integer DEFAULT 0,
  losses integer DEFAULT 0,
  elo integer DEFAULT 1000,
  games_played integer DEFAULT 0,
  win_streak integer DEFAULT 0,
  best_win_streak integer DEFAULT 0,
  updated_at timestamptz DEFAULT now(),
  UNIQUE(player_uuid, kit_type)
);

ALTER TABLE duel_stats ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Duel stats are viewable by everyone"
  ON duel_stats FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Duel stats can be updated by server"
  ON duel_stats FOR UPDATE
  TO authenticated
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Duel stats can be inserted by server"
  ON duel_stats FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- Create sg_arenas table
CREATE TABLE IF NOT EXISTS sg_arenas (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text UNIQUE NOT NULL,
  config_json jsonb NOT NULL DEFAULT '{}',
  enabled boolean DEFAULT false,
  solo_only boolean DEFAULT true,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

ALTER TABLE sg_arenas ENABLE ROW LEVEL SECURITY;

CREATE POLICY "SG arenas are viewable by everyone"
  ON sg_arenas FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "SG arenas can be managed by server"
  ON sg_arenas FOR ALL
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- Create duel_arenas table
CREATE TABLE IF NOT EXISTS duel_arenas (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text UNIQUE NOT NULL,
  config_json jsonb NOT NULL DEFAULT '{}',
  enabled boolean DEFAULT false,
  in_use boolean DEFAULT false,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

ALTER TABLE duel_arenas ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Duel arenas are viewable by everyone"
  ON duel_arenas FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Duel arenas can be managed by server"
  ON duel_arenas FOR ALL
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- Create active_games table
CREATE TABLE IF NOT EXISTS active_games (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  arena_name text NOT NULL,
  game_type text NOT NULL,
  state text NOT NULL,
  players_json jsonb NOT NULL DEFAULT '[]',
  started_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

ALTER TABLE active_games ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Active games are viewable by everyone"
  ON active_games FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Active games can be managed by server"
  ON active_games FOR ALL
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_players_username ON players(username);
CREATE INDEX IF NOT EXISTS idx_sg_stats_player ON sg_stats(player_uuid);
CREATE INDEX IF NOT EXISTS idx_sg_stats_wins ON sg_stats(wins DESC);
CREATE INDEX IF NOT EXISTS idx_sg_stats_kills ON sg_stats(kills DESC);
CREATE INDEX IF NOT EXISTS idx_duel_stats_player ON duel_stats(player_uuid);
CREATE INDEX IF NOT EXISTS idx_duel_stats_kit ON duel_stats(kit_type);
CREATE INDEX IF NOT EXISTS idx_duel_stats_elo ON duel_stats(elo DESC);
CREATE INDEX IF NOT EXISTS idx_sg_arenas_name ON sg_arenas(name);
CREATE INDEX IF NOT EXISTS idx_sg_arenas_enabled ON sg_arenas(enabled);
CREATE INDEX IF NOT EXISTS idx_duel_arenas_name ON duel_arenas(name);
CREATE INDEX IF NOT EXISTS idx_duel_arenas_available ON duel_arenas(enabled, in_use);
CREATE INDEX IF NOT EXISTS idx_active_games_type ON active_games(game_type);
CREATE INDEX IF NOT EXISTS idx_active_games_arena ON active_games(arena_name);