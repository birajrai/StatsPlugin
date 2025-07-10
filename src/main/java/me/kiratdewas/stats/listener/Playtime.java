package me.kiratdewas.stats.listener;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.PlayerManager;
import me.kiratdewas.stats.player.StatTimeEntry;
import me.kiratdewas.stats.stat.StatManager;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class Playtime {

    private Disposable disposable;

    public Playtime() {
        this.recordPlaytime();
    }

    private void recordPlaytime() {
        if (this.disposable != null) {
            // Already running
            return;
        }
        this.disposable = Observable.interval(1, TimeUnit.SECONDS).forEach(ignored ->
                Bukkit.getServer().getOnlinePlayers().forEach(player ->
                        PlayerManager.getInstance().getPlayer(player.getUniqueId()).subscribe(statsPlayer ->
                                StatManager.getInstance().getStat("Playtime").ifPresent(stat ->
                                        statsPlayer.getStats(stat).addEntry(
                                                new StatTimeEntry(System.currentTimeMillis(), 1,
                                                        Util.of("world", player.getWorld().getUID().toString()))
                                        )
                                ), Util::handleError
                        )
                )
        );
    }
}
