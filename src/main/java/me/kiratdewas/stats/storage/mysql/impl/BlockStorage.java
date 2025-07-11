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

public class BlockStorage implements StatMySQLHandler {

    private final boolean breaking;
    private final String tableName;

    public BlockStorage(boolean breaking) {
        this.breaking = breaking;
        this.tableName = breaking ? "stats_block_break" : "stats_block_place";
    }

    @SuppressWarnings("SqlResolve") // Shh the not being able to find columns because I'm using this.tableName
                                    // errors
    @Override
    public Collection<StatTimeEntry> loadEntries(Connection con, UUID uuid) throws SQLException {
        List<StatTimeEntry> entries = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE player_uuid=?")) {
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
                    // fallback to last_updated if timestamp is missing
                    timestamp = set.getTimestamp("last_updated").getTime();
                }
                if (breaking) {
                    entries.add(new StatTimeEntry(
                            timestamp, set.getDouble("amount"),
                            Util.of("world", worldUuidStr,
                                    "material", set.getString("material"),
                                    "tool", set.getString("tool"),
                                    "loc_x", x,
                                    "loc_y", y,
                                    "loc_z", z)));
                } else {
                    entries.add(new StatTimeEntry(
                            timestamp, set.getDouble("amount"),
                            Util.of("world", worldUuidStr,
                                    "material", set.getString("material"),
                                    "tool", set.getString("tool"),
                                    "loc_x", x,
                                    "loc_y", y,
                                    "loc_z", z)));
                }
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
        String tool = entry.getMetadata().get("tool") != null ? entry.getMetadata().get("tool").toString() : null;
        java.sql.Timestamp timestamp = new java.sql.Timestamp(entry.getTimestamp());
        if (breaking) {
            try (PreparedStatement st = con.prepareStatement(
                    "INSERT INTO stats_block_break (player_uuid, world_uuid, material, tool, amount, x, y, z, timestamp) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount=amount+VALUES(amount)")) {
                st.setString(1, player.getUuid().toString());
                st.setString(2, entry.getMetadata().get("world").toString());
                st.setObject(3, entry.getMetadata().get("material"));
                st.setObject(4, tool);
                st.setDouble(5, entry.getAmount());
                st.setInt(6, x);
                st.setInt(7, y);
                st.setInt(8, z);
                st.setTimestamp(9, timestamp);
                st.execute();
            }
        } else {
            try (PreparedStatement st = con
                    .prepareStatement("INSERT INTO stats_block_place (player_uuid, world_uuid, material, tool, amount, x, y, z, timestamp) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount=amount+VALUES(amount)")) {
                st.setString(1, player.getUuid().toString());
                st.setString(2, entry.getMetadata().get("world").toString());
                st.setObject(3, entry.getMetadata().get("material"));
                st.setObject(4, tool);
                st.setDouble(5, entry.getAmount());
                st.setInt(6, x);
                st.setInt(7, y);
                st.setInt(8, z);
                st.setTimestamp(9, timestamp);
                st.execute();
            }
        }
    }

    private void inputCommon(MySQLStatsPlayer player, StatTimeEntry entry, PreparedStatement st) throws SQLException {
        st.setString(1, player.getUuid().toString());
        st.setString(2, entry.getMetadata().get("world").toString());
        st.setObject(3, entry.getMetadata().get("material"));
    }
}
