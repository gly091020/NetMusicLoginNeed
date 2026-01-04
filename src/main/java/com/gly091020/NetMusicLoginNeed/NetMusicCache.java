package com.gly091020.NetMusicLoginNeed;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.loader.api.FabricLoader;

public final class NetMusicCache {
    private static final Set<String> DOWNLOADING = ConcurrentHashMap.newKeySet();
    private static final HexFormat HEX = HexFormat.of();
    private static final String MUSIC_EXT = ".mp3";
    private static final String METADATA_EXT = ".json";

    private NetMusicCache() {
    }

    // ======================== 音乐文件缓存 ========================
    
    public static String maybeGetCachedFileUrl(String url, String songName) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            Path file = resolveCacheFile(url, songName);
            if (Files.exists(file)) {
                NetMusicLoginNeed.LOGGER.info("Cache hit for {} -> {}", songName, file);
                return file.toUri().toURL().toString();
            }
        } catch (Exception e) {
            NetMusicLoginNeed.LOGGER.warn("Cache lookup failed for {}: {}", url, e.getMessage());
        }
        return null;
    }

    public static void warmCacheAsync(String url, String songName) {
        if (!isHttp(url)) {
            return;
        }
        Path file;
        try {
            file = resolveCacheFile(url, songName);
        } catch (Exception e) {
            NetMusicLoginNeed.LOGGER.warn("Cache path resolve failed for {}: {}", url, e.getMessage());
            return;
        }
        String key = file.toString();
        if (Files.exists(file) || !DOWNLOADING.add(key)) {
            NetMusicLoginNeed.LOGGER.debug("Cache skip (exists or downloading) {}", file);
            return;
        }
        NetMusicLoginNeed.LOGGER.info("Cache warm start {} -> {}", url, file);
        CompletableFuture.runAsync(() -> download(url, file))
                .whenComplete((unused, throwable) -> DOWNLOADING.remove(key));
    }

    // ======================== 元数据缓存（歌词、歌曲信息等） ========================
    
    /**
     * 获取缓存的元数据（歌词、歌曲信息等JSON数据）
     */
    public static String getCachedMetadata(String type, long songId) {
        try {
            Path metadataFile = getMetadataFilePath(type, songId);
            if (Files.exists(metadataFile)) {
                String content = Files.readString(metadataFile, StandardCharsets.UTF_8);
                NetMusicLoginNeed.LOGGER.debug("[Metadata Cache] ✓ Hit for {}.{}: {}", type, songId, 
                    content.length() > 100 ? content.substring(0, 100) + "..." : content);
                return content;
            }
        } catch (Exception e) {
            NetMusicLoginNeed.LOGGER.debug("[Metadata Cache] Lookup failed for {}.{}: {}", type, songId, e.getMessage());
        }
        return null;
    }
    
    /**
     * 缓存元数据（歌词、歌曲信息等JSON数据）
     */
    public static void cacheMetadata(String type, long songId, String data) {
        if (data == null || data.isBlank()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                Path metadataFile = getMetadataFilePath(type, songId);
                Files.createDirectories(metadataFile.getParent());
                Files.writeString(metadataFile, data, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                NetMusicLoginNeed.LOGGER.debug("[Metadata Cache] ✓ Cached {}.{}", type, songId);
            } catch (Exception e) {
                NetMusicLoginNeed.LOGGER.warn("[Metadata Cache] Failed to cache {}.{}: {}", type, songId, e.getMessage());
            }
        });
    }
    
    /**
     * 获取元数据文件路径
     * 格式: .minecraft/netmusic-metadata/{type}/{songId}.json
     */
    private static Path getMetadataFilePath(String type, long songId) {
        Path metadataDir = FabricLoader.getInstance().getGameDir().resolve("netmusic-metadata").resolve(type);
        return metadataDir.resolve(songId + METADATA_EXT);
    }

    private static void download(String url, Path target) {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(target.getParent());
            URI uri = URI.create(url);
            URLConnection connection = uri.toURL().openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(15000);
            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                in.transferTo(out);
            }
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            NetMusicLoginNeed.LOGGER.info("Cached song to {}", target);
        } catch (Exception e) {
            NetMusicLoginNeed.LOGGER.warn("Cache download failed for {}: {}", url, e.toString());
            try {
                Files.deleteIfExists(tmp);
            } catch (Exception ignored) {
            }
        }
    }

    public static Path getCacheFilePath(String songId) {
        Path cacheDir = FabricLoader.getInstance().getGameDir().resolve("netmusic-cache");
        return cacheDir.resolve(songId + MUSIC_EXT);
    }

    private static Path resolveCacheFile(String url, String songName) throws Exception {
        String baseName = tryExtractId(url);
        if (baseName == null || baseName.isBlank()) {
            // 若无法提取 ID，才用歌曲名或 URL 哈希
            baseName = sanitizeName(songName);
            if (baseName == null || baseName.isBlank()) {
                baseName = hexDigest(url);
            }
        }
        Path cacheDir = FabricLoader.getInstance().getGameDir().resolve("netmusic-cache");
        return cacheDir.resolve(baseName + MUSIC_EXT);
    }

    private static boolean isHttp(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private static String tryExtractId(String url) {
        try {
            long id = NetMusicLoginNeedUtil.pasteIdFromUrl(url);
            return Long.toString(id);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String cleaned = name.replaceAll("[^a-zA-Z0-9\\-_.]+", "_");
        if (cleaned.length() > 64) {
            cleaned = cleaned.substring(0, 64);
        }
        return cleaned;
    }

    private static String hexDigest(String url) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] bytes = digest.digest(url.getBytes(StandardCharsets.UTF_8));
        return HEX.formatHex(bytes);
    }
}

