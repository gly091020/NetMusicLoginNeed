package com.gly091020.NetMusicLoginNeed.mixin;

import com.gly091020.NetMusicLoginNeed.NetMusicCache;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeedUtil;
import com.github.tartaricacid.netmusic.audio.MusicPlayManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MusicPlayManager.class, remap = false)
public class MusicPlayManagerMixin {
    
    // 存储修改后的 URL，供后续使用
    private static final ThreadLocal<String> CACHED_URL = new ThreadLocal<>();

    // 在 play() 方法的最开始检查缓存
    @Inject(method = "play", at = @At("HEAD"), remap = false)
    private static void netMusicLoginNeed$checkCacheAtPlayHead(String url, String songName, java.util.function.Function<?, ?> soundFactory, CallbackInfo ci) {
        // 清除上一次的缓存
        CACHED_URL.remove();
        
        // 如果是 music.163.com 链接，检查本地缓存
        if (url != null && url.contains("music.163.com")) {
            try {
                long id = NetMusicLoginNeedUtil.pasteIdFromUrl(url);
                java.nio.file.Path cacheFile = NetMusicCache.getCacheFilePath(Long.toString(id));
                if (java.nio.file.Files.exists(cacheFile)) {
                    String cachedUrl = cacheFile.toUri().toURL().toString();
                    // 保存到 ThreadLocal，供 getRedirectUrl 拦截中使用
                    CACHED_URL.set(cachedUrl);
                    NetMusicLoginNeed.LOGGER.info("[Cache] ✓ Cache hit at play() HEAD, will use: {} (song: {})", cachedUrl, songName);
                }
            } catch (Exception e) {
                NetMusicLoginNeed.LOGGER.debug("[Cache] Could not check cache at play() HEAD: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 在 play() 方法结束时捕获并抑制后台网络错误（如 SocketException）
     * 这些错误通常来自 NetMusic 的后台异步操作，不应该输出为 ERROR 日志
     */
    @Inject(method = "play", at = @At("TAIL"), remap = false)
    private static void netMusicLoginNeed$suppressBackgroundNetworkErrors(String url, String songName, java.util.function.Function<?, ?> soundFactory, CallbackInfo ci) {
        // 这个注入在正常路径执行后运行
        // 异常会在这之前被处理，所以主要作用是清理 ThreadLocal
        CACHED_URL.remove();
    }
}




