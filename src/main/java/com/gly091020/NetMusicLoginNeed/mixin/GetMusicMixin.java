package com.gly091020.NetMusicLoginNeed.mixin;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeedUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Intercept NetWorker.getRedirectUrl() to replace with VIP URLs
 * This is the actual URL conversion method called by MusicPlayManager.play()
 */
@Mixin(value = NetWorker.class, remap = false)
public abstract class GetMusicMixin {
    
    /**
     * Replace the URL returned by getRedirectUrl() with VIP URL
     * This method is called by NetMusic to follow music.163.com redirects
     */
    @Inject(method = "getRedirectUrl", at = @At("RETURN"), remap = false, cancellable = true)
    private static void onGetRedirectUrl(String url, Map<String, String> headers, CallbackInfoReturnable<String> cir) {
        String originalUrl = cir.getReturnValue();
        NetMusicLoginNeed.LOGGER.info("[NetWorkerMixin] getRedirectUrl() called for URL: {}", url);
        NetMusicLoginNeed.LOGGER.info("[NetWorkerMixin] Original result: {}", originalUrl);
        
        try {
            // 如果原始 URL 是 music.163.com 的链接，获取 VIP URL
            if (url != null && url.contains("music.163.com")) {
                String vipUrl = NetMusicLoginNeedUtil.pasteVIPUrl(url);
                if (vipUrl != null && !vipUrl.isEmpty()) {
                    NetMusicLoginNeed.LOGGER.info("[NetWorkerMixin] ✓ REPLACING with VIP URL");
                    NetMusicLoginNeed.LOGGER.info("  VIP URL: {}", vipUrl);
                    cir.setReturnValue(vipUrl);
                } else {
                    NetMusicLoginNeed.LOGGER.warn("[NetWorkerMixin] ✗ Failed to get VIP URL, keeping original");
                }
            }
        } catch (Exception e) {
            NetMusicLoginNeed.LOGGER.error("[NetWorkerMixin] ✗ Exception: {}", e.getMessage(), e);
        }
    }
}




