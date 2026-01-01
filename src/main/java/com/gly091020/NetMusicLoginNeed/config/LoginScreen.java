package com.gly091020.NetMusicLoginNeed.config;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeed;
import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class LoginScreen extends Screen {
    private final Screen parent;
    private int tickCount = 0;
    private String uniKey;
    protected LoginScreen(Screen parent) {
        super(Component.literal("登录"));
        this.parent = parent;
        getKey();
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;
        if(tickCount % 20 * 5 == 0){
            isEnd();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int p_281550_, int p_282878_, float p_282465_) {
        renderBackground(graphics, p_281550_, p_282878_, p_282465_);
        super.render(graphics, p_281550_, p_282878_, p_282465_);
        graphics.drawCenteredString(Minecraft.getInstance().font, String.format("http://music.163.com/api/login/qrcode/client/login?type=1&key=%s", uniKey), width / 2, height / 2, 0XFFFFFFFF);
    }

    private void isEnd(){
        if(uniKey == null)return;
        try {
            var json = NetWorker.get("http://music.163.com/api/login/qrcode/client/login?type=1&key=" + uniKey, new HashMap<>());
            switch (NetMusicLoginNeedUtil.pasteCode(json)){
                case 801:
                case 802:
                    return;
                case 800:
                    fail(new RuntimeException("超时"));
                case 803:
                    NetMusicLoginNeed.LOGGER.info("ok");
                    onClose();
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private void getKey(){
        try {
            var json = NetWorker.get("https://music.163.com/api/login/qrcode/unikey?type=1", new HashMap<>());
            uniKey = NetMusicLoginNeedUtil.pasteKey(json);
            NetMusicLoginNeed.LOGGER.info("http://music.163.com/login?codekey={}", uniKey);
        } catch (Exception e) {
            fail(e);
        }
    }

    private void fail(@Nullable Exception e){
        if(e != null){
            NetMusicLoginNeed.LOGGER.error("登录失败：", e);
        }
        onClose();
    }
}
