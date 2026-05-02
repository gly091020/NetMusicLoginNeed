package com.gly091020.NetMusicLoginNeed.config;

import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = NetMusicLoginNeed.ModID)
public class NetMusicLoginNeedConfig implements ConfigData {
    public String cookie = "";
}
