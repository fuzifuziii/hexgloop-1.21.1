package com.samsthenerd.hexgloop.mixins.mirroritems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;
import com.samsthenerd.hexgloop.renderers.GaslightingVCProvider;
import com.samsthenerd.hexgloop.renderers.MiddleVertexConsumer;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemRenderer.class)
public class MixinClientMakeHandFuzzy {
    private VertexConsumer makeNewConsumer(VertexConsumer oldConsumer, boolean isHandMirror){
        return new MiddleVertexConsumer(oldConsumer){
                @Override
                // 1.21: VertexConsumer.color(int,int,int,int) -> setColor(int,int,int,int)
                public VertexConsumer setColor(int red, int green, int blue, int alpha){
                    int rgb = isHandMirror ? 0xA0F2CD : 0x378A6D; // darker if it's from a pedestal
                    return super.setColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, isHandMirror ? 192 : 64);
                }
            };
    }

    @WrapOperation(method="render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
    at=@At(value="INVOKE", target="net/minecraft/client/renderer/entity/ItemRenderer.renderModelLists (Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    public void gaslightBakedModelRenderer(ItemRenderer thisIR, BakedModel model, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer vertices, Operation<Void> original){
        CompoundTag nbt = StackNBT.get(stack);
        VertexConsumer newVertices = vertices;
        if(nbt != null && nbt.contains(SyncedItemHandling.IS_REFLECTED_TAG, Tag.TAG_DOUBLE)){
            double isReflected = nbt.getDouble(SyncedItemHandling.IS_REFLECTED_TAG);
            if(isReflected == 1){
                // from hand mirror
                newVertices = makeNewConsumer(vertices, true);
            } else if(isReflected == 2){
                // from pedestal
                newVertices = makeNewConsumer(vertices, false);
            }
        }
        original.call(thisIR, model, stack, light, overlay, matrices, newVertices);
    }

    @WrapOperation(method="render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
    at=@At(value="INVOKE", target="net/minecraft/client/renderer/BlockEntityWithoutLevelRenderer.renderByItem (Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    public void gaslightBuiltInRenderer(BlockEntityWithoutLevelRenderer modelItemRenderer, ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Operation<Void> original){
        CompoundTag nbt = StackNBT.get(stack);
        MultiBufferSource newProvider = vertexConsumers;
        if(nbt != null && nbt.contains(SyncedItemHandling.IS_REFLECTED_TAG, Tag.TAG_DOUBLE)){
            double isReflected = nbt.getDouble(SyncedItemHandling.IS_REFLECTED_TAG);
            if(isReflected == 1){
                // from hand mirror
                newProvider = new GaslightingVCProvider(vertexConsumers, (vc) -> makeNewConsumer(vc, true));
            } else if(isReflected == 2){
                // from pedestal
                newProvider = new GaslightingVCProvider(vertexConsumers, (vc) -> makeNewConsumer(vc, false));
            }
        }
        original.call(modelItemRenderer, stack, mode, matrices, newProvider, light, overlay);
    }
}
