package me.kiratdewas.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.kiratdewas.StatsReloadedPlugin;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class MongoDB {
    private final StatsReloadedPlugin.MongoConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StatsReloadedPlugin plugin;

    public MongoDB(StatsReloadedPlugin plugin, StatsReloadedPlugin.MongoConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean upsertPlayerStats(File file, String uuid) {
        try (MongoClient mongoClient = MongoClients.create(config.uri)) {
            MongoDatabase db = mongoClient.getDatabase(config.database);
            MongoCollection<Document> collection = db.getCollection("player_stats");
            JsonNode json = objectMapper.readTree(file);
            Document doc = new Document("uuid", uuid).append("stats", Document.parse(json.toString()));
            collection.replaceOne(new Document("uuid", uuid), doc,
                    new com.mongodb.client.model.ReplaceOptions().upsert(true));
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to import stats to MongoDB for " + uuid, e);
            return false;
        }
    }

    public void bulkImport(File[] files) {
        try (MongoClient mongoClient = MongoClients.create(config.uri)) {
            MongoDatabase db = mongoClient.getDatabase(config.database);
            MongoCollection<Document> collection = db.getCollection("player_stats");
            for (File file : files) {
                String uuid = file.getName().replace(".json", "");
                JsonNode json = objectMapper.readTree(file);
                Document doc = new Document("uuid", uuid).append("stats", Document.parse(json.toString()));
                collection.replaceOne(new Document("uuid", uuid), doc,
                        new com.mongodb.client.model.ReplaceOptions().upsert(true));
            }
            plugin.getLogger().info("Imported stats to MongoDB.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to import stats to MongoDB", e);
        }
    }
}