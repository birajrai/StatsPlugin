package me.kiratdewas.database;

import me.kiratdewas.StatsReloadedPlugin;
import java.io.File;

public class Core {
    private final StatsReloadedPlugin plugin;
    private final String dbType;
    private final MySQL mySQL;
    private final MongoDB mongoDB;

    public Core(StatsReloadedPlugin plugin) {
        this.plugin = plugin;
        this.dbType = plugin.getDbType();
        this.mySQL = dbType.equalsIgnoreCase("mysql") ? new MySQL(plugin, plugin.getMySQLConfig()) : null;
        this.mongoDB = dbType.equalsIgnoreCase("mongodb") ? new MongoDB(plugin, plugin.getMongoConfig()) : null;
    }

    public boolean upsertPlayerStats(File file, String uuid) {
        if (dbType.equalsIgnoreCase("mysql")) {
            return mySQL.upsertPlayerStats(file, uuid);
        } else if (dbType.equalsIgnoreCase("mongodb")) {
            return mongoDB.upsertPlayerStats(file, uuid);
        }
        return false;
    }

    public void bulkImport(File[] files) {
        if (dbType.equalsIgnoreCase("mysql")) {
            mySQL.bulkImport(files);
        } else if (dbType.equalsIgnoreCase("mongodb")) {
            mongoDB.bulkImport(files);
        }
    }
}