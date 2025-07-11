package me.kiratdewas.stats.storage.mysql.impl;

import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.MySQLStatsPlayer;
import me.kiratdewas.stats.player.StatTimeEntry;
import me.kiratdewas.stats.player.StatsContainer;
import me.kiratdewas.stats.stat.Stat;
import me.kiratdewas.stats.storage.mysql.MySQLWorldManager;
import me.kiratdewas.stats.storage.mysql.StatMySQLHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("SqlResolve")
public class GeneralStatStorage implements StatMySQLHandler {

    private final Stat stat;

    public GeneralStatStorage(Stat stat) {
        this.stat = stat;
    }

    private String getTableName() {
        return "stats_" + this.stat.getName().toLowerCase().replace(" ", "_");
    }

    @Override
    public Collection<StatTimeEntry> loadEntries(Connection con, UUID uuid) throws SQLException {
        List<StatTimeEntry> entries = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement("SELECT world_uuid, amount, last_updated " +
                "FROM " + getTableName() + " WHERE player_uuid=?")) {
            st.setString(1, uuid.toString());
            ResultSet set = st.executeQuery();
            while (set != null && set.next()) {
                String worldUuidStr = set.getString("world_uuid");
                entries.add(new StatTimeEntry(
                        set.getTimestamp("last_updated").getTime(), set.getDouble("amount"),
                        Util.of("world", worldUuidStr)));
            }
        }
        return entries;
    }

    @Override
    public void storeEntry(Connection con, MySQLStatsPlayer player, StatsContainer container, StatTimeEntry entry)
            throws SQLException {
        try (PreparedStatement st = con
                .prepareStatement("INSERT INTO " + getTableName() + " (player_uuid, world_uuid, amount) " +
                        "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount=amount+VALUES(amount)")) {
            st.setString(1, player.getUuid().toString());
            st.setString(2, entry.getMetadata().get("world").toString());
            st.setDouble(3, entry.getAmount());
            st.execute();
        }
    }
}
