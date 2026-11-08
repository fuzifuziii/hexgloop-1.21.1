package com.samsthenerd.hexgloop.mixins.mirroritems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;
import com.samsthenerd.hexgloop.utils.ClientUtils;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class MixinClientFPHandRender {
    @WrapOperation(method="renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V",
    at=@At(value="INVOKE", target="net/minecraft/client/renderer/ItemInHandRenderer.renderArmWithItem (Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    public void wrapFirstPersonHand(ItemInHandRenderer thisHIR, AbstractClientPlayer player, float tickDelta, 
        float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, Operation<Void> original){
            // most of this will just get passed through, mostly care about the item and *maybe* vertexconsumer? 
            // although might be better to handle that later so we don't accidentally make an arm green or something
            Tuple<ItemStack, Boolean> altStackData = SyncedItemHandling.getAlternateStackClient(player, hand);
            ItemStack itemToRender = item;
            if(altStackData != null && ClientUtils.shouldShowReflected()){
                itemToRender = altStackData.getA();
                if(altStackData.getB()){ // is from mirror
                    com.samsthenerd.hexgloop.utils.StackNBT.putDouble(itemToRender, SyncedItemHandling.IS_REFLECTED_TAG, 1);
                } else { // from pedestal
                    com.samsthenerd.hexgloop.utils.StackNBT.putDouble(itemToRender, SyncedItemHandling.IS_REFLECTED_TAG, 2);
                }
            }
            original.call(thisHIR, player, tickDelta, pitch, hand, swingProgress, itemToRender, equipProgress, matrices, vertexConsumers, light);
        }
}
