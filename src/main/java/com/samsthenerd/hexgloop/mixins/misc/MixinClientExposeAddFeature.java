package com.samsthenerd.hexgloop.mixins.misc;

import java.util.List;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface MixinClientExposeAddFeature {
    @Invoker("addLayer")
    boolean invokeAddFeature(RenderLayer feature);

    @Accessor("layers")
    List<RenderLayer> getFeatures();
}
