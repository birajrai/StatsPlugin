package me.kiratdewas.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kiratdewas.StatsPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;

public class StatsCommand implements CommandExecutor {
    private final StatsPlugin plugin;
    private final ObjectMapper objectMapper;

    public StatsCommand(StatsPlugin plugin) {
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
                sender.sendMessage("[Stats] Config and stats reloaded.");
                return true;
            } else if (args[0].equalsIgnoreCase("update") && args.length > 1) {
                String playerName = args[1];
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    boolean success = importPlayerStats(playerName);
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (success) {
                            sender.sendMessage("[Stats] Updated stats for " + playerName);
                        } else {
                            sender.sendMessage("[Stats] Could not find stats for " + playerName);
                        }
                    });
                });
                return true;
            }
        }
        sender.sendMessage("[Stats] Usage: /stats reload | /stats update <playername>");
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