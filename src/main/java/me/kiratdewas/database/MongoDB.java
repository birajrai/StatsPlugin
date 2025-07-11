package me.kiratdewas.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Sorts;
import org.bson.conversions.Bson;
import me.kiratdewas.StatsPlugin;
import me.kiratdewas.importer.PlayerStatsImporter;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

public class MongoDB {
    static {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARNING);
    }
    private final StatsPlugin.MongoConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StatsPlugin plugin;

    public MongoDB(StatsPlugin plugin, StatsPlugin.MongoConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    // Upsert a leaderboard value for a player
    private void upsertLeaderboardValue(MongoDatabase db, String collection, String uuid, String field, Number value) {
        MongoCollection<Document> leaderboard = db.getCollection(collection);
        Document doc = new Document("uuid", uuid).append(field, value);
        leaderboard.replaceOne(new Document("uuid", uuid), doc,
                new com.mongodb.client.model.ReplaceOptions().upsert(true));
    }

    public boolean upsertPlayerStats(File file, String uuid) {
        try (MongoClient mongoClient = MongoClients.create(config.uri)) {
            MongoDatabase db = mongoClient.getDatabase(config.database);
            MongoCollection<Document> collection = db.getCollection("player_stats");
            JsonNode json = objectMapper.readTree(file);
            // Inject balance and group
            json = PlayerStatsImporter.injectBalanceAndGroup(json, UUID.fromString(uuid));
            Document doc = new Document("uuid", uuid).append("stats", Document.parse(json.toString()));
            collection.replaceOne(new Document("uuid", uuid), doc,
                    new com.mongodb.client.model.ReplaceOptions().upsert(true));
            // Upsert leaderboard values with correct nested paths
            upsertLeaderboardValue(db, "topbalance", uuid, "balance", json.path("balance").asDouble(0));
            upsertLeaderboardValue(db, "mostblockbroken", uuid, "blockbroken",
                    json.path("minecraft:custom").path("minecraft:mine_block").asInt(0));
            upsertLeaderboardValue(db, "mostplaytime", uuid, "playtime",
                    json.path("minecraft:custom").path("minecraft:play_one_minute").asInt(0));
            upsertLeaderboardValue(db, "mostkills", uuid, "kills",
                    json.path("minecraft:custom").path("minecraft:player_kills").asInt(0));
            upsertLeaderboardValue(db, "mostdeath", uuid, "deaths",
                    json.path("minecraft:custom").path("minecraft:deaths").asInt(0));
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
                // Inject balance and group
                json = PlayerStatsImporter.injectBalanceAndGroup(json, UUID.fromString(uuid));
                Document doc = new Document("uuid", uuid).append("stats", Document.parse(json.toString()));
                collection.replaceOne(new Document("uuid", uuid), doc,
                        new com.mongodb.client.model.ReplaceOptions().upsert(true));
                // Upsert leaderboard values with correct nested paths
                upsertLeaderboardValue(db, "topbalance", uuid, "balance", json.path("balance").asDouble(0));
                upsertLeaderboardValue(db, "mostblockbroken", uuid, "blockbroken",
                        json.path("minecraft:custom").path("minecraft:mine_block").asInt(0));
                upsertLeaderboardValue(db, "mostplaytime", uuid, "playtime",
                        json.path("minecraft:custom").path("minecraft:play_one_minute").asInt(0));
                upsertLeaderboardValue(db, "mostkills", uuid, "kills",
                        json.path("minecraft:custom").path("minecraft:player_kills").asInt(0));
                upsertLeaderboardValue(db, "mostdeath", uuid, "deaths",
                        json.path("minecraft:custom").path("minecraft:deaths").asInt(0));
            }
            plugin.getLogger().info("Imported stats to MongoDB.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to import stats to MongoDB", e);
        }
    }

    public List<Document> getTop(String collectionName, String field, int limit) {
        try (MongoClient mongoClient = MongoClients.create(config.uri)) {
            MongoDatabase db = mongoClient.getDatabase(config.database);
            MongoCollection<Document> collection = db.getCollection(collectionName);
            Bson sort = Sorts.descending(field);
            FindIterable<Document> docs = collection.find().sort(sort).limit(limit);
            List<Document> result = new ArrayList<>();
            for (Document doc : docs) {
                result.add(doc);
            }
            return result;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to fetch top from " + collectionName, e);
            return new ArrayList<>();
        }
    }

    public List<Document> getTopBalance(int limit) {
        return getTop("topbalance", "balance", limit);
    }

    public List<Document> getMostBlockBroken(int limit) {
        return getTop("mostblockbroken", "blockbroken", limit);
    }

    public List<Document> getMostPlaytime(int limit) {
        return getTop("mostplaytime", "playtime", limit);
    }

    public List<Document> getMostKills(int limit) {
        return getTop("mostkills", "kills", limit);
    }

    public List<Document> getMostDeath(int limit) {
        return getTop("mostdeath", "deaths", limit);
    }
}