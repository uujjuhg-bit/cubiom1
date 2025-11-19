/*
  # Extend Cubiom Schema for Web API and Team Statistics

  ## Overview
  This migration extends the existing schema to support:
  - Solo vs Team Survival Games statistics
  - Match history tracking for website display
  - Player achievements system
  - Challenge history tracking
  - Arena usage statistics
  - Database functions for optimized web queries
  - Views for efficient leaderboard access

  ## Changes

  ### 1. Extended Statistics Tables
  - Add solo/team columns to sg_stats
  - Add player_name column for faster lookups

  ### 2. New Tables
  - match_history: Detailed game records for website
  - player_achievements: Achievement tracking
  - duel_challenges: Challenge history between players
  - arena_statistics: Track arena usage and performance

  ### 3. Database Functions
  - get_player_profile(): Complete player data in one query
  - get_player_rank(): Calculate player's leaderboard position
  - get_leaderboard(): Optimized paginated leaderboards
  - get_server_statistics(): Aggregated server stats

  ### 4. Views
  - player_stats_view: Combined player data with all stats
  - sg_leaderboard_view: Pre-calculated SG rankings
  - duel_leaderboard_view: Pre-calculated duel rankings

  ### 5. Security
  - Enable public read access for anonymous users (website)
  - Maintain write restrictions for authenticated server only
*/

-- ============================================================================
-- EXTEND EXISTING TABLES
-- ============================================================================

-- Add solo/team stats and player_name to sg_stats
DO $$
BEGIN
  -- Add player_name column
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'sg_stats' AND column_name = 'player_name'
  ) THEN
    ALTER TABLE sg_stats ADD COLUMN player_name text;
  END IF;

  -- Add solo stats columns
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'sg_stats' AND column_name = 'solo_wins'
  ) THEN
    ALTER TABLE sg_stats ADD COLUMN solo_wins integer DEFAULT 0;
    ALTER TABLE sg_stats ADD COLUMN solo_kills integer DEFAULT 0;
    ALTER TABLE sg_stats ADD COLUMN solo_deaths integer DEFAULT 0;
    ALTER TABLE sg_stats ADD COLUMN solo_games_played integer DEFAULT 0;
  END IF;

  -- Add team stats columns
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'sg_stats' AND column_name = 'team_wins'
  ) THEN
    ALTER TABLE sg_stats ADD COLUMN team_wins integer DEFAULT 0;
    ALTER TABLE sg_stats ADD COLUMN team_kills integer DEFAULT 0;
    ALTER TABLE sg_stats ADD COLUMN team_deaths integer DEFAULT 0;
    ALTER TABLE sg_stats ADD COLUMN team_games_played integer DEFAULT 0;
  END IF;
END $$;

-- Add player_name to duel_stats for faster lookups
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'duel_stats' AND column_name = 'player_name'
  ) THEN
    ALTER TABLE duel_stats ADD COLUMN player_name text;
  END IF;
END $$;

-- ============================================================================
-- CREATE NEW TABLES
-- ============================================================================

-- Match history for website display
CREATE TABLE IF NOT EXISTS match_history (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  game_type text NOT NULL,
  arena_name text NOT NULL,
  winner_uuid text,
  winner_name text,
  participants jsonb NOT NULL DEFAULT '[]',
  duration_seconds integer,
  ended_at timestamptz DEFAULT now(),
  stats_json jsonb NOT NULL DEFAULT '{}'
);

ALTER TABLE match_history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Match history is viewable by everyone"
  ON match_history FOR SELECT
  TO anon, authenticated
  USING (true);

CREATE POLICY "Match history can be inserted by server"
  ON match_history FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- Player achievements
CREATE TABLE IF NOT EXISTS player_achievements (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  player_uuid text REFERENCES players(uuid) ON DELETE CASCADE,
  achievement_id text NOT NULL,
  achievement_name text NOT NULL,
  achievement_description text,
  unlocked_at timestamptz DEFAULT now(),
  progress integer DEFAULT 0,
  target integer DEFAULT 1,
  UNIQUE(player_uuid, achievement_id)
);

ALTER TABLE player_achievements ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Achievements are viewable by everyone"
  ON player_achievements FOR SELECT
  TO anon, authenticated
  USING (true);

CREATE POLICY "Achievements can be managed by server"
  ON player_achievements FOR ALL
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- Duel challenge history
CREATE TABLE IF NOT EXISTS duel_challenges (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  challenger_uuid text REFERENCES players(uuid) ON DELETE CASCADE,
  challenger_name text NOT NULL,
  challenged_uuid text REFERENCES players(uuid) ON DELETE CASCADE,
  challenged_name text NOT NULL,
  kit_type text NOT NULL,
  status text NOT NULL,
  winner_uuid text,
  created_at timestamptz DEFAULT now(),
  completed_at timestamptz
);

ALTER TABLE duel_challenges ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Challenge history is viewable by everyone"
  ON duel_challenges FOR SELECT
  TO anon, authenticated
  USING (true);

CREATE POLICY "Challenges can be managed by server"
  ON duel_challenges FOR ALL
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- Arena usage statistics
CREATE TABLE IF NOT EXISTS arena_statistics (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  arena_name text NOT NULL,
  game_type text NOT NULL,
  total_games integer DEFAULT 0,
  total_players integer DEFAULT 0,
  average_duration_seconds integer DEFAULT 0,
  last_used timestamptz,
  popularity_score integer DEFAULT 0,
  updated_at timestamptz DEFAULT now(),
  UNIQUE(arena_name, game_type)
);

ALTER TABLE arena_statistics ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Arena stats are viewable by everyone"
  ON arena_statistics FOR SELECT
  TO anon, authenticated
  USING (true);

CREATE POLICY "Arena stats can be managed by server"
  ON arena_statistics FOR ALL
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- ============================================================================
-- CREATE INDEXES FOR PERFORMANCE
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_sg_stats_player_name ON sg_stats(player_name);
CREATE INDEX IF NOT EXISTS idx_sg_stats_solo_wins ON sg_stats(solo_wins DESC);
CREATE INDEX IF NOT EXISTS idx_sg_stats_team_wins ON sg_stats(team_wins DESC);
CREATE INDEX IF NOT EXISTS idx_duel_stats_player_name ON duel_stats(player_name);
CREATE INDEX IF NOT EXISTS idx_match_history_game_type ON match_history(game_type);
CREATE INDEX IF NOT EXISTS idx_match_history_ended_at ON match_history(ended_at DESC);
CREATE INDEX IF NOT EXISTS idx_match_history_winner ON match_history(winner_uuid);
CREATE INDEX IF NOT EXISTS idx_player_achievements_player ON player_achievements(player_uuid);
CREATE INDEX IF NOT EXISTS idx_duel_challenges_players ON duel_challenges(challenger_uuid, challenged_uuid);
CREATE INDEX IF NOT EXISTS idx_duel_challenges_status ON duel_challenges(status);
CREATE INDEX IF NOT EXISTS idx_arena_statistics_popularity ON arena_statistics(popularity_score DESC);

-- ============================================================================
-- CREATE VIEWS FOR OPTIMIZED QUERIES
-- ============================================================================

-- Combined player stats view
CREATE OR REPLACE VIEW player_stats_view AS
SELECT 
  p.uuid,
  p.username,
  p.language,
  p.created_at,
  p.last_seen,
  sg.wins as sg_total_wins,
  sg.kills as sg_total_kills,
  sg.deaths as sg_total_deaths,
  sg.games_played as sg_total_games,
  sg.solo_wins,
  sg.solo_kills,
  sg.solo_deaths,
  sg.solo_games_played,
  sg.team_wins,
  sg.team_kills,
  sg.team_deaths,
  sg.team_games_played,
  CASE 
    WHEN sg.deaths > 0 THEN ROUND((sg.kills::numeric / sg.deaths::numeric), 2)
    ELSE sg.kills::numeric
  END as sg_kd_ratio
FROM players p
LEFT JOIN sg_stats sg ON p.uuid = sg.player_uuid;

-- SG Leaderboard view
CREATE OR REPLACE VIEW sg_leaderboard_view AS
SELECT 
  ROW_NUMBER() OVER (ORDER BY wins DESC, kills DESC) as rank,
  player_uuid,
  player_name,
  wins,
  kills,
  deaths,
  games_played,
  CASE 
    WHEN deaths > 0 THEN ROUND((kills::numeric / deaths::numeric), 2)
    ELSE kills::numeric
  END as kd_ratio
FROM sg_stats
ORDER BY wins DESC, kills DESC;

-- Duel Leaderboard view
CREATE OR REPLACE VIEW duel_leaderboard_view AS
SELECT 
  ROW_NUMBER() OVER (PARTITION BY kit_type ORDER BY elo DESC, wins DESC) as rank,
  player_uuid,
  player_name,
  kit_type,
  elo,
  wins,
  losses,
  games_played,
  win_streak,
  best_win_streak,
  CASE 
    WHEN (wins + losses) > 0 THEN ROUND((wins::numeric / (wins + losses)::numeric * 100), 2)
    ELSE 0
  END as win_rate
FROM duel_stats
ORDER BY kit_type, elo DESC;

-- ============================================================================
-- CREATE DATABASE FUNCTIONS
-- ============================================================================

-- Get complete player profile
CREATE OR REPLACE FUNCTION get_player_profile(p_uuid text)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
  result jsonb;
BEGIN
  SELECT jsonb_build_object(
    'player', (
      SELECT jsonb_build_object(
        'uuid', uuid,
        'username', username,
        'language', language,
        'created_at', created_at,
        'last_seen', last_seen
      )
      FROM players WHERE uuid = p_uuid
    ),
    'sg_stats', (
      SELECT jsonb_build_object(
        'total', jsonb_build_object(
          'wins', wins,
          'kills', kills,
          'deaths', deaths,
          'games_played', games_played,
          'kd_ratio', CASE WHEN deaths > 0 THEN ROUND((kills::numeric / deaths::numeric), 2) ELSE kills::numeric END
        ),
        'solo', jsonb_build_object(
          'wins', solo_wins,
          'kills', solo_kills,
          'deaths', solo_deaths,
          'games_played', solo_games_played,
          'kd_ratio', CASE WHEN solo_deaths > 0 THEN ROUND((solo_kills::numeric / solo_deaths::numeric), 2) ELSE solo_kills::numeric END
        ),
        'team', jsonb_build_object(
          'wins', team_wins,
          'kills', team_kills,
          'deaths', team_deaths,
          'games_played', team_games_played,
          'kd_ratio', CASE WHEN team_deaths > 0 THEN ROUND((team_kills::numeric / team_deaths::numeric), 2) ELSE team_kills::numeric END
        )
      )
      FROM sg_stats WHERE player_uuid = p_uuid
    ),
    'duel_stats', (
      SELECT jsonb_agg(
        jsonb_build_object(
          'kit_type', kit_type,
          'elo', elo,
          'wins', wins,
          'losses', losses,
          'games_played', games_played,
          'win_streak', win_streak,
          'best_win_streak', best_win_streak,
          'win_rate', CASE WHEN (wins + losses) > 0 THEN ROUND((wins::numeric / (wins + losses)::numeric * 100), 2) ELSE 0 END
        )
      )
      FROM duel_stats WHERE player_uuid = p_uuid
    ),
    'recent_matches', (
      SELECT jsonb_agg(matches.*)
      FROM (
        SELECT game_type, arena_name, winner_uuid, winner_name, duration_seconds, ended_at
        FROM match_history
        WHERE participants::text LIKE '%' || p_uuid || '%'
        ORDER BY ended_at DESC
        LIMIT 10
      ) matches
    ),
    'achievements', (
      SELECT jsonb_agg(
        jsonb_build_object(
          'achievement_id', achievement_id,
          'achievement_name', achievement_name,
          'achievement_description', achievement_description,
          'unlocked_at', unlocked_at,
          'progress', progress,
          'target', target
        )
      )
      FROM player_achievements WHERE player_uuid = p_uuid
    )
  ) INTO result;
  
  RETURN result;
END;
$$;

-- Get player rank for specific stat
CREATE OR REPLACE FUNCTION get_player_rank(p_uuid text, stat_type text, kit text DEFAULT NULL)
RETURNS integer
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
  player_rank integer;
BEGIN
  IF stat_type = 'sg_wins' THEN
    SELECT COUNT(*) + 1 INTO player_rank
    FROM sg_stats
    WHERE wins > (SELECT wins FROM sg_stats WHERE player_uuid = p_uuid);
  ELSIF stat_type = 'sg_kills' THEN
    SELECT COUNT(*) + 1 INTO player_rank
    FROM sg_stats
    WHERE kills > (SELECT kills FROM sg_stats WHERE player_uuid = p_uuid);
  ELSIF stat_type = 'duel_elo' AND kit IS NOT NULL THEN
    SELECT COUNT(*) + 1 INTO player_rank
    FROM duel_stats
    WHERE kit_type = kit AND elo > (SELECT elo FROM duel_stats WHERE player_uuid = p_uuid AND kit_type = kit);
  ELSE
    player_rank := 0;
  END IF;
  
  RETURN player_rank;
END;
$$;

-- Get leaderboard with pagination
CREATE OR REPLACE FUNCTION get_leaderboard(
  game_type text,
  stat_type text,
  page_limit integer DEFAULT 10,
  page_offset integer DEFAULT 0,
  kit text DEFAULT NULL
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
  result jsonb;
BEGIN
  IF game_type = 'sg' AND stat_type = 'wins' THEN
    SELECT jsonb_agg(
      jsonb_build_object(
        'rank', rank,
        'player_uuid', player_uuid,
        'player_name', player_name,
        'wins', wins,
        'kills', kills,
        'deaths', deaths,
        'games_played', games_played,
        'kd_ratio', kd_ratio
      )
    ) INTO result
    FROM (
      SELECT * FROM sg_leaderboard_view
      ORDER BY rank
      LIMIT page_limit OFFSET page_offset
    ) sub;
  ELSIF game_type = 'duel' AND kit IS NOT NULL THEN
    SELECT jsonb_agg(
      jsonb_build_object(
        'rank', rank,
        'player_uuid', player_uuid,
        'player_name', player_name,
        'kit_type', kit_type,
        'elo', elo,
        'wins', wins,
        'losses', losses,
        'games_played', games_played,
        'win_streak', win_streak,
        'best_win_streak', best_win_streak,
        'win_rate', win_rate
      )
    ) INTO result
    FROM (
      SELECT * FROM duel_leaderboard_view
      WHERE kit_type = kit
      ORDER BY rank
      LIMIT page_limit OFFSET page_offset
    ) sub;
  ELSE
    result := '[]'::jsonb;
  END IF;
  
  RETURN result;
END;
$$;

-- Get server statistics
CREATE OR REPLACE FUNCTION get_server_statistics()
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
  result jsonb;
BEGIN
  SELECT jsonb_build_object(
    'total_players', (SELECT COUNT(*) FROM players),
    'total_sg_games', (SELECT SUM(games_played) FROM sg_stats),
    'total_duel_games', (SELECT SUM(games_played) FROM duel_stats),
    'total_matches_recorded', (SELECT COUNT(*) FROM match_history),
    'active_arenas', jsonb_build_object(
      'sg', (SELECT COUNT(*) FROM sg_arenas WHERE enabled = true),
      'duel', (SELECT COUNT(*) FROM duel_arenas WHERE enabled = true)
    ),
    'popular_arenas', (
      SELECT jsonb_agg(
        jsonb_build_object(
          'arena_name', arena_name,
          'game_type', game_type,
          'total_games', total_games,
          'popularity_score', popularity_score
        )
      )
      FROM (
        SELECT * FROM arena_statistics
        ORDER BY popularity_score DESC
        LIMIT 5
      ) top_arenas
    ),
    'recent_matches', (
      SELECT jsonb_agg(matches.*)
      FROM (
        SELECT game_type, arena_name, winner_name, duration_seconds, ended_at
        FROM match_history
        ORDER BY ended_at DESC
        LIMIT 10
      ) matches
    )
  ) INTO result;
  
  RETURN result;
END;
$$;

-- ============================================================================
-- UPDATE RLS POLICIES FOR ANONYMOUS ACCESS
-- ============================================================================

-- Allow anonymous (website) users to read player data
DROP POLICY IF EXISTS "Players are viewable by everyone" ON players;
CREATE POLICY "Players are viewable by everyone"
  ON players FOR SELECT
  TO anon, authenticated
  USING (true);

-- Allow anonymous users to read SG stats
DROP POLICY IF EXISTS "SG stats are viewable by everyone" ON sg_stats;
CREATE POLICY "SG stats are viewable by everyone"
  ON sg_stats FOR SELECT
  TO anon, authenticated
  USING (true);

-- Allow anonymous users to read duel stats
DROP POLICY IF EXISTS "Duel stats are viewable by everyone" ON duel_stats;
CREATE POLICY "Duel stats are viewable by everyone"
  ON duel_stats FOR SELECT
  TO anon, authenticated
  USING (true);

-- Allow anonymous users to read arena data
DROP POLICY IF EXISTS "SG arenas are viewable by everyone" ON sg_arenas;
CREATE POLICY "SG arenas are viewable by everyone"
  ON sg_arenas FOR SELECT
  TO anon, authenticated
  USING (true);

DROP POLICY IF EXISTS "Duel arenas are viewable by everyone" ON duel_arenas;
CREATE POLICY "Duel arenas are viewable by everyone"
  ON duel_arenas FOR SELECT
  TO anon, authenticated
  USING (true);
