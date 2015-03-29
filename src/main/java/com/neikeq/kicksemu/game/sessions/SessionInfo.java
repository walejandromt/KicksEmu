package com.neikeq.kicksemu.game.sessions;

import com.neikeq.kicksemu.storage.MySqlManager;
import com.neikeq.kicksemu.storage.SqlUtils;
import com.neikeq.kicksemu.utils.RandomGenerator;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionInfo {

    private static final String TABLE = "sessions";

    public static int getPlayerId(int sessionId, Connection ... con) {
        return getInt("player_id", TABLE, sessionId, con);
    }

    public static void setPlayerId(int playerId, int sessionId, Connection ... con) {
        SqlUtils.setInt("player_id", playerId, TABLE, sessionId, con);
    }

    public static int getUserId(int sessionId, Connection ... con) {
        return getInt("user_id", TABLE, sessionId, con);
    }

    public static String getHash(int sessionId, Connection ... con) {
        return getString("hash", TABLE, sessionId, con);
    }

    public static void reduceExpiration(int sessionId) {
        String sql = "UPDATE " + TABLE + " SET expiration = CURRENT_TIMESTAMP + " +
                "INTERVAL 30 SECOND WHERE id = ?";

        try (Connection con = MySqlManager.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public static void remove(int sessionId) {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";

        try (Connection con = MySqlManager.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public static void resetExpiration(int sessionId) {
        String sql = "UPDATE " + TABLE + " SET expiration = CURRENT_TIMESTAMP + " +
                "INTERVAL 1 DAY WHERE id = ?";

        try (Connection con = MySqlManager.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public static int generateSessionId() {
        String sql = "SELECT FLOOR((RAND() * 4294967295) - 2147483648) " +
                "AS random_session_id FROM " + TABLE + " WHERE \"random_session_id\" " +
                "NOT IN (SELECT id FROM " + TABLE + ") LIMIT 1";

        try (Connection con = MySqlManager.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("random_session_id");
                } else {
                    return RandomGenerator.randomInt();
                }
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            return -1;
        }
    }

    public static void insertSession(int sessionId, int userId, int playerId, String hash) {
        String sql = "INSERT INTO " + TABLE + " (id, user_id, player_id, hash, expiration) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP + INTERVAL 1 DAY)";

        try (Connection con = MySqlManager.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, userId);
            stmt.setInt(3, playerId);
            stmt.setString(4, hash);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public static int getInt(String column, String table, int id, Connection ... con) {
        String query = "SELECT " + column + " FROM " + table +
                " WHERE id = ? AND expiration > CURRENT_TIMESTAMP";

        try {
            Connection connection = con.length > 0 ? con[0] : MySqlManager.getConnection();

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(column);
                    } else {
                        return -1;
                    }
                }
            } finally {
                if (con.length <= 0) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    public static String getString(String column, String table, int id, Connection ... con) {
        String query = "SELECT " + column + " FROM " + table +
                " WHERE id = ? AND expiration > CURRENT_TIMESTAMP";

        try {
            Connection connection = con.length > 0 ? con[0] : MySqlManager.getConnection();

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(column);
                    } else {
                        return null;
                    }
                }
            } finally {
                if (con.length <= 0) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            return null;
        }
    }
}
