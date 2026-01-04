package com.gly091020.NetMusicLoginNeed.mixin;

import com.github.tartaricacid.netmusic.api.WebApi;
import com.gly091020.NetMusicLoginNeed.NetMusicCache;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to cache WebApi responses (lyrics, song info, comments, etc.)
 * 缓存 WebApi 的响应数据（歌词、歌曲信息、评论等）
 */
@Mixin(value = WebApi.class, remap = false)
public class WebApiMixin {
    
    /**
     * 缓存 song() 方法的响应（获取歌曲详细信息）
     */
    @Inject(method = "song", at = @At("RETURN"), remap = false, cancellable = true)
    private void onSongReturn(long songId, CallbackInfoReturnable<String> cir) {
        String result = cir.getReturnValue();
        if (result != null && !result.isBlank()) {
            NetMusicCache.cacheMetadata("song", songId, result);
            NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] Caching song metadata for ID: {}", songId);
        }
    }
    
    /**
     * 检查缓存的 song() 响应，如果存在则返回缓存
     */
    @Inject(method = "song", at = @At("HEAD"), remap = false, cancellable = true)
    private void onSongHead(long songId, CallbackInfoReturnable<String> cir) {
        String cached = NetMusicCache.getCachedMetadata("song", songId);
        if (cached != null) {
            NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] ✓ Returning cached song metadata for ID: {}", songId);
            cir.setReturnValue(cached);
        }
    }
    
    /**
     * 缓存 lyric() 方法的响应（获取歌词）
     */
    @Inject(method = "lyric", at = @At("RETURN"), remap = false, cancellable = true)
    private void onLyricReturn(long songId, CallbackInfoReturnable<String> cir) {
        String result = cir.getReturnValue();
        if (result != null && !result.isBlank()) {
            NetMusicCache.cacheMetadata("lyric", songId, result);
            NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] Caching lyrics for ID: {}", songId);
        }
    }
    
    /**
     * 检查缓存的 lyric() 响应，如果存在则返回缓存
     */
    @Inject(method = "lyric", at = @At("HEAD"), remap = false, cancellable = true)
    private void onLyricHead(long songId, CallbackInfoReturnable<String> cir) {
        String cached = NetMusicCache.getCachedMetadata("lyric", songId);
        if (cached != null) {
            NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] ✓ Returning cached lyrics for ID: {}", songId);
            cir.setReturnValue(cached);
        }
    }
    
    /**
     * 缓存 songComments() 方法的响应（获取评论）
     */
    @Inject(method = "songComments", at = @At("RETURN"), remap = false, cancellable = true)
    private void onSongCommentsReturn(long songId, long offset, long limit, CallbackInfoReturnable<String> cir) {
        String result = cir.getReturnValue();
        if (result != null && !result.isBlank()) {
            // 仅缓存第一页评论（offset=0）
            if (offset == 0) {
                NetMusicCache.cacheMetadata("comments", songId, result);
                NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] Caching song comments for ID: {}", songId);
            }
        }
    }
    
    /**
     * 检查缓存的 songComments() 响应，如果存在则返回缓存
     */
    @Inject(method = "songComments", at = @At("HEAD"), remap = false, cancellable = true)
    private void onSongCommentsHead(long songId, long offset, long limit, CallbackInfoReturnable<String> cir) {
        // 仅对第一页评论使用缓存
        if (offset == 0) {
            String cached = NetMusicCache.getCachedMetadata("comments", songId);
            if (cached != null) {
                NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] ✓ Returning cached song comments for ID: {}", songId);
                cir.setReturnValue(cached);
            }
        }
    }
    
    /**
     * 缓存 album() 方法的响应（获取专辑信息）
     */
    @Inject(method = "album", at = @At("RETURN"), remap = false, cancellable = true)
    private void onAlbumReturn(long albumId, CallbackInfoReturnable<String> cir) {
        String result = cir.getReturnValue();
        if (result != null && !result.isBlank()) {
            NetMusicCache.cacheMetadata("album", albumId, result);
            NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] Caching album metadata for ID: {}", albumId);
        }
    }
    
    /**
     * 检查缓存的 album() 响应，如果存在则返回缓存
     */
    @Inject(method = "album", at = @At("HEAD"), remap = false, cancellable = true)
    private void onAlbumHead(long albumId, CallbackInfoReturnable<String> cir) {
        String cached = NetMusicCache.getCachedMetadata("album", albumId);
        if (cached != null) {
            NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] ✓ Returning cached album metadata for ID: {}", albumId);
            cir.setReturnValue(cached);
        }
    }
    
    /**
     * 缓存 mp3() 方法的响应（获取MP3链接和播放信息）
     */
    @Inject(method = "mp3", at = @At("RETURN"), remap = false, cancellable = true)
    private void onMp3Return(long songId, long[] bitrates, CallbackInfoReturnable<String> cir) {
        String result = cir.getReturnValue();
        if (result != null && !result.isBlank()) {
            NetMusicCache.cacheMetadata("mp3info", songId, result);
            NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] Caching MP3 info for ID: {}", songId);
        }
    }
    
    /**
     * 检查缓存的 mp3() 响应，如果存在则返回缓存
     */
    @Inject(method = "mp3", at = @At("HEAD"), remap = false, cancellable = true)
    private void onMp3Head(long songId, long[] bitrates, CallbackInfoReturnable<String> cir) {
        String cached = NetMusicCache.getCachedMetadata("mp3info", songId);
        if (cached != null) {
            NetMusicLoginNeed.LOGGER.debug("[WebApi Cache] ✓ Returning cached MP3 info for ID: {}", songId);
            cir.setReturnValue(cached);
        }
    }
}
