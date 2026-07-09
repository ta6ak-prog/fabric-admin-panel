package com.adminpanel.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class AdminCommandRegistrar {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AdminCommand.registerCommands(dispatcher);
        });
    }
}