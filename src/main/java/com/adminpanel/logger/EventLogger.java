package com.adminpanel.logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EventLogger {

    private static final int MAX_MEMORY_EVENTS = 500;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Deque<LogEntry> recentEvents = new ConcurrentLinkedDeque<>();
    private Path logDir;

    public void init(MinecraftServer server) {
        logDir = server.getRunDirectory().toPath().resolve("adminpanel/logs");
        try {
            Files.createDirectories(logDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String type, String player, String details) {
        LogEntry entry = new LogEntry(
            LocalDateTime.now().format(TIME_FMT),
            type,
            player,
            details
        );

        recentEvents.addLast(entry);
        while (recentEvents.size() > MAX_MEMORY_EVENTS) {
            recentEvents.pollFirst();
        }

        saveToFile(entry);
    }

    public List<LogEntry> getRecent(int count) {
        List<LogEntry> result = new ArrayList<>(recentEvents);
        int start = Math.max(0, result.size() - count);
        return result.subList(start, result.size());
    }

    public List<LogEntry> getByPlayer(String playerName) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : recentEvents) {
            if (entry.player().equalsIgnoreCase(playerName)) {
                result.add(entry);
            }
        }
        return result;
    }

    public List<LogEntry> getByType(String type) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : recentEvents) {
            if (entry.type().equalsIgnoreCase(type)) {
                result.add(entry);
            }
        }
        return result;
    }

    private void saveToFile(LogEntry entry) {
        if (logDir == null) return;
        String filename = LocalDateTime.now().format(DATE_FMT) + ".json";
        Path file = logDir.resolve(filename);

        try {
            List<LogEntry> entries = new ArrayList<>();
            if (Files.exists(file)) {
                String json = Files.readString(file);
                LogEntry[] arr = GSON.fromJson(json, LogEntry[].class);
                if (arr != null) entries.addAll(Arrays.asList(arr));
            }
            entries.add(entry);
            Files.writeString(file, GSON.toJson(entries));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public record LogEntry(String time, String type, String player, String details) {}
}