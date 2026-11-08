package com.samsthenerd.hexgloop.mixins.wnboi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.samsthenerd.hexgloop.casting.orchard.IOrchard;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.items.ItemFidget;
import com.samsthenerd.hexgloop.items.ItemMultiFocus;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;

// Hex 0.12: MsgShiftScrollSyn -> MsgShiftScrollC2S (record, common.msgs), поле invertSpellbook убрано,
// 1.21: hasCustomHoverName() -> has(DataComponents.CUSTOM_NAME), Rarity.color -> color()
@Mixin(MsgShiftScrollC2S.class)
public class MixinHandleScrolling{
    @Inject(at = @At("TAIL"), method = "handleForHand(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/InteractionHand;D)V")
    private void handleForHand(ServerPlayer sender, InteractionHand hand, double delta, CallbackInfo info) {
        if(delta != 0){
            ItemStack stack = sender.getItemInHand(hand);
            if(stack.getItem() instanceof ItemMultiFocus){
                handleMultiFocusScroll(sender, hand, stack, delta);
            }
            if(stack.getItem() instanceof ItemFidget){
                handleFidgetScroll(sender, hand, stack, delta);
            }
            if(stack.getItem() == HexGloopItems.DYEABLE_SPELLBOOK_ITEM.get()){
                ((MixinScrollbookInvoker)(Object)this).invokeHandleSpellbook(sender, hand, stack, delta);
            }
        }
    }

    // i love stealing hex code :D
    public void handleMultiFocusScroll(ServerPlayer sender, InteractionHand hand, ItemStack stack, double delta) {
        var newIdx = ItemMultiFocus.rotatePageIdx(stack, delta < 0.0);

        var len = ItemSpellbook.highestPage(stack);

        var sealed = ItemSpellbook.isSealed(stack);

        MutableComponent component;
        if (hand == InteractionHand.OFF_HAND && stack.has(DataComponents.CUSTOM_NAME)) {
            if (sealed) {
                component = Component.translatable("hexgloop.tooltip.multi_focus.page_with_name.sealed",
                    Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    Component.literal("").withStyle(stack.getRarity().color(), ChatFormatting.ITALIC)
                        .append(stack.getHoverName()),
                    Component.translatable("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD));
            } else {
                component = Component.translatable("hexgloop.tooltip.multi_focus.page_with_name",
                    Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    Component.literal("").withStyle(stack.getRarity().color(), ChatFormatting.ITALIC)
                        .append(stack.getHoverName()));
            }
        } else {
            if (sealed) {
                component = Component.translatable("hexgloop.tooltip.multi_focus.page.sealed",
                    Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    Component.translatable("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD));
            } else {
                component = Component.translatable("hexgloop.tooltip.multi_focus.page",
                    Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE));
            }
        }

        sender.displayClientMessage(component.withStyle(ChatFormatting.GRAY), true);
    }

    public void handleFidgetScroll(ServerPlayer sender, InteractionHand hand, ItemStack stack, double delta) {
        var newIdx = ItemFidget.rotatePageIdx(stack, delta < 0.0);

        ((IOrchard)sender).setOrchardValue(newIdx);

        ItemFidget fidgetItem = (ItemFidget) stack.getItem();

        var len = fidgetItem.fidgetSettings.slots;

        MutableComponent component;
        if (hand == InteractionHand.OFF_HAND && stack.has(DataComponents.CUSTOM_NAME)) {
            component = Component.translatable("hexgloop.tooltip.fidget.page_with_name",
                Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                Component.literal("").withStyle(stack.getRarity().color(), ChatFormatting.ITALIC)
                    .append(stack.getHoverName()));
        } else {
            component = Component.translatable("hexgloop.tooltip.fidget.page",
                Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE));
        }
        sender.displayClientMessage(component.withStyle(ChatFormatting.GRAY), true);
    }
}
