package me.kiratdewas.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import net.milkbowl.vault.economy.Economy;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;

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

    public static JsonNode injectBalanceAndGroup(JsonNode stats, UUID uuid) {
        ObjectNode statsWithCustom = stats.deepCopy();
        // Get balance using Vault (CMI Economy)
        Economy economy = null;
        try {
            economy = (Economy) Bukkit.getServer().getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.economy.Economy")).getProvider();
        } catch (Exception e) {
            // Vault or CMI Economy not present
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        double balance = (economy != null) ? economy.getBalance(player) : 0.0;
        statsWithCustom.put("balance", balance);
        // Get group using LuckPerms
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().loadUser(uuid).join();
        String group = user.getPrimaryGroup();
        statsWithCustom.put("group", group);
        return statsWithCustom;
    }
}