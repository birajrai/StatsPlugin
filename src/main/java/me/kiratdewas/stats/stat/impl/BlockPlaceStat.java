package me.kiratdewas.stats.stat.impl;

import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.StatMetaData;
import me.kiratdewas.stats.stat.Stat;

import java.util.Collection;

public class BlockPlaceStat extends Stat {

    public BlockPlaceStat() {
        super("Blocks placed", "Amount of blocks placed");
    }

    @Override
    public Collection<StatMetaData> getMetaData() {
        return Util.listOf(new StatMetaData("world", String.class, true),
                new StatMetaData("loc_x", Integer.class, false),
                new StatMetaData("loc_y", Integer.class, false),
                new StatMetaData("loc_z", Integer.class, false),
                new StatMetaData("material", String.class, true));
    }
}
