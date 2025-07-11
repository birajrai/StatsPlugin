package me.kiratdewas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import org.yaml.snakeyaml.Yaml;

public class StatsReloadedPlugin extends JavaPlugin {
    private String dbType;
    private MySQLConfig mysqlConfig;
    private MongoConfig mongoConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getLogger().info("StatsReloaded enabled. Importing stats...");
        importStats();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("statsreloaded")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                importStats();
                sender.sendMessage("Stats re-imported from world/stats/.");
                return true;
            }
        }
        return false;
    }

    private void loadConfig() {
        this.saveDefaultConfig();
        org.bukkit.configuration.file.FileConfiguration config = getConfig();
        dbType = config.getString("database.type");
        if ("mysql".equalsIgnoreCase(dbType)) {
            org.bukkit.configuration.ConfigurationSection mysql = config.getConfigurationSection("database.mysql");
            mysqlConfig = new MySQLConfig(
                mysql.getString("host"),
                mysql.getInt("port"),
                mysql.getString("database"),
                mysql.getString("username"),
                mysql.getString("password")
            );
        } else if ("mongodb".equalsIgnoreCase(dbType)) {
            org.bukkit.configuration.ConfigurationSection mongo = config.getConfigurationSection("database.mongodb");
            mongoConfig = new MongoConfig(
                mongo.getString("uri"),
                mongo.getString("database")
            );
        }
    }

    private void importStats() {
        File statsDir = new File(getServer().getWorldContainer(), "world/stats");
        if (!statsDir.exists() || !statsDir.isDirectory()) {
            getLogger().warning("Directory world/stats/ does not exist.");
            return;
        }
        File[] files = statsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            getLogger().info("No JSON files found in world/stats/");
            return;
        }
        if (dbType.equalsIgnoreCase("mysql")) {
            importToMySQL(files);
        } else if (dbType.equalsIgnoreCase("mongodb")) {
            importToMongoDB(files);
        }
    }

    private void importToMySQL(File[] files) {
        String url = String.format("jdbc:mysql://%s:%d/%s", mysqlConfig.host, mysqlConfig.port, mysqlConfig.database);
        try (Connection conn = DriverManager.getConnection(url, mysqlConfig.username, mysqlConfig.password)) {
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
            getLogger().info("Imported stats to MySQL.");
        } catch (SQLException | IOException e) {
            getLogger().log(Level.SEVERE, "Failed to import stats to MySQL", e);
        }
    }

    private void importToMongoDB(File[] files) {
        try (MongoClient mongoClient = MongoClients.create(mongoConfig.uri)) {
            MongoDatabase db = mongoClient.getDatabase(mongoConfig.database);
            MongoCollection<Document> collection = db.getCollection("player_stats");
            for (File file : files) {
                String uuid = file.getName().replace(".json", "");
                JsonNode json = objectMapper.readTree(file);
                Document doc = new Document("uuid", uuid).append("stats", Document.parse(json.toString()));
                collection.replaceOne(new Document("uuid", uuid), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
            }
            getLogger().info("Imported stats to MongoDB.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to import stats to MongoDB", e);
        }
    }

    private static class MySQLConfig {
        String host;
        int port;
        String database;
        String username;
        String password;
        MySQLConfig(String host, int port, String database, String username, String password) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
        }
    }
    private static class MongoConfig {
        String uri;
        String database;
        MongoConfig(String uri, String database) {
            this.uri = uri;
            this.database = database;
        }
    }
} 