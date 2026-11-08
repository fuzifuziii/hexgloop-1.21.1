package com.samsthenerd.hexgloop.mixins.booktweaks;

import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.misc.clientgreatbook.GreatBook;
import com.samsthenerd.hexgloop.misc.clientgreatbook.PatternPageLookup;
import com.samsthenerd.hexgloop.utils.patternmatching.PatternMatching;

import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.item.ItemModBook;

// and also scroll tooltip stuff
@Mixin(Item.class)
public class MixinClientClicksForGreatBook {
    
    // not in hand
    @Inject(
        method="overrideOtherStackedOnMe(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/SlotAccess;)Z",
        at=@At("HEAD")
    )
    public void onBookClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if(player.level().isClientSide && stack.getItem() instanceof ItemModBook modBook){ 
            Book book = ItemModBook.getBook(stack);
            if(book == null || !book.id.toString().equals("hexcasting:thehexbook")) return;
            // should be hex book
            if(otherStack.getItem() instanceof ItemScroll scrollItem){
                try{
                    if(scrollItem.readIota(otherStack) instanceof PatternIota pIota){
                        GreatBook.INSTANCE.savePattern(pIota.getPattern());
                    }
                } catch(Exception e){
                    // do nothing, just to catch any issues with using null
                }
                return;
            }
            if(otherStack.getItem() == Items.SHEARS){
                GreatBook.INSTANCE.clearAll();
                return;
            }
        }
    }

    // in hand
    // @Inject(
    //     method="overrideStackedOnOther(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;)Z",
    //     at=@At("HEAD")
    // )
    // public void onBookStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {

    // }

    // pattern tooltip stuff
    @Inject(
        method="appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V",
        at = @At("HEAD")
    )
    public void appendPatternTooltip(ItemStack stack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag context, CallbackInfo ci) {
        if(stack.getItem() instanceof ItemScroll scrollItem){
            try{
                if(scrollItem.readIota(stack) instanceof PatternIota pIota){
                    ResourceLocation id = PatternMatching.getIdentifier(pIota.getPattern());
                    if(id == null) return;
                    Component patternName = PatternMatching.getName(pIota.getPattern());
                    Style tooltipStyle = Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY);
                    tooltip.add(patternName.copy().setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
                    if(Screen.hasShiftDown()){
                        net.minecraft.util.Tuple<Component,Component> descriptionTexts = PatternPageLookup.getDescription(pIota.getPattern());
                        tooltip.add(descriptionTexts.getA().copy().setStyle(tooltipStyle));
                        tooltip.add(descriptionTexts.getB().copy().setStyle(tooltipStyle));
                    } else {
                        tooltip.add(Component.translatable("hexgloop.tooltip.pattern_shift").copy().setStyle(tooltipStyle));
                    }
                }
            } catch(Exception e){
                // do nothing, just to catch any issues with using null
                HexGloop.logPrint("caught in scroll tooltip: " + e.toString());
            }
        }
    }
}
