package com.venomgrave.hexvg.impl.session;

import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.database.DatabaseType;
import com.venomgrave.hexvg.api.session.SessionService;
import com.venomgrave.hexvg.config.CoreConfig;

import java.sql.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SessionServiceImpl implements SessionService {

    private final DatabaseService db;
    private final Logger          logger;
    private final CoreConfig      config;

    // UUID → czas dołączenia (epoch millis) dla aktywnych sesji
    private final Map<UUID, Long> activeSessions = new ConcurrentHashMap<>();

    public SessionServiceImpl(DatabaseService db,
                              Logger logger,
                              CoreConfig config) {
        this.db     = db;
        this.logger = logger;
        this.config = config;
    }

    // ── SessionService ────────────────────────────────────────────────────

    @Override
    public void handleJoin(UUID uuid, String playerName, String ipHash) {
        activeSessions.put(uuid, System.currentTimeMillis());
        insertSession(uuid, playerName, ipHash);

        if (config.isDebug()) {
            logger.info("[Session] Join: " + playerName + " (" + uuid + ")");
        }
    }

    @Override
    public void handleQuit(UUID uuid) {
        Long joinTime = activeSessions.remove(uuid);
        if (joinTime == null) return;

        long seconds = (System.currentTimeMillis() - joinTime) / 1000L;
        addPlaytime(uuid, seconds);
        closeSession(uuid);

        if (config.isDebug()) {
            logger.info("[Session] Quit: " + uuid
                    + " | sesja: " + seconds + "s");
        }
    }

    @Override
    public Instant getLastSeen(UUID uuid) {
        // Jeśli gracz jest online — teraz
        if (activeSessions.containsKey(uuid)) return Instant.now();

        String sql = """
                SELECT login_at FROM hexvg_sessions
                WHERE player_uuid = ?
                ORDER BY login_at DESC
                LIMIT 1
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("login_at");
                    return ts != null ? ts.toInstant() : null;
                }
            }
        } catch (SQLException e) {
            logger.warning("[Session] getLastSeen error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public long getPlaytimeSeconds(UUID uuid) {
        // Dodaj bieżącą sesję jeśli gracz online
        long extra = 0L;
        Long joinTime = activeSessions.get(uuid);
        if (joinTime != null) {
            extra = (System.currentTimeMillis() - joinTime) / 1000L;
        }

        String sql = "SELECT total_seconds FROM hexvg_playtime WHERE uuid = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("total_seconds") + extra;
            }
        } catch (SQLException e) {
            logger.warning("[Session] getPlaytimeSeconds error: " + e.getMessage());
        }
        return extra;
    }

    @Override
    public int getLoginCount(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM hexvg_sessions WHERE player_uuid = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warning("[Session] getLoginCount error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public Instant getFirstJoin(UUID uuid) {
        String sql = """
                SELECT MIN(login_at) AS first_join
                FROM hexvg_sessions
                WHERE player_uuid = ?
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("first_join");
                    return ts != null ? ts.toInstant() : null;
                }
            }
        } catch (SQLException e) {
            logger.warning("[Session] getFirstJoin error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean hasPlayed(UUID uuid) {
        return getLoginCount(uuid) > 0;
    }

    @Override
    public void flushAll() {
        if (activeSessions.isEmpty()) return;

        logger.info("[Session] Flushuję " + activeSessions.size()
                + " aktywnych sesji...");

        // Kopiujemy żeby uniknąć ConcurrentModificationException
        Map<UUID, Long> snapshot = Map.copyOf(activeSessions);

        snapshot.forEach((uuid, joinTime) -> {
            long seconds = (System.currentTimeMillis() - joinTime) / 1000L;
            addPlaytime(uuid, seconds);
            closeSession(uuid);
        });

        activeSessions.clear();
        logger.info("[Session] Flush zakończony.");
    }

    // ── SQL helpers ───────────────────────────────────────────────────────

    private void insertSession(UUID uuid, String playerName, String ipHash) {
        String sql = """
                INSERT INTO hexvg_sessions (player_uuid, player_name, login_at, ip_hash)
                VALUES (?, ?, CURRENT_TIMESTAMP, ?)
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.setString(3, ipHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("[Session] insertSession error: " + e.getMessage());
        }
    }

    private void closeSession(UUID uuid) {
        // SQLite nie wspiera LIMIT w UPDATE — osobna ścieżka
        String sql = db.getType() == DatabaseType.MYSQL
                ? """
                  UPDATE hexvg_sessions
                  SET logout_at = CURRENT_TIMESTAMP
                  WHERE player_uuid = ? AND logout_at IS NULL
                  ORDER BY login_at DESC
                  LIMIT 1
                  """
                : """
                  UPDATE hexvg_sessions
                  SET logout_at = CURRENT_TIMESTAMP
                  WHERE id = (
                      SELECT id FROM hexvg_sessions
                      WHERE player_uuid = ? AND logout_at IS NULL
                      ORDER BY login_at DESC
                      LIMIT 1
                  )
                  """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("[Session] closeSession error: " + e.getMessage());
        }
    }

    private void addPlaytime(UUID uuid, long seconds) {
        if (seconds <= 0) return;

        String sql = db.getType() == DatabaseType.MYSQL
                ? """
                  INSERT INTO hexvg_playtime (uuid, total_seconds)
                  VALUES (?, ?)
                  ON DUPLICATE KEY UPDATE
                      total_seconds = total_seconds + VALUES(total_seconds),
                      updated_at    = CURRENT_TIMESTAMP
                  """
                : """
                  INSERT INTO hexvg_playtime (uuid, total_seconds)
                  VALUES (?, ?)
                  ON CONFLICT(uuid) DO UPDATE SET
                      total_seconds = total_seconds + excluded.total_seconds,
                      updated_at    = CURRENT_TIMESTAMP
                  """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setLong(  2, seconds);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("[Session] addPlaytime error: " + e.getMessage());
        }
    }
}