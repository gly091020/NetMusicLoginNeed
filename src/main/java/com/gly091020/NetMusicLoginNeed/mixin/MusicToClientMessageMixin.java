package com.gly091020.NetMusicLoginNeed.mixin;

import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeedUtil;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MusicToClientMessage.class, remap = false)
public class MusicToClientMessageMixin {
    
    @Shadow
    @Final
    @Mutable
    private String url;
    
    @Shadow
    @Final
    @Mutable
    private BlockPos pos;
    
    @Shadow
    @Final
    @Mutable
    private int timeSecond;
    
    @Shadow
    @Final
    @Mutable
    private String songName;
    
    public MusicToClientMessageMixin() {
    }
    
    protected void replaceSongUrl(String oldUrl, String songName) {
        if (oldUrl != null && (oldUrl.contains("music.163.com") || oldUrl.contains("music.126.net"))) {
            NetMusicLoginNeed.LOGGER.info("Intercepting music message - URL: {}, Song: {}", oldUrl, songName);
            
            if (oldUrl.contains("music.163.com")) {
                // Try to extract ID and get VIP URL
                try {
                    long id = NetMusicLoginNeedUtil.pasteIdFromUrl(oldUrl);
                    String vipUrl = NetMusicLoginNeedUtil.pasteVIPUrlById(id);
                    if (vipUrl != null && !vipUrl.isEmpty()) {
                        NetMusicLoginNeed.LOGGER.info("✓ VIP URL obtained in message - REPLACING");
                        NetMusicLoginNeed.LOGGER.info("Using: {}", vipUrl);
                        this.url = vipUrl;
                    }
                } catch (Exception e) {
                    NetMusicLoginNeed.LOGGER.warn("Failed to extract ID from URL: {}", oldUrl);
                }
            }
        }
    }
}

