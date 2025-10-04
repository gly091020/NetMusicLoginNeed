package com.gly091020.NetMusicLoginNeed.config;

import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
        var button = new ButtonEntry(Component.translatable("text.net_music_login_need.login"), button1 ->
                Minecraft.getInstance().setScreen(new LoginScreen(Minecraft.getInstance().screen)));

        button.isEnable(!FMLEnvironment.production);
        button.setTooltip(Component.translatable("text.net_music_login_need.not_login"));

        category.addEntry(button);
        configBuilder.setSavingRunnable(() -> {
            AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).setConfig(NetMusicLoginNeed.config);
            AutoConfig.getConfigHolder(NetMusicLoginNeedConfig.class).save();
        });
        return configBuilder.build();
    }
}
