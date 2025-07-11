package me.kiratdewas.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kiratdewas.StatsPlugin;
import me.kiratdewas.importer.PlayerStatsImporter;
import java.util.UUID;

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
            // Create leaderboard tables if not exist
            String createMostKills = "CREATE TABLE IF NOT EXISTS mostkills (uuid VARCHAR(40) PRIMARY KEY, kills INT)";
            String createTopBalance = "CREATE TABLE IF NOT EXISTS topbalance (uuid VARCHAR(40) PRIMARY KEY, balance DOUBLE)";
            String createMostDeath = "CREATE TABLE IF NOT EXISTS mostdeath (uuid VARCHAR(40) PRIMARY KEY, deaths INT)";
            String createMostBlockBroken = "CREATE TABLE IF NOT EXISTS mostblockbroken (uuid VARCHAR(40) PRIMARY KEY, blockbroken INT)";
            String createMostPlaytime = "CREATE TABLE IF NOT EXISTS mostplaytime (uuid VARCHAR(40) PRIMARY KEY, playtime BIGINT)";
            conn.createStatement().execute(createMostKills);
            conn.createStatement().execute(createTopBalance);
            conn.createStatement().execute(createMostDeath);
            conn.createStatement().execute(createMostBlockBroken);
            conn.createStatement().execute(createMostPlaytime);
            String upsert = "REPLACE INTO player_stats (uuid, stats) VALUES (?, ?)";
            JsonNode json = objectMapper.readTree(file);
            // Inject balance and group
            json = PlayerStatsImporter.injectBalanceAndGroup(json, UUID.fromString(uuid));
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setString(1, uuid);
                stmt.setString(2, json.toString());
                stmt.executeUpdate();
            }
            // Extract leaderboard stats
            int kills = 0, deaths = 0, blockbroken = 0;
            long playtime = 0;
            double balance = 0.0;
            if (json.has("balance"))
                balance = json.get("balance").asDouble();
            if (json.has("stats")) {
                JsonNode statsNode = json.get("stats");
                if (statsNode.has("minecraft:custom")) {
                    JsonNode custom = statsNode.get("minecraft:custom");
                    if (custom.has("minecraft:mob_kills"))
                        kills = custom.get("minecraft:mob_kills").asInt();
                    if (custom.has("minecraft:deaths"))
                        deaths = custom.get("minecraft:deaths").asInt();
                    if (custom.has("minecraft:play_time"))
                        playtime = custom.get("minecraft:play_time").asLong();
                }
                if (statsNode.has("minecraft:mined")) {
                    JsonNode mined = statsNode.get("minecraft:mined");
                    blockbroken = 0;
                    for (JsonNode v : mined)
                        blockbroken += v.asInt();
                }
            }
            // Upsert into leaderboard tables
            String upsertKills = "REPLACE INTO mostkills (uuid, kills) VALUES (?, ?)";
            String upsertBalance = "REPLACE INTO topbalance (uuid, balance) VALUES (?, ?)";
            String upsertDeaths = "REPLACE INTO mostdeath (uuid, deaths) VALUES (?, ?)";
            String upsertBlockBroken = "REPLACE INTO mostblockbroken (uuid, blockbroken) VALUES (?, ?)";
            String upsertPlaytime = "REPLACE INTO mostplaytime (uuid, playtime) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(upsertKills)) {
                stmt.setString(1, uuid);
                stmt.setInt(2, kills);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(upsertBalance)) {
                stmt.setString(1, uuid);
                stmt.setDouble(2, balance);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(upsertDeaths)) {
                stmt.setString(1, uuid);
                stmt.setInt(2, deaths);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(upsertBlockBroken)) {
                stmt.setString(1, uuid);
                stmt.setInt(2, blockbroken);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(upsertPlaytime)) {
                stmt.setString(1, uuid);
                stmt.setLong(2, playtime);
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
            // Create leaderboard tables if not exist
            String createMostKills = "CREATE TABLE IF NOT EXISTS mostkills (uuid VARCHAR(40) PRIMARY KEY, kills INT)";
            String createTopBalance = "CREATE TABLE IF NOT EXISTS topbalance (uuid VARCHAR(40) PRIMARY KEY, balance DOUBLE)";
            String createMostDeath = "CREATE TABLE IF NOT EXISTS mostdeath (uuid VARCHAR(40) PRIMARY KEY, deaths INT)";
            String createMostBlockBroken = "CREATE TABLE IF NOT EXISTS mostblockbroken (uuid VARCHAR(40) PRIMARY KEY, blockbroken INT)";
            String createMostPlaytime = "CREATE TABLE IF NOT EXISTS mostplaytime (uuid VARCHAR(40) PRIMARY KEY, playtime BIGINT)";
            conn.createStatement().execute(createMostKills);
            conn.createStatement().execute(createTopBalance);
            conn.createStatement().execute(createMostDeath);
            conn.createStatement().execute(createMostBlockBroken);
            conn.createStatement().execute(createMostPlaytime);
            String upsert = "REPLACE INTO player_stats (uuid, stats) VALUES (?, ?)";
            // Leaderboard upsert queries
            String upsertKills = "REPLACE INTO mostkills (uuid, kills) VALUES (?, ?)";
            String upsertBalance = "REPLACE INTO topbalance (uuid, balance) VALUES (?, ?)";
            String upsertDeaths = "REPLACE INTO mostdeath (uuid, deaths) VALUES (?, ?)";
            String upsertBlockBroken = "REPLACE INTO mostblockbroken (uuid, blockbroken) VALUES (?, ?)";
            String upsertPlaytime = "REPLACE INTO mostplaytime (uuid, playtime) VALUES (?, ?)";
            for (File file : files) {
                String uuid = file.getName().replace(".json", "");
                JsonNode json = objectMapper.readTree(file);
                // Inject balance and group
                json = PlayerStatsImporter.injectBalanceAndGroup(json, UUID.fromString(uuid));
                try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                    stmt.setString(1, uuid);
                    stmt.setString(2, json.toString());
                    stmt.executeUpdate();
                }
                // Extract leaderboard stats
                int kills = 0, deaths = 0, blockbroken = 0;
                long playtime = 0;
                double balance = 0.0;
                if (json.has("balance"))
                    balance = json.get("balance").asDouble();
                if (json.has("stats")) {
                    JsonNode statsNode = json.get("stats");
                    if (statsNode.has("minecraft:custom")) {
                        JsonNode custom = statsNode.get("minecraft:custom");
                        if (custom.has("minecraft:mob_kills"))
                            kills = custom.get("minecraft:mob_kills").asInt();
                        if (custom.has("minecraft:deaths"))
                            deaths = custom.get("minecraft:deaths").asInt();
                        if (custom.has("minecraft:play_time"))
                            playtime = custom.get("minecraft:play_time").asLong();
                    }
                    if (statsNode.has("minecraft:mined")) {
                        JsonNode mined = statsNode.get("minecraft:mined");
                        blockbroken = 0;
                        for (JsonNode v : mined)
                            blockbroken += v.asInt();
                    }
                }
                // Upsert into leaderboard tables
                try (PreparedStatement stmt2 = conn.prepareStatement(upsertKills)) {
                    stmt2.setString(1, uuid);
                    stmt2.setInt(2, kills);
                    stmt2.executeUpdate();
                }
                try (PreparedStatement stmt2 = conn.prepareStatement(upsertBalance)) {
                    stmt2.setString(1, uuid);
                    stmt2.setDouble(2, balance);
                    stmt2.executeUpdate();
                }
                try (PreparedStatement stmt2 = conn.prepareStatement(upsertDeaths)) {
                    stmt2.setString(1, uuid);
                    stmt2.setInt(2, deaths);
                    stmt2.executeUpdate();
                }
                try (PreparedStatement stmt2 = conn.prepareStatement(upsertBlockBroken)) {
                    stmt2.setString(1, uuid);
                    stmt2.setInt(2, blockbroken);
                    stmt2.executeUpdate();
                }
                try (PreparedStatement stmt2 = conn.prepareStatement(upsertPlaytime)) {
                    stmt2.setString(1, uuid);
                    stmt2.setLong(2, playtime);
                    stmt2.executeUpdate();
                }
            }
            plugin.getLogger().info("Imported stats to MySQL.");
        } catch (SQLException | IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to import stats to MySQL", e);
        }
    }
}