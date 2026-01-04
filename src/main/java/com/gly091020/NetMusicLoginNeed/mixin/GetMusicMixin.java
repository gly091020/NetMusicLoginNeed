package com.gly091020.NetMusicLoginNeed.mixin;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.gly091020.NetMusicLoginNeed.NetMusicCache;
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
     * 在 getRedirectUrl 开始时检查缓存，如果存在则直接返回缓存文件
     * 这样可以避免网络请求，优先使用本地缓存
     */
    @Inject(method = "getRedirectUrl", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onGetRedirectUrlHead(String url, Map<String, String> headers, CallbackInfoReturnable<String> cir) {
        // 如果是 music.163.com 链接，先检查缓存
        if (url != null && url.contains("music.163.com")) {
            try {
                long id = NetMusicLoginNeedUtil.pasteIdFromUrl(url);
                java.nio.file.Path cacheFile = NetMusicCache.getCacheFilePath(Long.toString(id));
                if (java.nio.file.Files.exists(cacheFile)) {
                    String cachedUrl = cacheFile.toUri().toURL().toString();
                    NetMusicLoginNeed.LOGGER.info("[Cache] ✓ Cache hit at HEAD (ID: {}), using: {}", id, cachedUrl);
                    cir.setReturnValue(cachedUrl);
                    // 立即返回，不执行后续代码
                    return;
                } else {
                    NetMusicLoginNeed.LOGGER.debug("[Cache] No cache for ID: {}, will fetch from network", id);
                }
            } catch (Exception e) {
                NetMusicLoginNeed.LOGGER.debug("[Cache] Could not check cache at HEAD: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Intercept NetWorker.getRedirectUrl() to replace with VIP URLs
     * This is the actual URL conversion method called by MusicPlayManager.play()
     */
    @Inject(method = "getRedirectUrl", at = @At("RETURN"), remap = false, cancellable = true)
    private static void onGetRedirectUrl(String url, Map<String, String> headers, CallbackInfoReturnable<String> cir) {
        String originalUrl = cir.getReturnValue();
        NetMusicLoginNeed.LOGGER.info("[NetWorkerMixin] getRedirectUrl() called for URL: {}", url);
        NetMusicLoginNeed.LOGGER.info("[NetWorkerMixin] Original result: {}", originalUrl);
        
        try {
            // 如果原始 URL 是 music.163.com 的链接，先检查缓存，然后获取 VIP URL
            if (url != null && url.contains("music.163.com")) {
                long id = -1;
                try {
                    id = NetMusicLoginNeedUtil.pasteIdFromUrl(url);
                    // 再次检查缓存（防止 HEAD 拦截失效的情况）
                    java.nio.file.Path cacheFile = NetMusicCache.getCacheFilePath(Long.toString(id));
                    if (java.nio.file.Files.exists(cacheFile)) {
                        String cachedUrl = cacheFile.toUri().toURL().toString();
                        NetMusicLoginNeed.LOGGER.info("[Cache] ✓ Cache hit at RETURN (re-check), using: {}", cachedUrl);
                        cir.setReturnValue(cachedUrl);
                        return;
                    }
                } catch (Exception e) {
                    NetMusicLoginNeed.LOGGER.debug("[Cache] Could not extract ID or check cache: {}", e.getMessage());
                }
                
                // 没有缓存，获取 VIP URL
                String vipUrl = NetMusicLoginNeedUtil.pasteVIPUrl(url);
                if (vipUrl != null && !vipUrl.isEmpty()) {
                    NetMusicLoginNeed.LOGGER.info("[NetWorkerMixin] ✓ REPLACING with VIP URL");
                    NetMusicLoginNeed.LOGGER.info("  VIP URL: {}", vipUrl);
                    
                    // 异步缓存 VIP URL（用 ID 作为文件名以保证一致性）
                    if (id > 0) {
                        NetMusicCache.warmCacheAsync(vipUrl, Long.toString(id));
                    }
                    
                    cir.setReturnValue(vipUrl);
                } else {
                    NetMusicLoginNeed.LOGGER.warn("[NetWorkerMixin] ✗ Failed to get VIP URL, keeping original");
                }
            }
        } catch (Exception e) {
            // 如果网络请求失败，尝试使用缓存
            if (url != null && url.contains("music.163.com")) {
                try {
                    long id = NetMusicLoginNeedUtil.pasteIdFromUrl(url);
                    java.nio.file.Path cacheFile = NetMusicCache.getCacheFilePath(Long.toString(id));
                    if (java.nio.file.Files.exists(cacheFile)) {
                        String cachedUrl = cacheFile.toUri().toURL().toString();
                        NetMusicLoginNeed.LOGGER.info("[Cache] ✓ Network error fallback, using cache: {}", cachedUrl);
                        cir.setReturnValue(cachedUrl);
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
            NetMusicLoginNeed.LOGGER.error("[NetWorkerMixin] ✗ Exception: {}", e.getMessage(), e);
        }
    }
}




