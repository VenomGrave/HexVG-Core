package com.hexvg.core.database;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Bazowa klasa dla repozytoriów SQL.
 * Pluginy zależne dziedziczą z tej klasy, żeby mieć dostęp do pomocniczych metod.
 *
 * Przykład użycia:
 * <pre>
 *     public class PlayerRepository extends BaseRepository {
 *         public PlayerRepository(HexVGCore core) {
 *             super(core);
 *         }
 *
 *         public Optional<PlayerData> findByUUID(UUID uuid) {
 *             return queryOne(
 *                 "SELECT * FROM players WHERE uuid = ?",
 *                 rs -> new PlayerData(rs.getString("uuid"), rs.getString("name")),
 *                 uuid.toString()
 *             );
 *         }
 *     }
 * </pre>
 */
public abstract class BaseRepository {

    protected final HexVGCore core;
    protected final DatabaseManager db;

    protected BaseRepository(HexVGCore core) {
        this.core = core;
        this.db = core.getDatabaseManager();
    }

    /**
     * Wykonuje zapytanie SQL zwracające wiele wyników.
     *
     * @param sql     zapytanie SQL
     * @param mapper  funkcja mapująca ResultSet -> obiekt
     * @param params  parametry zapytania
     * @return lista wyników
     */
    protected <T> List<T> queryMany(String sql, Function<ResultSet, T> mapper, Object... params) {
        List<T> results = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                T result = mapper.apply(rs);
                if (result != null) results.add(result);
            }

        } catch (SQLException e) {
            Logger.error("Błąd zapytania SQL: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Wykonuje zapytanie SQL zwracające jeden wynik.
     */
    protected <T> Optional<T> queryOne(String sql, Function<ResultSet, T> mapper, Object... params) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return Optional.ofNullable(mapper.apply(rs));
            }

        } catch (SQLException e) {
            Logger.error("Błąd zapytania SQL: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Wykonuje zapytanie UPDATE/INSERT/DELETE.
     *
     * @return liczba zmodyfikowanych wierszy, -1 przy błędzie
     */
    protected int execute(String sql, Object... params) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params)) {

            return stmt.executeUpdate();

        } catch (SQLException e) {
            Logger.error("Błąd wykonania SQL: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Asynchronicznie wykonuje zapytanie UPDATE/INSERT/DELETE.
     */
    protected void executeAsync(String sql, Object... params) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> execute(sql, params));
    }

    /**
     * Sprawdza czy istnieje rekord spełniający warunek.
     */
    protected boolean exists(String sql, Object... params) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next();

        } catch (SQLException e) {
            Logger.error("Błąd sprawdzania istnienia: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tworzy tabelę jeśli nie istnieje.
     */
    protected void createTableIfNotExists(String createTableSQL) {
        execute(createTableSQL);
    }

    /**
     * Przygotowuje PreparedStatement z parametrami.
     */
    private PreparedStatement prepareStatement(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }
}