package me.kiratdewas.stats.listener.bukkit;

import me.kiratdewas.stats.BukkitUtil;
import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.PlayerManager;
import me.kiratdewas.stats.player.StatTimeEntry;
import me.kiratdewas.stats.stat.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;

public class EntityDeath implements Listener {

    public EntityDeath(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return; // natural cause-ish.
        }
        if (event.getEntity().getKiller().hasMetadata("NPC")) {
            return;
        }
        Player player = event.getEntity().getKiller();
        PlayerManager.getInstance().getPlayer(player.getUniqueId()).subscribe(statsPlayer ->
                StatManager.getInstance().getStat("Kills").ifPresent(stat ->
                        statsPlayer.getStats(stat).addEntry(new StatTimeEntry(
                                System.currentTimeMillis(), 1,
                                getMetadata(event)
                        ))
                )
        );
    }

    private Map<String, Object> getMetadata(EntityDeathEvent event) {
        return Util.of("world", event.getEntity().getLocation().getWorld().getUID().toString(),
                "victimType", event.getEntityType().toString(),
                "victimName", Optional.ofNullable(event.getEntity().getCustomName()).orElse(event.getEntityType().toString()),
                "weapon", BukkitUtil.getWeaponName(event.getEntity().getKiller().getInventory().getItemInMainHand())
        );
    }
}
