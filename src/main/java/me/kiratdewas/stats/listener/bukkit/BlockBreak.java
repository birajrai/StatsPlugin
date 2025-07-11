package me.kiratdewas.stats.listener.bukkit;

import me.kiratdewas.stats.BukkitUtil;
import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.PlayerManager;
import me.kiratdewas.stats.player.StatTimeEntry;
import me.kiratdewas.stats.stat.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class BlockBreak implements Listener {

    public BlockBreak(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        PlayerManager.getInstance().getPlayer(event.getPlayer().getUniqueId()).subscribe(player ->
            StatManager.getInstance().getStat("Blocks broken").ifPresent(stat -> {
                Map<String, Object> metadata = generateMetadata(event);
                player.getStats(stat).addEntry(
                        new StatTimeEntry(System.currentTimeMillis(), 1, metadata)
                );
            }), Util::handleError
        );
    }

    private Map<String, Object> generateMetadata(BlockBreakEvent event) {
        Map<String, Object> map = new HashMap<>();
        map.put("world", event.getBlock().getWorld().getUID().toString());
        map.put("material", BukkitUtil.getMaterialType(event.getBlock().getType()));
        map.put("tool", BukkitUtil.getItemType(event.getPlayer().getInventory().getItemInMainHand()));
        map.put("loc_x", event.getBlock().getX());
        map.put("loc_y", event.getBlock().getY());
        map.put("loc_z", event.getBlock().getZ());
        return map;
    }
}
