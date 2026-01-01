package com.gly091020.NetMusicLoginNeed;

import com.gly091020.NetMusicLoginNeed.config.NetMusicLoginNeedConfig;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class NetMusicLoginNeed implements ModInitializer {
    public static final String MOD_ID = "net_music_login_need";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static NetMusicLoginNeedConfig config;

    @Override
    @SuppressWarnings("all")
    public void onInitialize() {
        AutoConfig.register(NetMusicLoginNeedConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).get();
    }
}
