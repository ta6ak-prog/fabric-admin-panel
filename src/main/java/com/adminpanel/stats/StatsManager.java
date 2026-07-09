package com.adminpanel.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, PlayerStats> stats = new ConcurrentHashMap<>();
    private Path statsFile;

    public void init(MinecraftServer server) {
        statsFile = server.getRunDirectory().toPath().resolve("adminpanel/stats.json");
        load();
    }

    public void save() {
        if (statsFile == null) return;
        try {
            Files.createDirectories(statsFile.getParent());
            Files.writeString(statsFile, GSON.toJson(stats));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (statsFile == null || !Files.exists(statsFile)) return;
        try {
            String json = Files.readString(statsFile);
            Map<String, PlayerStats> loaded = GSON.fromJson(json, new TypeToken<Map<String, PlayerStats>>(){}.getType());
            if (loaded != null) stats.putAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerStats getOrCreate(String playerName) {
        return stats.computeIfAbsent(playerName.toLowerCase(), k -> new PlayerStats(k));
    }

    public Map<String, PlayerStats> getAllStats() {
        return Collections.unmodifiableMap(stats);
    }

    public List<PlayerStats> getTopPlayers(String field, int count) {
        return stats.values().stream()
            .sorted(Comparator.comparingLong(s -> -getFieldValue(s, field)))
            .limit(count)
            .toList();
    }

    private long getFieldValue(PlayerStats s, String field) {
        return switch (field.toLowerCase()) {
            case "playtime" -> s.playtimeMinutes;
            case "blocks_broken" -> s.blocksBroken;
            case "blocks_placed" -> s.blocksPlaced;
            case "mobs_killed" -> s.mobsKilled;
            case "deaths" -> s.deaths;
            case "distance" -> s.distanceWalked;
            case "commands" -> s.commandsUsed;
            case "joins" -> s.joinCount;
            default -> 0;
        };
    }

    public static class PlayerStats {
        public String playerName;
        public long playtimeMinutes = 0;
        public long blocksBroken = 0;
        public long blocksPlaced = 0;
        public long mobsKilled = 0;
        public long deaths = 0;
        public long distanceWalked = 0;
        public long commandsUsed = 0;
        public long joinCount = 0;
        public String firstJoin = "";
        public String lastJoin = "";

        public PlayerStats(String name) {
            this.playerName = name;
        }
    }
}