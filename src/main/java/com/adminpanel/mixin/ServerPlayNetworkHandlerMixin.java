package com.adminpanel.mixin;

import com.adminpanel.AdminPanelMod;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "handleChatMessage", at = @At("HEAD"))
    private void onChatMessage(SignedMessage message, CallbackInfo ci) {
        String name = player.getName().getString();
        String content = message.getContent().getString();
        AdminPanelMod.eventLogger.log("CHAT", name, content);
    }
}