package com.gly091020.NetMusicLoginNeed;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetMusicLoginNeedUtil {
    private static final Gson GSON = new Gson();
        private static final String BASE_URL = "https://music.163.com/api/song/enhance/player/url/v1?encodeType=mp3&ids=[%s]&level=%s";
        private static final List<String> LEVELS = Arrays.asList(
            // 只使用 MP3 格式，跳过 FLAC，因为可能不被支持
            "exhigh",   // 320kbps MP3
            "higher",   // 192kbps MP3
            "standard"  // 128kbps MP3
        );

    @SuppressWarnings("all")
    public static String getSongUrl(String json) throws Exception{
        var j = GSON.fromJson(json, TypeToken.get(Object.class));
        return (String)((Map<String, Object>)((List<Object>)((Map<String, Object>)j).get("data")).get(0)).get("url");
    }

    public static long pasteIdFromUrl(String s) throws IllegalAccessException {
        String[] parts = s.split("[?&]id=");  // 为 什 么 要 用 这 种 代 码
        String idPart;
        if (parts.length > 1) {
            idPart = parts[1].split("&")[0];
        } else {
            throw new IllegalAccessException("解析失败");
        }
        return Long.parseLong(idPart.replace(".mp3", ""));
    }

    @SuppressWarnings("all")
    public static String pasteKey(String json) throws Exception{
        var j = GSON.fromJson(json, TypeToken.get(Object.class));
        return (String) ((Map<String, Object>)j).get("unikey");
    }

    @SuppressWarnings("all")
    public static int pasteCode(String json) throws Exception{
        var j = GSON.fromJson(json, TypeToken.get(Object.class));
        return ((Double)((Map<String, Object>)j).get("code")).intValue();
    }

    @Nullable
    public static String pasteVIPUrl(String oldURL){
        String cookie = NetMusicLoginNeed.runtimeCookie;
        if (cookie == null || cookie.isEmpty()) {
            cookie = (NetMusicLoginNeed.config != null) ? NetMusicLoginNeed.config.cookie : "";
        }
        if(cookie == null || cookie.isEmpty()) {
            NetMusicLoginNeed.LOGGER.warn("Cookie not set (client), skip replace for {}", oldURL);
            return null;
        }
        Map<String, String> data = new HashMap<>();
        long id;
        try{
            id = pasteIdFromUrl(oldURL);
        } catch (IllegalAccessException e) {
            NetMusicLoginNeed.LOGGER.warn("Fail parse id from url {}", oldURL);
            return null;
        }
        return pasteVIPUrlById(id);
    }
    
    @Nullable
    public static String pasteVIPUrlById(long id){
        String cookie = NetMusicLoginNeed.runtimeCookie;
        if (cookie == null || cookie.isEmpty()) {
            cookie = (NetMusicLoginNeed.config != null) ? NetMusicLoginNeed.config.cookie : "";
        }
        if(cookie == null || cookie.isEmpty()) {
            NetMusicLoginNeed.LOGGER.warn("Cookie not set (client), skip VIP request for id {}", id);
            return null;
        }
        Map<String, String> data = new HashMap<>();
        data.put("cookie", cookie);
        try {
            for (var level : LEVELS) {
                var json = NetWorker.get(String.format(BASE_URL, id, level), data);
                var code = pasteCode(json);
                var newUrl = getSongUrl(json);
                NetMusicLoginNeed.LOGGER.info("Request id {} level {} code {} url {}", id, level, code, newUrl);
                if(newUrl != null && !newUrl.isEmpty()){
                    return newUrl;
                }
                NetMusicLoginNeed.LOGGER.warn("Level {} empty url for id {} raw {}", level, id, json);
            }
            return null;
        } catch (Exception e) {
            NetMusicLoginNeed.LOGGER.warn("Request or parse failed for id {}: {}", id, e.getMessage());
        }
        return null;
    }
}
