package com.adminpanel;

import com.adminpanel.command.AdminCommandRegistrar;
import com.adminpanel.config.ModConfig;
import com.adminpanel.logger.EventLogger;
import com.adminpanel.stats.StatsManager;
import com.adminpanel.telegram.TelegramNotifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminPanelMod implements ModInitializer {

    public static final String MOD_ID = "adminpanel";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static EventLogger eventLogger;
    public static StatsManager statsManager;
    public static TelegramNotifier telegramNotifier;
    public static ModConfig config;

    @Override
    public void onInitialize() {
        LOGGER.info("Admin Panel mod initializing...");

        eventLogger = new EventLogger();
        statsManager = new StatsManager();
        telegramNotifier = new TelegramNotifier();

        AdminCommandRegistrar.init();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            eventLogger.init(server);
            statsManager.init(server);

            config = ModConfig.load(server);
            telegramNotifier.init(config.bot_token, config.chat_id);

            LOGGER.info("Admin Panel initialized on server.");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            statsManager.save();
            LOGGER.info("Admin Panel data saved.");
        });

        LOGGER.info("Admin Panel mod loaded successfully!");
    }

    /**
     * Determines player privilege.
     * For basic usage — checks if display name contains tags like [VIP], [Admin], etc.
     * For advanced usage — integrate with LuckPerms or similar permissions mod.
     */
    public static String getPlayerPrivilege(String displayName) {
        if (displayName.contains("[Admin]") || displayName.contains("[Админ]")) return "Админ";
        if (displayName.contains("[VIP]") || displayName.contains("[Вип]")) return "VIP";
        if (displayName.contains("[Mod]") || displayName.contains("[Модер]")) return "Модератор";
        return config != null ? config.default_privilege : "Игрок";
    }
}