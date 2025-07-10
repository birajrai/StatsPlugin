package me.kiratdewas.stats.storage.mysql;

import me.kiratdewas.stats.player.MySQLStatsPlayer;
import me.kiratdewas.stats.player.StatTimeEntry;
import me.kiratdewas.stats.player.StatsContainer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

public interface StatMySQLHandler {

    Collection<StatTimeEntry> loadEntries(Connection con, UUID uuid) throws SQLException;

    void storeEntry(Connection con, MySQLStatsPlayer player, StatsContainer container, StatTimeEntry entry) throws SQLException;
}
