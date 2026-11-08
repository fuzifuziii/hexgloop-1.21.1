package com.samsthenerd.hexgloop.mixins.mirroritems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.samsthenerd.hexgloop.casting.mirror.IShallowMirrorBinder;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;
import com.samsthenerd.hexgloop.utils.ClientUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandLayer.class)
public class MixinClientTPHandRender {
    @WrapOperation(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
    at=@At(value="INVOKE", target="net/minecraft/client/renderer/entity/layers/ItemInHandLayer.renderArmWithItem (Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    public void wrapThirdPersonHand(ItemInHandLayer thisHIFR, LivingEntity entity, ItemStack stack, 
        ItemDisplayContext transformationMode, HumanoidArm arm, PoseStack matrices, MultiBufferSource vertexConsumers, int light, Operation<Void> original){
            // most of this will just get passed through, mostly care about the item and *maybe* vertexconsumer? 
            // although might be better to handle that later so we don't accidentally make an arm green or something
            
            InteractionHand hand = arm == HumanoidArm.LEFT ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            Tuple<ItemStack, Boolean> altStackData = SyncedItemHandling.getAlternateStackClient(entity, hand);
            ItemStack itemToRender = stack;
            if(altStackData != null && ClientUtils.shouldShowReflected()){
                itemToRender = altStackData.getA();
                if(altStackData.getB()){ // is from mirror
                    com.samsthenerd.hexgloop.utils.StackNBT.putDouble(itemToRender, SyncedItemHandling.IS_REFLECTED_TAG, 1);
                } else { // from pedestal
                    com.samsthenerd.hexgloop.utils.StackNBT.putDouble(itemToRender, SyncedItemHandling.IS_REFLECTED_TAG, 2);
                }
            }
            original.call(thisHIFR, entity, itemToRender, transformationMode, arm, matrices, vertexConsumers, light);
    }

    // make sure it renders even when there's nothing in the hand
    @WrapOperation(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
    at=@At(value="INVOKE", target="net/minecraft/world/item/ItemStack.isEmpty ()Z"))
    public boolean plzRenderThirdPerson(ItemStack stack, Operation<Boolean> original, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity){
        if(((Object)livingEntity) instanceof IShallowMirrorBinder){
            IShallowMirrorBinder binder = (IShallowMirrorBinder)livingEntity;
            if(!binder.getTrackedStack().isEmpty() && ClientUtils.shouldShowReflected()){
                return false;
            }
        }
        return original.call(stack);
    }

}
