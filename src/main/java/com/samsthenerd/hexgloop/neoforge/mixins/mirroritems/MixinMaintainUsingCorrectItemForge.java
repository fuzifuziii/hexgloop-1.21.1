package com.samsthenerd.hexgloop.neoforge.mixins.mirroritems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.samsthenerd.hexgloop.items.ItemAbstractPassThrough;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;

@Mixin(LivingEntity.class)
public class MixinMaintainUsingCorrectItemForge {

    // 1.21: activeItemStack was renamed to useItem
    @Shadow
    protected ItemStack useItem;

    // 1.21 NeoForge: tickActiveItemStack() is now the private updatingUsingItem(), ForgeHooks is
    // CommonHooks, and canContinueUsing is called as canContinueUsing(useItem, handItem) -
    // i.e. left is the active stack, right is the stack currently in hand (swapped vs old Forge)
    @Redirect(method = "updatingUsingItem()V",
    at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;canContinueUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean makeItemsEqualInUpdatingUsingItem(ItemStack left, ItemStack right){
        // left is active (useItem) stack, right is hand stack
        boolean originalRes = CommonHooks.canContinueUsing(left, right);
        if(originalRes) return true;
        if(right.getItem() instanceof ItemAbstractPassThrough passItem){
            LivingEntity thisEnt = (LivingEntity)(Object)this;
            ItemStack storedItem = passItem.getStoredItem(right, thisEnt, thisEnt.level(), thisEnt.getUsedItemHand());
            if(storedItem == null) return originalRes;
            return CommonHooks.canContinueUsing(left, storedItem);
        }
        return originalRes;
    }
}
