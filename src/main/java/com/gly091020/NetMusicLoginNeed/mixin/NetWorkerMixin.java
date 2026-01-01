package com.gly091020.NetMusicLoginNeed.mixin;

import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Intercept NetWorker.get() calls to log and potentially intercept music URLs
 */
@Mixin(value = com.github.tartaricacid.netmusic.api.NetWorker.class, remap = false)
public class NetWorkerMixin {

    @Inject(
        method = "get",
        at = @At("HEAD"),
        remap = false
    )
    private static void onNetWorkerGet(String url, Map<String, String> headers, CallbackInfoReturnable<String> cir) {
        NetMusicLoginNeed.LOGGER.info("[NetWorker.get] URL: {}", url);
        if (url.contains("music.163.com") && url.contains("song")) {
            NetMusicLoginNeed.LOGGER.info("[NetWorker.get] ✓ Music URL detected!");
        }
    }
}
