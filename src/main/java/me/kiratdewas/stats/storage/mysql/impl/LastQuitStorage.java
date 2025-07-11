package me.kiratdewas.stats.storage.mysql.impl;

import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.MySQLStatsPlayer;
import me.kiratdewas.stats.player.StatTimeEntry;
import me.kiratdewas.stats.player.StatsContainer;
import me.kiratdewas.stats.storage.mysql.MySQLWorldManager;
import me.kiratdewas.stats.storage.mysql.StatMySQLHandler;

import java.sql.*;
import java.util.*;

public class LastQuitStorage implements StatMySQLHandler {

    @Override
    public Collection<StatTimeEntry> loadEntries(Connection con, UUID uuid) throws SQLException {
        List<StatTimeEntry> entries = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(
                "SELECT * FROM stats_last_quit WHERE player_uuid=? ORDER BY timestamp DESC LIMIT 1")) {
            st.setString(1, uuid.toString());
            ResultSet set = st.executeQuery();
            while (set != null && set.next()) {
                String worldUuidStr = set.getString("world_uuid");
                long timestamp = set.getTimestamp("timestamp").getTime();
                entries.add(new StatTimeEntry(
                        timestamp, timestamp, Util.of("world", worldUuidStr)));
            }
        }
        return entries;
    }

    @Override
    public void storeEntry(Connection con, MySQLStatsPlayer player, StatsContainer container, StatTimeEntry entry)
            throws SQLException {
        try (PreparedStatement st = con
                .prepareStatement("INSERT INTO stats_last_quit (player_uuid, world_uuid, amount, `timestamp`) " +
                        "VALUES (?, ?, ?, ?)")) {
            st.setString(1, player.getUuid().toString());
            st.setString(2, entry.getMetadata().get("world").toString());
            st.setDouble(3, entry.getAmount());
            st.setTimestamp(4, new Timestamp(entry.getTimestamp()));
            st.execute();
        }
    }
}
