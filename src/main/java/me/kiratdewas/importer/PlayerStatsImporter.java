package me.kiratdewas.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Server;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerStatsImporter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static File getStatsFile(Server server, UUID uuid) {
        File statsDir = new File(server.getWorldContainer(), "world/stats");
        return new File(statsDir, uuid.toString() + ".json");
    }

    public static JsonNode readStats(File file) throws IOException {
        return objectMapper.readTree(file);
    }

    public static String readStatsAsString(File file) throws IOException {
        return objectMapper.readTree(file).toString();
    }
}