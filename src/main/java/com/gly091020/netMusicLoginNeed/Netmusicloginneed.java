package com.gly091020.NetMusicLoginNeed;

import com.gly091020.NetMusicLoginNeed.config.ConfigScreenGetter;
import com.gly091020.NetMusicLoginNeed.config.NetMusicLoginNeedConfig;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(NetMusicLoginNeed.ModID)
public class NetMusicLoginNeed {
    public static final String ModID = "net_music_login_need";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static NetMusicLoginNeedConfig config;

    public NetMusicLoginNeed(ModContainer container){
        AutoConfig.register(NetMusicLoginNeedConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).get();
        if(FMLEnvironment.getDist().isClient()){
            container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) ->
                    ConfigScreenGetter.get(parent));
        }
    }
}
