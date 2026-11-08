package com.samsthenerd.hexgloop.mixins.textpatterns;

import com.mojang.blaze3d.vertex.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tesselator.class)
public interface MixinSetTessBuffer {

    @Accessor("instance")
    @Mutable
    public static void setInstance(Tesselator tes){
        throw new AssertionError();
    }
}
