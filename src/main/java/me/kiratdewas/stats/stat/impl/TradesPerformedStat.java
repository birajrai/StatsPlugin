package me.kiratdewas.stats.stat.impl;

import me.kiratdewas.stats.SimpleItem;
import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.StatMetaData;
import me.kiratdewas.stats.stat.Stat;

import java.util.Collection;
import java.util.List;

public class TradesPerformedStat extends Stat {

    public TradesPerformedStat() {
        super("Trades performed", "Amount of trades done with villagers");
    }

    @Override
    public Collection<StatMetaData> getMetaData() {
        return Util.listOf(new StatMetaData("world", String.class, true),
                new StatMetaData("item", SimpleItem.class, false),
                new StatMetaData("price", List.class, false));
    }
}
