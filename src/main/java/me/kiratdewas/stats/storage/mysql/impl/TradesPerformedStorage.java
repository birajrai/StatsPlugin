package me.kiratdewas.stats.storage.mysql.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.kiratdewas.stats.SimpleItem;
import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.MySQLStatsPlayer;
import me.kiratdewas.stats.player.StatTimeEntry;
import me.kiratdewas.stats.player.StatsContainer;
import me.kiratdewas.stats.storage.mysql.MySQLWorldManager;
import me.kiratdewas.stats.storage.mysql.StatMySQLHandler;

import java.sql.*;
import java.util.*;

public class TradesPerformedStorage implements StatMySQLHandler {

    private final Gson gson = new Gson();

    @Override
    public Collection<StatTimeEntry> loadEntries(Connection con, UUID uuid) throws SQLException {
        List<StatTimeEntry> entries = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement("SELECT * FROM stats_trades_performed WHERE player_uuid=?")) {
            st.setString(1, uuid.toString());
            ResultSet set = st.executeQuery();
            while (set != null && set.next()) {
                String worldUuidStr = set.getString("world_uuid");
                entries.add(new StatTimeEntry(
                        set.getTimestamp("timestamp").getTime(), 1,
                        Util.of("world", worldUuidStr,
                                "item", this.gson.fromJson(set.getString("item"), SimpleItem.class),
                                "price",
                                this.gson.fromJson(set.getString("price"), new TypeToken<ArrayList<SimpleItem>>() {
                                }.getType()))));
            }
        }
        return entries;
    }

    @Override
    public void storeEntry(Connection con, MySQLStatsPlayer player, StatsContainer container, StatTimeEntry entry)
            throws SQLException {
        try (PreparedStatement st = con.prepareStatement(
                "INSERT INTO stats_trades_performed (player_uuid, world_uuid, item, price, timestamp) " +
                        "VALUES (?, ?, ?, ?, ?)")) {
            st.setString(1, player.getUuid().toString());
            st.setString(2, entry.getMetadata().get("world").toString());
            st.setString(3, this.gson.toJson(entry.getMetadata().get("item")));
            st.setString(4, this.gson.toJson(entry.getMetadata().get("price")));
            st.setTimestamp(5, new Timestamp(entry.getTimestamp()));
            st.execute();
        }
    }
}
