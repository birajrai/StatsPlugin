package me.kiratdewas.stats.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WorldManager {

    private Map<UUID, String> nameMap = new HashMap<>();

    public String getName(UUID uuid) {
        return this.nameMap.get(uuid);
    }

    public void setWorld(UUID uuid, String name) {
        this.nameMap.put(uuid, name);
    }

    public void setWeather(UUID uid, boolean thundering) {
        throw new IllegalStateException("Can't save weather for world, not implemented");
    }
}
