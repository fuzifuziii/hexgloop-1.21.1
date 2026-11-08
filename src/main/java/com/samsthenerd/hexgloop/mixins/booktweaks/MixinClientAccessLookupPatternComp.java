package com.samsthenerd.hexgloop.mixins.booktweaks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import at.petrak.hexcasting.interop.patchouli.LookupPatternComponent;
import net.minecraft.resources.ResourceLocation;

@Mixin(LookupPatternComponent.class)
public interface MixinClientAccessLookupPatternComp {
    @Accessor("opName")
    ResourceLocation getOpName();
    
    @Accessor("strokeOrder")
    boolean getStrokeOrder();
}
