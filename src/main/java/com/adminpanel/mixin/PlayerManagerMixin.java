package com.adminpanel.mixin;

import com.adminpanel.AdminPanelMod;
import com.adminpanel.stats.StatsManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        String name = player.getName().getString();
        String displayName = player.getDisplayName() != null ? player.getDisplayName().getString() : name;
        String privilege = AdminPanelMod.getPlayerPrivilege(displayName);

        // Log event
        AdminPanelMod.eventLogger.log("JOIN", name, "Connected from " + connection.getAddress());

        // Update stats
        StatsManager.PlayerStats stats = AdminPanelMod.statsManager.getOrCreate(name);
        stats.joinCount++;
        stats.lastJoin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (stats.firstJoin.isEmpty()) {
            stats.firstJoin = stats.lastJoin;
        }

        // Send Telegram notification — show PREVIOUS session stats
        AdminPanelMod.telegramNotifier.onPlayerJoin(name, privilege, stats);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
        String name = player.getName().getString();
        String displayName = player.getDisplayName() != null ? player.getDisplayName().getString() : name;
        String privilege = AdminPanelMod.getPlayerPrivilege(displayName);

        // Log event
        AdminPanelMod.eventLogger.log("LEAVE", name, "Disconnected");

        // Get current stats for this session
        StatsManager.PlayerStats stats = AdminPanelMod.statsManager.getOrCreate(name);

        // Send Telegram notification — show THIS session stats
        AdminPanelMod.telegramNotifier.onPlayerLeave(name, privilege, stats);

        // Save and reset session counters
        AdminPanelMod.statsManager.save();
    }
}