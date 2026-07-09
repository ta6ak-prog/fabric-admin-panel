package com.adminpanel;

import com.adminpanel.command.AdminCommand;
import com.adminpanel.command.AdminCommandRegistrar;
import com.adminpanel.logger.EventLogger;
import com.adminpanel.stats.StatsManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminPanelMod implements ModInitializer {

    public static final String MOD_ID = "adminpanel";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static EventLogger eventLogger;
    public static StatsManager statsManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Admin Panel mod initializing...");

        eventLogger = new EventLogger();
        statsManager = new StatsManager();

        AdminCommandRegistrar.init();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            eventLogger.init(server);
            statsManager.init(server);
            LOGGER.info("Admin Panel initialized on server.");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            statsManager.save();
            LOGGER.info("Admin Panel data saved.");
        });

        LOGGER.info("Admin Panel mod loaded successfully!");
    }
}