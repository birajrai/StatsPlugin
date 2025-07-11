package me.kiratdewas.stats.stat.impl;

import me.kiratdewas.stats.Util;
import me.kiratdewas.stats.player.StatMetaData;
import me.kiratdewas.stats.stat.Stat;

import java.util.Collection;

public class JumpsStat extends Stat {
    public JumpsStat() {
        super("Jumps", "Number of jumps performed");
    }

    @Override
    public Collection<StatMetaData> getMetaData() {
        return Util.listOf(
            new StatMetaData("world", String.class, true),
            new StatMetaData("type", String.class, true)
        );
    }
} 