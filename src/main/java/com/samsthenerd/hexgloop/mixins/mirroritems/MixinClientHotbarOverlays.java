package com.samsthenerd.hexgloop.mixins.mirroritems;

import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.samsthenerd.hexgloop.casting.mirror.IShallowMirrorBinder;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;
import com.samsthenerd.hexgloop.items.ItemHandMirror;
import com.samsthenerd.hexgloop.renderers.HUDOverlay;
import com.samsthenerd.hexgloop.utils.ClientUtils;
import com.samsthenerd.hexgloop.utils.StackNBT;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// MC 1.21: Gui.renderSlot/renderItemHotbar take GuiGraphics + DeltaTracker instead of PoseStack/float,
// item rendering moved from ItemRenderer to GuiGraphics, the Gui client field is `minecraft`,
// RenderSystem.getModelViewStack() returns Matrix4fStack, item NBT lives in StackNBT (CUSTOM_DATA)
@Mixin(Gui.class)
public class MixinClientHotbarOverlays {

    // seems like the seed that gets passed to renderSlot is 1-9 for hotbar slots and 10 for the offhand ?
    private boolean isOffhand(int slot){
        return slot == 10;
    }

    private boolean isSelected(int slot, Player player){
        return player.getInventory().selected == slot-1;
    }

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method="renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
    at=@At("RETURN"))
    private void renderHotbarOverlay(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack stack, int seed,
    CallbackInfo ci){
        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
        // diagonstics:
        if(stack.getItem() instanceof ItemHandMirror mirrorItem){
            if(!mirrorItem.isMirrorActivated(stack) || !ClientUtils.shouldShowReflected()) return;
            Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.pushMatrix();
            matrixStack.translate(-3.0F, -3.0F, 0.0F);
            RenderSystem.applyModelViewMatrix();
            if(isSelected(seed, player)){ // selected
                HUDOverlay.SELECTED_HAND_MIRROR_INDICATOR.render(x, y, 22, 22, tickDelta);
            } else { // not selected
                HUDOverlay.HAND_MIRROR_INDICATOR.render(x, y, 22, 22, tickDelta);
            }
            matrixStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        } else if(stack.isEmpty() // only render if theres nothing in the hand
            && (isOffhand(seed) // render in offhand fine
            || (isSelected(seed, player) && !player.getOffhandItem().isEmpty())) // render in mainhand if offhand has an item
            && ((Object)player) instanceof IShallowMirrorBinder shallowBinder) // safe cast to get the item
        {
            ItemStack trackedStack = shallowBinder.getTrackedStack();
            if(trackedStack == null || trackedStack.isEmpty()) return; // don't render nothing
            Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.pushMatrix();
            matrixStack.translate(-3.0F, -3.0F, 0.0F);
            RenderSystem.applyModelViewMatrix();
            if(isOffhand(seed)){
                HUDOverlay.BOUND_MIRROR_INDICATOR.render(x, y, 22, 22, tickDelta);
            } else {
                HUDOverlay.SELECTED_BOUND_MIRROR_INDICATOR.render(x, y, 22, 22, tickDelta);
            }
            matrixStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            // probably want to render item while we're here too ?
            trackedStack = trackedStack.copy(); // so we don't accidentally change the nbt of the actual item
            StackNBT.putDouble(trackedStack, SyncedItemHandling.IS_REFLECTED_TAG, 1);
            guiGraphics.renderItem(player, trackedStack, x, y, seed);
            guiGraphics.renderItemDecorations(minecraft.font, trackedStack, x, y);
        }
    }

    // so it renders the offhand slot even if it's technically empty
    @WrapOperation(method="renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
    at=@At(value="INVOKE", target = "net/minecraft/world/item/ItemStack.isEmpty()Z"))
    private boolean plsRenderOffhand(ItemStack stack, Operation<Boolean> original){
        if(ClientUtils.shouldShowReflected() && minecraft.player instanceof IShallowMirrorBinder shallowBinder){
            ItemStack trackedStack = shallowBinder.getTrackedStack();
            if(trackedStack != null && !trackedStack.isEmpty()){
                return false;
            }
        }
        return original.call(stack);
    }

    @WrapOperation(method="renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
    at=@At(value="INVOKE", target="net/minecraft/client/gui/GuiGraphics.renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void renderReflectedItem(GuiGraphics guiGraphics, LivingEntity entity, ItemStack stack, int x, int y, int seed, Operation<Void> original){
        ItemStack itemToRender = stack;
        if(ClientUtils.shouldShowReflected() && !stack.isEmpty() && stack.getItem() instanceof ItemHandMirror mirrorItem){
            if(mirrorItem.isMirrorActivated(stack)){
                ItemStack maybeItemToRender = mirrorItem.getMirroredItemStack(stack);
                if(maybeItemToRender != null && !maybeItemToRender.isEmpty()){
                    itemToRender = maybeItemToRender;
                    StackNBT.putDouble(itemToRender, SyncedItemHandling.IS_REFLECTED_TAG, 1);
                }
            }
        }
        // need to handle empty hand reflection somewhere else since this won't even be called if it's empty
        original.call(guiGraphics, entity, itemToRender, x, y, seed);
    }

    @WrapOperation(method="renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
    at=@At(value="INVOKE", target="net/minecraft/client/gui/GuiGraphics.renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
    private void renderReflectedItemOverlay(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y, Operation<Void> original){
        ItemStack itemToRender = stack;
        if(ClientUtils.shouldShowReflected() && !stack.isEmpty() && stack.getItem() instanceof ItemHandMirror mirrorItem){
            if(mirrorItem.isMirrorActivated(stack)){
                ItemStack maybeItemToRender = mirrorItem.getMirroredItemStack(stack);
                if(maybeItemToRender != null && !maybeItemToRender.isEmpty()){
                    itemToRender = maybeItemToRender;
                    StackNBT.putDouble(itemToRender, SyncedItemHandling.IS_REFLECTED_TAG, 1);
                }
            }
        }
        original.call(guiGraphics, font, itemToRender, x, y);
    }

}
