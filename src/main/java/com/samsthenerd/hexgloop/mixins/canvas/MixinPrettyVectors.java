package com.samsthenerd.hexgloop.mixins.canvas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;

@Mixin(Vec3Iota.class)
public class MixinPrettyVectors {
    @ModifyReturnValue(
        method="display(DDD)Lnet/minecraft/network/chat/Component;",
        at=@At("RETURN")
    )
    private static Component makeColorful(Component original, double x, double y, double z){
        // check that it's 
        if(x >= 0 && x <= 255 && y >= 0 && y <= 255 && z >= 0 && z <= 255){
            int color = ((int) x) << 16 | ((int) y) << 8 | ((int) z);
            Style style = original.getStyle().withColor(color);
            MutableComponent colorMarker = Component.literal("█").setStyle(style);
            return original.copy().append(colorMarker);
        }
        return original;
    }
}
