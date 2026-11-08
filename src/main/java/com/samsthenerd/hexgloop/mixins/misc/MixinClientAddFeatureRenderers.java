package com.samsthenerd.hexgloop.mixins.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.samsthenerd.hexgloop.renderers.HandThingFeatureRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(PlayerRenderer.class)
public abstract class MixinClientAddFeatureRenderers extends LivingEntityRenderer {

    private MixinClientAddFeatureRenderers() {
		super(null, null, 0);
	}

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V",
        at=@At("TAIL"))
    public void addPlayerRenderers(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci){
        PlayerRenderer perThis = (PlayerRenderer)(Object)this;
        this.addLayer(new HandThingFeatureRenderer(((RenderLayerParent)(Object)this), ctx.getModelSet(), slim));
    }

    @Inject(method="renderLeftHand", at=@At("TAIL"))
    private void renderLeftArmFeature(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo ci){
        MixinClientExposeAddFeature perThis = (MixinClientExposeAddFeature)(Object)this;
        for(RenderLayer fr : perThis.getFeatures()){
            if(fr instanceof HandThingFeatureRenderer handRenderer){
                handRenderer.renderFirstPersonArm(matrices, vertexConsumers, light, player, HumanoidArm.LEFT);
            }
        }
    }

    @Inject(method="renderRightHand", at=@At("TAIL"))
    private void renderRightArmFeature(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo ci){
        MixinClientExposeAddFeature perThis = (MixinClientExposeAddFeature)(Object)this;
        for(RenderLayer fr : perThis.getFeatures()){
            if(fr instanceof HandThingFeatureRenderer handRenderer){
                handRenderer.renderFirstPersonArm(matrices, vertexConsumers, light, player, HumanoidArm.RIGHT);
            }
        }
    }
}
