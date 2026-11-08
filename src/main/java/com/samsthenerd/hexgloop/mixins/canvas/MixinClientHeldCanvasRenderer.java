package com.samsthenerd.hexgloop.mixins.canvas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import at.petrak.hexcasting.client.gui.PatternTooltipComponent;

@Mixin(ItemInHandRenderer.class)
public class MixinClientHeldCanvasRenderer {
    // 1.21: renderArmWithItem() checks `instanceof MapItem` instead of ItemStack.is(FILLED_MAP),
    // and ItemSlateCanvas already extends MapItem, so no hook is needed here anymore.

    @WrapOperation(
        method="renderMap(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ItemStack;)V",
        at=@At(value="INVOKE", target="net/minecraft/client/renderer/MultiBufferSource.getBuffer (Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    public VertexConsumer makeItSlatey(MultiBufferSource provider, RenderType originalLayer, Operation<VertexConsumer> original, PoseStack matrices, MultiBufferSource vertexConsumers, int swingProgress, ItemStack stack){
        if(stack.is(HexGloopItems.SLATE_CANVAS_ITEM.get())){
            return original.call(provider, RenderType.text(PatternTooltipComponent.SLATE_BG));
        }
        return original.call(provider, originalLayer);
    }
}
