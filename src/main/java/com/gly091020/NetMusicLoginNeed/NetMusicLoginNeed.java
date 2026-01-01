package com.gly091020.NetMusicLoginNeed;

import com.gly091020.NetMusicLoginNeed.config.NetMusicLoginNeedConfig;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class NetMusicLoginNeed implements ModInitializer {
    public static final String MOD_ID = "net_music_login_need";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static NetMusicLoginNeedConfig config;
    public static final ResourceLocation COOKIE_SYNC = ResourceLocation.fromNamespaceAndPath(MOD_ID, "cookie_sync");
    public static String runtimeCookie = ""; // client-side value synced from server

    @Override
    @SuppressWarnings("all")
    public void onInitialize() {
        AutoConfig.register(NetMusicLoginNeedConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).get();

        // When a player joins, send server-configured cookie down to clients
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var cookie = config != null ? config.cookie : "";
            if (cookie != null && !cookie.isEmpty()) {
                ServerPlayNetworking.send(handler.player, new CookieSyncPayload(cookie));
            }
        });
    }

    /** Custom payload for cookie sync to satisfy new networking API. */
    public record CookieSyncPayload(String cookie) implements CustomPacketPayload {
        public static final Type<CookieSyncPayload> TYPE = new Type<>(COOKIE_SYNC);
        public static final StreamCodec<FriendlyByteBuf, CookieSyncPayload> CODEC =
            StreamCodec.of((FriendlyByteBuf buf, CookieSyncPayload payload) -> payload.write(buf), CookieSyncPayload::new);

        public CookieSyncPayload(FriendlyByteBuf buf) {
            this(buf.readUtf());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private void write(FriendlyByteBuf buf) {
            buf.writeUtf(cookie);
        }
    }
}
