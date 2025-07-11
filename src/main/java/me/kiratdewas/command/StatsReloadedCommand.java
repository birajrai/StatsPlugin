package me.kiratdewas.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kiratdewas.StatsReloadedPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;

public class StatsReloadedCommand implements CommandExecutor {
    private final StatsReloadedPlugin plugin;
    private final ObjectMapper objectMapper;

    public StatsReloadedCommand(StatsReloadedPlugin plugin) {
        this.plugin = plugin;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                plugin.loadConfig();
                plugin.importStats();
                sender.sendMessage("[StatsReloaded] Config and stats reloaded.");
                return true;
            } else if (args[0].equalsIgnoreCase("update") && args.length > 1) {
                String playerName = args[1];
                boolean success = importPlayerStats(playerName);
                if (success) {
                    sender.sendMessage("[StatsReloaded] Updated stats for " + playerName);
                } else {
                    sender.sendMessage("[StatsReloaded] Could not find stats for " + playerName);
                }
                return true;
            }
        }
        sender.sendMessage("[StatsReloaded] Usage: /statsreloaded reload | /statsreloaded update <playername>");
        return true;
    }

    // Import stats for a single player by name (or UUID string)
    private boolean importPlayerStats(String playerName) {
        // Try to resolve player name to UUID (offline mode: use name as UUID)
        java.util.UUID uuid = null;
        try {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerName);
            uuid = offlinePlayer.getUniqueId();
        } catch (Exception e) {
            // fallback: try as UUID string
            try {
                uuid = java.util.UUID.fromString(playerName);
            } catch (Exception ignored) {
            }
        }
        if (uuid == null)
            return false;
        File statsDir = new File(plugin.getServer().getWorldContainer(), "world/stats");
        File file = new File(statsDir, uuid.toString() + ".json");
        if (!file.exists())
            return false;
        return plugin.getDbCore().upsertPlayerStats(file, uuid.toString());
    }
}