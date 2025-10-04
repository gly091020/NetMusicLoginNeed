package com.gly091020.NetMusicLoginNeed.mixin;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeedUtil;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;
import java.util.function.Function;

@Mixin(MusicPlayManager.class)
public abstract class GetMusicMixin {
    @Shadow(remap = false)
    private static void playMusic(String url, String songName, Function<URL, SoundInstance> sound) {
    }

    @Inject(method = "play", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void onPlay(String url, String songName, Function<URL, SoundInstance> sound, CallbackInfo ci){
        var newURL = NetMusicLoginNeedUtil.pasteVIPUrl(url);
        if(newURL != null){
            playMusic(newURL, songName, sound);
            ci.cancel();
        }
    }
}
