package com.adminpanel.config;

import com.adminpanel.AdminPanelMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.nio.file.*;

/**
 * Mod configuration — loaded from adminpanel/config.json.
 * Contains Telegram bot token, chat ID, and privilege mappings.
 */
public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Telegram settings
    public String bot_token = "";
    public long chat_id = 0;

    // Privilege detection: if player name contains a tag like [VIP], assign that privilege
    // You can extend this with a permissions mod integration (LuckPerms etc.)
    public String default_privilege = "Игрок";

    private static Path configPath;

    public static ModConfig load(MinecraftServer server) {
        configPath = server.getRunDirectory().toPath().resolve("adminpanel/config.json");
        try {
            Files.createDirectories(configPath.getParent());
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                ModConfig config = GSON.fromJson(json, ModConfig.class);
                if (config != null) return config;
            }
        } catch (Exception e) {
            AdminPanelMod.LOGGER.error("Failed to load config: " + e.getMessage());
        }

        // Create default config
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        if (configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(this));
            AdminPanelMod.LOGGER.info("Config saved to " + configPath);
        } catch (IOException e) {
            AdminPanelMod.LOGGER.error("Failed to save config: " + e.getMessage());
        }
    }
}