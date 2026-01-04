package com.gly091020.NetMusicLoginNeed.mixin;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemMusicCD.class, remap = false)
public class AlwaysCanPlayVIPMixin {
    @Inject(method = "getSongInfo", at = @At("RETURN"), cancellable = true)
    private static void stripVip(ItemStack stack, CallbackInfoReturnable<ItemMusicCD.SongInfo> cir) {
        var info = cir.getReturnValue();
        if (info != null) {
            info.vip = false;
            cir.setReturnValue(info);
        }
    }
}
