package com.samsthenerd.hexgloop.mixins.recipes;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.items.HexGloopItems;

import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexItems;

@Mixin(PotionBrewing.class)
public class MixinBrewCastingPotion {
    
    @Inject(method = "mix(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void brewCastingPotion(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if(!input.isEmpty() && input.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS) instanceof net.minecraft.world.item.alchemy.PotionContents pc && pc.is(net.minecraft.world.item.alchemy.Potions.THICK)){
            if(ingredient.getItem() == HexItems.AMETHYST_DUST){
                cir.setReturnValue(HexGloopItems.CASTING_POTION_ITEM.get().getDefaultInstance());
            }
            if(ingredient.getItem() instanceof PigmentItem){
                // use uuid zero so that it gets synced when player touches it
                UUID uuid = new UUID(0, 0);
                FrozenPigment thisFrozen = new FrozenPigment(ingredient, uuid);
                ItemStack colorizedPotion = HexGloopItems.CASTING_POTION_ITEM.get().withColorizer(HexGloopItems.CASTING_POTION_ITEM.get().getDefaultInstance(), thisFrozen);
                cir.setReturnValue(colorizedPotion);
            }
        }
    }

    @Inject(method = "hasMix(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void giveCastingRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir){
        if(!input.isEmpty()){
            net.minecraft.world.item.alchemy.PotionContents contents = input.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
            if(contents == null || !contents.is(net.minecraft.world.item.alchemy.Potions.THICK)){
                return;
            }
            if(ingredient.getItem() == HexItems.AMETHYST_DUST || ingredient.getItem() instanceof PigmentItem){
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method="isPotionIngredient(Lnet/minecraft/world/item/ItemStack;)Z", at=@At("HEAD"), cancellable=true)
    private static void isPotionRecipeIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if(stack.getItem() instanceof PigmentItem || stack.getItem() == HexItems.AMETHYST_DUST){
            cir.setReturnValue(true);
        }
    }
}
