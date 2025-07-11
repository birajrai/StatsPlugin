package me.kiratdewas.config;

import me.kiratdewas.StatsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLoader {
    public static String getDbType(FileConfiguration config) {
        return config.getString("database.type");
    }

    public static StatsPlugin.MySQLConfig loadMySQLConfig(FileConfiguration config) {
        org.bukkit.configuration.ConfigurationSection mysql = config.getConfigurationSection("database.mysql");
        return new StatsPlugin.MySQLConfig(
                mysql.getString("host"),
                mysql.getInt("port"),
                mysql.getString("database"),
                mysql.getString("username"),
                mysql.getString("password"));
    }

    public static StatsPlugin.MongoConfig loadMongoConfig(FileConfiguration config) {
        org.bukkit.configuration.ConfigurationSection mongo = config.getConfigurationSection("database.mongodb");
        return new StatsPlugin.MongoConfig(
                mongo.getString("uri"),
                mongo.getString("database"));
    }

    public static boolean isCmiCompatible(org.bukkit.configuration.file.FileConfiguration config) {
        return config.getBoolean("cmi-compatible", false);
    }

    public static boolean isLuckPermsCompatible(org.bukkit.configuration.file.FileConfiguration config) {
        return config.getBoolean("luckperms-compatible", false);
    }
}