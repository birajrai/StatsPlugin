package me.kiratdewas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import me.kiratdewas.command.StatsCommand;
import me.kiratdewas.database.Core;
import me.kiratdewas.config.ConfigLoader;

public class StatsPlugin extends JavaPlugin {
    private String dbType;
    private MySQLConfig mysqlConfig;
    private MongoConfig mongoConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Core dbCore;
    private boolean cmiCompatible;
    private boolean luckPermsCompatible;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getLogger().info("Stats enabled. Importing stats...");
        dbCore = new Core(this);
        importStats();

        // Schedule stats import every 6 hours (6 * 60 * 60 * 20 ticks)
        long intervalTicks = 6L * 60 * 60 * 20;
        getServer().getScheduler().runTaskTimer(this, this::importStats, intervalTicks, intervalTicks);

        // Register command executor
        getCommand("stats").setExecutor(new StatsCommand(this));
    }

    public void loadConfig() {
        this.saveDefaultConfig();
        org.bukkit.configuration.file.FileConfiguration config = getConfig();
        dbType = ConfigLoader.getDbType(config);
        cmiCompatible = ConfigLoader.isCmiCompatible(config);
        luckPermsCompatible = ConfigLoader.isLuckPermsCompatible(config);
        if ("mysql".equalsIgnoreCase(dbType)) {
            mysqlConfig = ConfigLoader.loadMySQLConfig(config);
        } else if ("mongodb".equalsIgnoreCase(dbType)) {
            mongoConfig = ConfigLoader.loadMongoConfig(config);
        }
        // Re-instantiate dbCore to reload database connections with new config
        dbCore = new Core(this);
    }

    public void importStats() {
        // Run the whole import process asynchronously except for Bukkit API calls
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
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
            for (File file : files) {
                // For each file, parse JSON async, then switch to main thread for Bukkit API,
                // then back to async for DB
                try {
                    com.fasterxml.jackson.databind.JsonNode json = objectMapper.readTree(file);
                    String uuid = file.getName().replace(".json", "");
                    // Switch to main thread for Bukkit API
                    getServer().getScheduler().runTask(this, () -> {
                        com.fasterxml.jackson.databind.JsonNode enriched = me.kiratdewas.importer.PlayerStatsImporter
                                .injectBalanceAndGroup(json, java.util.UUID.fromString(uuid));
                        // Switch back to async for DB update
                        getServer().getScheduler().runTaskAsynchronously(this, () -> {
                            dbCore.upsertPlayerStats(file, uuid); // upsertPlayerStats will use the enriched data if
                                                                  // needed
                        });
                    });
                } catch (Exception e) {
                    getLogger().warning("Failed to process file: " + file.getName() + ", error: " + e.getMessage());
                }
            }
        });
    }

    public String getDbType() {
        return dbType;
    }

    public MySQLConfig getMySQLConfig() {
        return mysqlConfig;
    }

    public MongoConfig getMongoConfig() {
        return mongoConfig;
    }

    public Core getDbCore() {
        return dbCore;
    }

    public boolean isCmiCompatible() {
        return cmiCompatible;
    }

    public boolean isLuckPermsCompatible() {
        return luckPermsCompatible;
    }

    public static class MySQLConfig {
        public String host;
        public int port;
        public String database;
        public String username;
        public String password;

        public MySQLConfig(String host, int port, String database, String username, String password) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
        }
    }

    public static class MongoConfig {
        public String uri;
        public String database;

        public MongoConfig(String uri, String database) {
            this.uri = uri;
            this.database = database;
        }
    }
}