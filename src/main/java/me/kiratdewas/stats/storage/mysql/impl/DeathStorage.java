package me.kiratdewas.stats.storage.mysql.impl;

import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.MySQLStatsPlayer;
import me.kiratdewas.stats.player.StatTimeEntry;
import me.kiratdewas.stats.player.StatsContainer;
import me.kiratdewas.stats.storage.mysql.MySQLWorldManager;
import me.kiratdewas.stats.storage.mysql.StatMySQLHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DeathStorage implements StatMySQLHandler {

    @Override
    public Collection<StatTimeEntry> loadEntries(Connection con, UUID uuid) throws SQLException {
        List<StatTimeEntry> entries = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement("SELECT * FROM stats_death WHERE player_uuid=?")) {
            st.setString(1, uuid.toString());
            ResultSet set = st.executeQuery();
            while (set != null && set.next()) {
                String worldUuidStr = set.getString("world_uuid");
                int x = set.getInt("x");
                int y = set.getInt("y");
                int z = set.getInt("z");
                long timestamp = 0L;
                try {
                    timestamp = set.getTimestamp("timestamp").getTime();
                } catch (Exception e) {
                    timestamp = set.getTimestamp("last_updated").getTime();
                }
                entries.add(new StatTimeEntry(
                        timestamp, set.getDouble("amount"),
                        Util.of("world", worldUuidStr,
                                "cause", set.getString("cause"),
                                "loc_x", x,
                                "loc_y", y,
                                "loc_z", z)));
            }
        }
        return entries;
    }

    @Override
    public void storeEntry(Connection con, MySQLStatsPlayer player, StatsContainer container, StatTimeEntry entry)
            throws SQLException {
        int x = entry.getMetadata().get("loc_x") != null ? (int) entry.getMetadata().get("loc_x") : 0;
        int y = entry.getMetadata().get("loc_y") != null ? (int) entry.getMetadata().get("loc_y") : 0;
        int z = entry.getMetadata().get("loc_z") != null ? (int) entry.getMetadata().get("loc_z") : 0;
        java.sql.Timestamp timestamp = new java.sql.Timestamp(entry.getTimestamp());
        try (PreparedStatement st = con
                .prepareStatement("INSERT INTO stats_death (player_uuid, world_uuid, cause, amount, x, y, z, timestamp, last_updated) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE amount=amount+VALUES(amount), last_updated=CURRENT_TIMESTAMP")) {
            st.setString(1, player.getUuid().toString());
            st.setString(2, entry.getMetadata().get("world").toString());
            st.setString(3, entry.getMetadata().get("cause").toString());
            st.setDouble(4, entry.getAmount());
            st.setInt(5, x);
            st.setInt(6, y);
            st.setInt(7, z);
            st.setTimestamp(8, timestamp);
            st.execute();
        }
    }
}
