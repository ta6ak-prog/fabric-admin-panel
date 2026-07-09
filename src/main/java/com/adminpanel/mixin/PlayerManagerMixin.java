package com.adminpanel.mixin;

import com.adminpanel.AdminPanelMod;
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
        AdminPanelMod.eventLogger.log("JOIN", name, "Connected from " + connection.getAddress());

        var stats = AdminPanelMod.statsManager.getOrCreate(name);
        stats.joinCount++;
        stats.lastJoin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (stats.firstJoin.isEmpty()) {
            stats.firstJoin = stats.lastJoin;
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
        String name = player.getName().getString();
        AdminPanelMod.eventLogger.log("LEAVE", name, "Disconnected");
        AdminPanelMod.statsManager.save();
    }
}