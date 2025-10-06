package com.gly091020.NetMusicLoginNeed.mixin;

import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockMusicPlayer.class)
public class AlwaysCanPlayVIPMixin {
    @Redirect(method = "m_6227_", at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/netmusic/item/ItemMusicCD;getSongInfo(Lnet/minecraft/world/item/ItemStack;)Lcom/github/tartaricacid/netmusic/item/ItemMusicCD$SongInfo;"), remap = false)
    public ItemMusicCD.SongInfo getInfo(ItemStack infoTag){
        var info = ItemMusicCD.getSongInfo(infoTag);
        if(info != null){
            info.vip = false;
        }
        return info;
    }
}
