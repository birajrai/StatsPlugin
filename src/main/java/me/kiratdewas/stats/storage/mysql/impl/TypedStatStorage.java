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
public class TypedStatStorage implements StatMySQLHandler {

    private final Stat stat;

    public TypedStatStorage(Stat stat) {
        this.stat = stat;
    }

    private String getTableName() {
        return "stats_" + this.stat.getName().toLowerCase().replace(" ", "_");
    }

    @Override
    public Collection<StatTimeEntry> loadEntries(Connection con, UUID uuid) throws SQLException {
        List<StatTimeEntry> entries = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement("SELECT type, world_uuid, amount, last_updated " +
                "FROM " + getTableName() + " WHERE player_uuid=?")) {
            st.setString(1, uuid.toString());
            ResultSet set = st.executeQuery();
            while (set != null && set.next()) {
                String worldUuidStr = set.getString("world_uuid");
                entries.add(new StatTimeEntry(
                        set.getTimestamp("last_updated").getTime(), set.getDouble("amount"),
                        Util.of("world", worldUuidStr, "type", set.getString("type"))));
            }
        }
        return entries;
    }

    @Override
    public void storeEntry(Connection con, MySQLStatsPlayer player, StatsContainer container, StatTimeEntry entry)
            throws SQLException {
        try (PreparedStatement st = con
                .prepareStatement("INSERT INTO " + getTableName() + " (player_uuid, world_uuid, type, amount) " +
                        "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount=amount+VALUES(amount)")) {
            st.setString(1, player.getUuid().toString());
            st.setString(2, entry.getMetadata().get("world").toString());
            st.setObject(3, entry.getMetadata().get("type"));
            st.setDouble(4, entry.getAmount());
            st.execute();
        }
    }

    public void deleteEntriesForPlayerType(Connection con, UUID playerUuid, String worldUuid, String type)
            throws SQLException {
        String sql = "DELETE FROM " + getTableName()
                + " WHERE player_uuid=? AND world_uuid=? AND type=?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, playerUuid.toString());
            st.setString(2, worldUuid);
            st.setString(3, type);
            st.executeUpdate();
        }
    }
}