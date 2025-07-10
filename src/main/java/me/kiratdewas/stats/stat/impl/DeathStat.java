package me.kiratdewas.stats.stat.impl;

import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.StatMetaData;
import me.kiratdewas.stats.stat.Stat;

import java.util.Collection;

public class DeathStat extends Stat {
    public DeathStat() {
        super("Deaths", "Number of player deaths");
    }

    @Override
    public Collection<StatMetaData> getMetaData() {
        return Util.listOf(
                new StatMetaData("cause", String.class, true),
                new StatMetaData("world", String.class, true),
                new StatMetaData("loc_x", Double.class, false),
                new StatMetaData("loc_y", Double.class, false),
                new StatMetaData("loc_z", Double.class, false)
        );
    }
}
