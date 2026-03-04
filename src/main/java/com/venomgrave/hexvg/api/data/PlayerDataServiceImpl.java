package com.venomgrave.hexvg.impl.data;

import com.venomgrave.hexvg.api.data.PlayerDataService;
import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.database.DatabaseType;
import com.venomgrave.hexvg.config.CoreConfig;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PlayerDataServiceImpl implements PlayerDataService {

    private final DatabaseService db;
    private final Logger          logger;
    private final CoreConfig      config;

    // Cache ban/mute — klucz: uuid, wartość: true/false
    private final Map<UUID, Boolean> banCache  = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> muteCache = new ConcurrentHashMap<>();

    public PlayerDataServiceImpl(DatabaseService db,
                                 Logger logger,
                                 CoreConfig config) {
        this.db     = db;
        this.logger = logger;
        this.config = config;
    }

    // ── PlayerDataService ─────────────────────────────────────────────────

    @Override
    public void upsertProfile(UUID uuid, String name, String ipHash) {
        String sql = db.getType() == DatabaseType.MYSQL
                ? """
                  INSERT INTO hexvg_players (uuid, name, first_ip_hash)
                  VALUES (?, ?, ?)
                  ON DUPLICATE KEY UPDATE
                      name       = VALUES(name),
                      updated_at = CURRENT_TIMESTAMP
                  """
                : """
                  INSERT INTO hexvg_players (uuid, name, first_ip_hash)
                  VALUES (?, ?, ?)
                  ON CONFLICT(uuid) DO UPDATE SET
                      name       = excluded.name,
                      updated_at = CURRENT_TIMESTAMP
                  """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setString(3, ipHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("[PlayerData] upsertProfile error: " + e.getMessage());
        }
    }

    @Override
    public String getNick(UUID uuid) {
        String sql = "SELECT nick FROM hexvg_players WHERE uuid = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("nick");
            }
        } catch (SQLException e) {
            logger.warning("[PlayerData] getNick error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void setNick(UUID uuid, String nick) {
        String sql = db.getType() == DatabaseType.MYSQL
                ? """
                  INSERT INTO hexvg_players (uuid, nick)
                  VALUES (?, ?)
                  ON DUPLICATE KEY UPDATE nick = VALUES(nick)
                  """
                : """
                  INSERT INTO hexvg_players (uuid, nick)
                  VALUES (?, ?)
                  ON CONFLICT(uuid) DO UPDATE SET nick = excluded.nick
                  """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, nick);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("[PlayerData] setNick error: " + e.getMessage());
        }
    }

    @Override
    public void clearNick(UUID uuid) {
        String sql = "UPDATE hexvg_players SET nick = NULL WHERE uuid = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("[PlayerData] clearNick error: " + e.getMessage());
        }
    }

    @Override
    public boolean isNickTaken(String nick, UUID excludeUuid) {
        String sql = """
                SELECT 1 FROM hexvg_players
                WHERE nick = ? AND uuid != ?
                LIMIT 1
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setString(2, excludeUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.warning("[PlayerData] isNickTaken error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String getFirstIpHash(UUID uuid) {
        String sql = "SELECT first_ip_hash FROM hexvg_players WHERE uuid = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("first_ip_hash");
            }
        } catch (SQLException e) {
            logger.warning("[PlayerData] getFirstIpHash error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isBanned(UUID uuid) {
        return banCache.computeIfAbsent(uuid, this::fetchBanStatus);
    }

    @Override
    public boolean isMuted(UUID uuid) {
        return muteCache.computeIfAbsent(uuid, this::fetchMuteStatus);
    }

    @Override
    public int getActiveWarnCount(UUID uuid) {
        String sql = """
                SELECT COUNT(*) FROM hexvg_warnings
                WHERE player_uuid = ?
                  AND active = 1
                  AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warning("[PlayerData] getActiveWarnCount error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public void invalidateCache(UUID uuid) {
        banCache.remove(uuid);
        muteCache.remove(uuid);

        if (config.isDebug()) {
            logger.info("[PlayerData] Invalidated cache: " + uuid);
        }
    }

    // ── Prywatne ─────────────────────────────────────────────────────────

    private boolean fetchBanStatus(UUID uuid) {
        String sql = """
                SELECT 1 FROM hexvg_punishments
                WHERE player_uuid = ?
                  AND type = 'BAN'
                  AND active = 1
                  AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
                LIMIT 1
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.warning("[PlayerData] fetchBanStatus error: " + e.getMessage());
        }
        return false;
    }

    private boolean fetchMuteStatus(UUID uuid) {
        String sql = """
                SELECT 1 FROM hexvg_punishments
                WHERE player_uuid = ?
                  AND type = 'MUTE'
                  AND active = 1
                  AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
                LIMIT 1
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.warning("[PlayerData] fetchMuteStatus error: " + e.getMessage());
        }
        return false;
    }
}