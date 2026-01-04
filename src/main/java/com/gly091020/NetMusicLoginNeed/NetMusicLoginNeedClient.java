package com.gly091020.NetMusicLoginNeed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import com.gly091020.NetMusicLoginNeed.config.NetMusicLoginNeedConfig;

public class NetMusicLoginNeedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register payload type before hooking receiver
        PayloadTypeRegistry.playS2C().register(NetMusicLoginNeed.CookieSyncPayload.TYPE, NetMusicLoginNeed.CookieSyncPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(NetMusicLoginNeed.CookieSyncPayload.TYPE, (payload, context) -> {
            String cookie = payload.cookie();
            context.client().execute(() -> {
                NetMusicLoginNeed.runtimeCookie = cookie;
                NetMusicLoginNeed.LOGGER.info("Received cookie from server");
            });
        });

        // Register /cookie set command (reads from clipboard using GLFW)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> cookieCommand = 
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("cookie")
                    .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("set")
                        .executes(context -> {
                            FabricClientCommandSource source = context.getSource();
                            try {
                                // Use GLFW clipboard API (works in Minecraft environment)
                                String cookieValue = org.lwjgl.glfw.GLFW.glfwGetClipboardString(0);
                                if (cookieValue == null || cookieValue.isEmpty()) {
                                    source.sendFeedback(Component.literal("✗ Clipboard is empty"));
                                    return 0;
                                }
                                
                                NetMusicLoginNeed.runtimeCookie = cookieValue;
                                if (NetMusicLoginNeed.config != null) {
                                    NetMusicLoginNeed.config.cookie = cookieValue;
                                    AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).setConfig(NetMusicLoginNeed.config);
                                    AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).save();
                                }
                                source.sendFeedback(Component.literal("✓ Cookie set from clipboard (" + cookieValue.length() + " chars)"));
                                NetMusicLoginNeed.LOGGER.info("Cookie set from clipboard: length={}", cookieValue.length());
                            } catch (Exception e) {
                                source.sendFeedback(Component.literal("✗ Error: " + e.getMessage()));
                                NetMusicLoginNeed.LOGGER.error("Error reading clipboard", e);
                            }
                            return 1;
                        }))
                    .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("get")
                        .executes(context -> {
                            String cookie = NetMusicLoginNeed.runtimeCookie;
                            if (cookie == null || cookie.isEmpty()) {
                                cookie = (NetMusicLoginNeed.config != null) ? NetMusicLoginNeed.config.cookie : "";
                            }
                            context.getSource().sendFeedback(Component.literal("Current cookie: " + (cookie.isEmpty() ? "[empty]" : "[" + cookie.length() + " chars]")));
                            return 1;
                        }));
            dispatcher.register(cookieCommand);
        });
    }
}