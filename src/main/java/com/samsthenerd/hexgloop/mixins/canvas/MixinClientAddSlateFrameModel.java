package com.samsthenerd.hexgloop.mixins.canvas;

// TODO(1.21 port): DISABLED — ModelBakery.ITEM_FRAME_STATE_FACTORY and the ImmutableMap special-model registration
// in its static initializer are gone. In 1.21 special models are registered through the model loader
// (ModelBakery.SPECIAL_MODEL_LOADERS / NeoForge model events); slate_frame needs re-registering there.

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@Mixin(ModelBakery.class)
public class MixinClientAddSlateFrameModel {

    @Shadow
    @Final
    private static StateDefinition<Block, BlockState> ITEM_FRAME_STATE_FACTORY;

    @ModifyExpressionValue(
        method="<clinit>()V",
        at=@At(value="INVOKE", target="com/google/common/collect/ImmutableMap.of (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap;"),
        remap=false
    )
    private static ImmutableMap<Object, Object> addSlateFrameModel(ImmutableMap<Object, Object> original){
        return ImmutableMap.<Object, Object>builder().putAll(original).put(ResourceLocation.fromNamespaceAndPath("hexgloop", "slate_frame"), ITEM_FRAME_STATE_FACTORY).build();
    }
}
