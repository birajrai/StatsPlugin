package me.kiratdewas.config;

import me.kiratdewas.StatsReloadedPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLoader {
    public static String getDbType(FileConfiguration config) {
        return config.getString("database.type");
    }

    public static StatsReloadedPlugin.MySQLConfig loadMySQLConfig(FileConfiguration config) {
        org.bukkit.configuration.ConfigurationSection mysql = config.getConfigurationSection("database.mysql");
        return new StatsReloadedPlugin.MySQLConfig(
                mysql.getString("host"),
                mysql.getInt("port"),
                mysql.getString("database"),
                mysql.getString("username"),
                mysql.getString("password"));
    }

    public static StatsReloadedPlugin.MongoConfig loadMongoConfig(FileConfiguration config) {
        org.bukkit.configuration.ConfigurationSection mongo = config.getConfigurationSection("database.mongodb");
        return new StatsReloadedPlugin.MongoConfig(
                mongo.getString("uri"),
                mongo.getString("database"));
    }
}