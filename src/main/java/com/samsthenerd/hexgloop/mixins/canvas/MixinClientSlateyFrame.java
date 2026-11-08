package com.samsthenerd.hexgloop.mixins.canvas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.items.HexGloopItems;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemFrameRenderer.class)
public class MixinClientSlateyFrame {
    @Inject(
        method="getFrameModelResourceLoc(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/ModelResourceLocation;",
        at=@At("HEAD"),
        cancellable=true
    )
    public void makeItSlatey(ItemFrame entity, ItemStack stack, CallbackInfoReturnable<ModelResourceLocation> cir){
        if(stack.is(HexGloopItems.SLATE_CANVAS_ITEM.get())){
            cir.setReturnValue(new ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hexgloop", "slate_frame"), "map=true"));
        }
    }
}
