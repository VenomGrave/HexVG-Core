package com.hexvg.core.player;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.database.BaseRepository;
import com.hexvg.core.utils.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class PlayerDataRepository extends BaseRepository {

    public PlayerDataRepository(HexVGCore core) {
        super(core);
    }

    public void save(CorePlayerData data) {
        boolean isMySQL = db.getDatabaseType().name().equals("MYSQL");

        // Java 17 - text blocks
        String sql = isMySQL ? """
                INSERT INTO hexvg_players (uuid, name, first_join, last_join) VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE name = VALUES(name), last_join = VALUES(last_join)
                """ : """
                INSERT OR REPLACE INTO hexvg_players (uuid, name, first_join, last_join) VALUES (?, ?, ?, ?)
                """;

        executeAsync(sql,
                data.getUuid().toString(),
                data.getPlayerName(),
                data.getFirstJoin().toString(),
                data.getLastJoin().toString()
        );

        Logger.debug("Zapisano dane gracza: " + data.getPlayerName());
    }

    public Optional<CorePlayerData> load(UUID uuid) {
        return queryOne(
                "SELECT * FROM hexvg_players WHERE uuid = ?",
                this::mapResultSet,
                uuid.toString()
        );
    }

    public boolean playerExists(UUID uuid) {
        return exists("SELECT 1 FROM hexvg_players WHERE uuid = ?", uuid.toString());
    }

    public void updateName(UUID uuid, String newName) {
        executeAsync("UPDATE hexvg_players SET name = ? WHERE uuid = ?",
                newName, uuid.toString());
    }

    public void updateLastJoin(UUID uuid) {
        executeAsync("UPDATE hexvg_players SET last_join = ? WHERE uuid = ?",
                Instant.now().toString(), uuid.toString());
    }

    public void delete(UUID uuid) {
        executeAsync("DELETE FROM hexvg_players WHERE uuid = ?", uuid.toString());
    }

    private CorePlayerData mapResultSet(ResultSet rs) {
        try {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            String name = rs.getString("name");
            return new CorePlayerData(uuid, name);
        } catch (SQLException e) {
            Logger.error("Błąd mapowania danych gracza: " + e.getMessage());
            return null;
        }
    }
}