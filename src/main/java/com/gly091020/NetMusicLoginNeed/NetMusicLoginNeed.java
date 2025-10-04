package com.gly091020.NetMusicLoginNeed;

import com.gly091020.NetMusicLoginNeed.config.ConfigScreenGetter;
import com.gly091020.NetMusicLoginNeed.config.NetMusicLoginNeedConfig;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(NetMusicLoginNeed.ModID)
public class NetMusicLoginNeed {
    public static final String ModID = "net_music_login_need";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static NetMusicLoginNeedConfig config;

    @SuppressWarnings("all")
    public NetMusicLoginNeed(){
        AutoConfig.register(NetMusicLoginNeedConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).get();
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, screen) ->
                                ConfigScreenGetter.get(screen)
                )
        );
    }
}
