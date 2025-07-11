package me.kiratdewas.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kiratdewas.StatsPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class MySQL {
    private final StatsPlugin.MySQLConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StatsPlugin plugin;

    public MySQL(StatsPlugin plugin, StatsPlugin.MySQLConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean upsertPlayerStats(File file, String uuid) {
        String url = String.format("jdbc:mysql://%s:%d/%s", config.host, config.port, config.database);
        try (Connection conn = DriverManager.getConnection(url, config.username, config.password)) {
            String createTable = "CREATE TABLE IF NOT EXISTS player_stats (uuid VARCHAR(40) PRIMARY KEY, stats JSON)";
            conn.createStatement().execute(createTable);
            String upsert = "REPLACE INTO player_stats (uuid, stats) VALUES (?, ?)";
            JsonNode json = objectMapper.readTree(file);
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setString(1, uuid);
                stmt.setString(2, json.toString());
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException | IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to import stats to MySQL for " + uuid, e);
            return false;
        }
    }

    public void bulkImport(File[] files) {
        String url = String.format("jdbc:mysql://%s:%d/%s", config.host, config.port, config.database);
        try (Connection conn = DriverManager.getConnection(url, config.username, config.password)) {
            String createTable = "CREATE TABLE IF NOT EXISTS player_stats (uuid VARCHAR(40) PRIMARY KEY, stats JSON)";
            conn.createStatement().execute(createTable);
            String upsert = "REPLACE INTO player_stats (uuid, stats) VALUES (?, ?)";
            for (File file : files) {
                String uuid = file.getName().replace(".json", "");
                JsonNode json = objectMapper.readTree(file);
                try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                    stmt.setString(1, uuid);
                    stmt.setString(2, json.toString());
                    stmt.executeUpdate();
                }
            }
            plugin.getLogger().info("Imported stats to MySQL.");
        } catch (SQLException | IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to import stats to MySQL", e);
        }
    }
}