package me.kiratdewas.stats;

import io.reactivex.rxjava3.schedulers.Schedulers;
import me.kiratdewas.stats.listener.bukkit.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import me.kiratdewas.stats.converters.Stats2;
import me.kiratdewas.stats.listener.Playtime;
import nl.lolmewn.stats.listener.bukkit.*;
import me.kiratdewas.stats.player.PlayerManager;
import me.kiratdewas.stats.player.SimpleStatContainer;
import me.kiratdewas.stats.player.StatsPlayer;
import me.kiratdewas.stats.signs.BukkitSignListener;
import me.kiratdewas.stats.signs.SignManager;
import me.kiratdewas.stats.stat.StatManager;
import me.kiratdewas.stats.storage.WorldManager;
import me.kiratdewas.stats.storage.mysql.MySQLConfig;
import me.kiratdewas.stats.storage.mysql.MySQLStorage;
import me.kiratdewas.stats.storage.mysql.MySQLWorldManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

public class BukkitMain extends JavaPlugin {

    private MySQLStorage storage;
    private WorldManager worldManager;

    private static final Logger LOG = Logger.getLogger(BukkitMain.class.getName());

    @Override
    public void onEnable() {
        Schedulers.start();

        super.getConfig().addDefault("server-id", UUID.randomUUID().toString());
        super.getConfig().options().copyDefaults(true);
        super.saveConfig();

        this.checkConversion();

        if (super.getConfig().getString("mysql.username", "username").equals("username")) {
            this.LOG.info("Stats is not yet configured");
            this.LOG.info("Stats has generated a config.yml file");
            this.LOG.info("Please configure Stats and then restart your server");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        SharedMain.registerStats();

        try {
            this.storage = new MySQLStorage(this.getMySQLConfig());
            this.worldManager = new MySQLWorldManager(this.storage);
            new SignManager(this, this.storage);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            this.LOG.severe("MySQL error, not starting plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new WorldListener(this, this.worldManager);
        new BlockBreak(this);
        new BlockPlace(this);
        new PlayerDeath(this);
        new EntityDeath(this);
        new PlayerMove(this);
        new Playtime();
        new SimpleStatsListener(this);
        new BukkitSignListener(this);

        SharedMain.serverUuid = super.getConfig().getString("server-id");
        SharedMain.setDebug(super.getConfig().getBoolean("debug", false));

        int pluginId = 4523;
        new Metrics(this, pluginId);
    }

    private void checkConversion() {
        if (getConfig().contains("storage-version")) {
            // Stats3
        }
        if (getConfig().contains("snapshots")) {
            // Stats2
            new Stats2(this.getConfig());
        }
        if (getConfig().contains("convertFrom")) {
            if (getConfig().getInt("convertFrom") == 2) {
                new Stats2(this.getConfig());
            }
            if (getConfig().getInt("convertFrom") == 3) {

            }
        }
    }

    private MySQLConfig getMySQLConfig() {
        return new MySQLConfig(
                this.getConfig().getString("mysql.url"),
                this.getConfig().getString("mysql.username"),
                this.getConfig().getString("mysql.password"));
    }

    @Override
    public void onDisable() {
        if (this.storage != null)
            this.storage.shutdown();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Your total stats");
        PlayerManager.getInstance().getPlayer(((Player) sender).getUniqueId())
                .subscribe(player -> sendStatistics(sender, player), err -> {
                    sender.sendMessage(ChatColor.RED + "An Unknown error occurred!");
                    this.LOG.warning("Command error: " + err);
                });
        return true;
    }

    private void sendStatistics(CommandSender sender, StatsPlayer player) {
        StatManager.getInstance().getStats().forEach(stat -> {
            TextComponent statMessage = new TextComponent(stat.getName());
            statMessage.setColor(ChatColor.DARK_GREEN);
            statMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(stat.getDescription()).color(ChatColor.GOLD).create()));
            TextComponent colon = new TextComponent(": ");
            colon.setColor(ChatColor.RED);
            TextComponent statValue = new TextComponent(stat.format(player.getStats(stat).getTotal()));
            statValue.setColor(ChatColor.GOLD);
            statValue.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(getValuesFor("world", player.getStats(stat).getSimpleStatContainer())
                            .entrySet().stream().map(
                                    entry -> "In " + getWorldName(entry.getKey()) + ": "
                                            + stat.format(entry.getValue()))
                            .reduce((s, s2) -> s + "\n" + s2).orElse("No data recorded yet!")).create()));
            sender.spigot().sendMessage(statMessage, colon, statValue);
        });
    }

    public Map<String, Double> getValuesFor(String metadataKey, SimpleStatContainer statContainer) {
        Map<String, Double> results = new TreeMap<>();
        statContainer.getValues().entrySet().stream()
                .filter(e -> e.getKey().containsKey(metadataKey))
                .forEach(e -> results.merge(e.getKey().get(metadataKey).toString(), e.getValue(), Double::sum));
        return results;
    }

    public String getWorldName(String worldId) {
        try {
            UUID uuid = UUID.fromString(worldId);
            World world = getServer().getWorld(uuid);
            if (world != null) {
                return world.getName();
            }
        } catch (IllegalArgumentException ignored) {
        }
        return "Unknown world";
    }

    public MySQLStorage getMySQLStorage() {
        return this.storage;
    }
}
