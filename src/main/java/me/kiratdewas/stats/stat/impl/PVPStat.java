package me.kiratdewas.stats.stat.impl;

import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.StatMetaData;
import me.kiratdewas.stats.stat.Stat;

import java.util.Collection;

public class PVPStat extends Stat {

    public PVPStat() {
        super("PVP Kills", "The amount of players you have slain");
    }

    @Override
    public Collection<StatMetaData> getMetaData() {
        return Util.listOf(new StatMetaData("world", String.class, true),
                new StatMetaData("victimType", String.class, true),
                new StatMetaData("victimName", String.class, false),
                new StatMetaData("weapon", String.class, false));
    }
}
