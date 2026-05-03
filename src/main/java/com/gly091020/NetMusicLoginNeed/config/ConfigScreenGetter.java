package com.gly091020.NetMusicLoginNeed.config;

import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreenGetter {
    public static Screen get(Screen parent){
        var configBuilder = ConfigBuilder.create();
        configBuilder.setParentScreen(parent);
        configBuilder.setTitle(Component.translatable("config.net_music_login_need.title"));
        var entryBuilder = configBuilder.entryBuilder();
        var category = configBuilder.getOrCreateCategory(Component.empty());
        category.addEntry(entryBuilder.startStrField(Component.literal("Cookie"), NetMusicLoginNeed.config.cookie)
                        .setDefaultValue("")
                        .setSaveConsumer(s -> NetMusicLoginNeed.config.cookie = s)
                .build());
        configBuilder.setSavingRunnable(() -> {
            AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).setConfig(NetMusicLoginNeed.config);
            AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).save();
        });
        return configBuilder.build();
    }
}
