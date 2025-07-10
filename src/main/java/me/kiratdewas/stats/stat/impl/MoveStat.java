package me.kiratdewas.stats.stat.impl;

import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.StatMetaData;
import me.kiratdewas.stats.stat.Stat;

import java.util.Collection;

public class MoveStat extends Stat {

    public MoveStat() {
        super("Move", "Amount of blocks moved");
    }

    @Override
    public Collection<StatMetaData> getMetaData() {
        return Util.listOf(new StatMetaData("world", String.class, true),
                new StatMetaData("type", String.class, true));
    }
}
