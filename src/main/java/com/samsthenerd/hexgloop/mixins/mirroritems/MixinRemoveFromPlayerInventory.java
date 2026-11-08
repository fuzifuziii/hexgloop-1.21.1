package com.samsthenerd.hexgloop.mixins.mirroritems;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.items.ItemAbstractPassThrough;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Inventory.class)
public class MixinRemoveFromPlayerInventory {

    @Shadow
    @Final
    public Player player;


    @WrapOperation(method="removeItem(Lnet/minecraft/world/item/ItemStack;)V",
    at=@At(value="INVOKE", target="net/minecraft/core/NonNullList.get (I)Ljava/lang/Object;"))
    private Object removeMirrorItems(NonNullList defaultedList, int index, Operation<Object> original, ItemStack stackToRemove){
        Object objectAtSlot = original.call(defaultedList, index);
        if(!(objectAtSlot instanceof ItemStack stackAtSlot)) return objectAtSlot;
        if(stackAtSlot.isEmpty()) return stackAtSlot;
        // maybe add more checks in here so it grabs stuff for like more offhand mods ?
        InteractionHand hand = null;
        if(stackAtSlot == player.getMainHandItem()) hand = InteractionHand.MAIN_HAND;
        if(stackAtSlot == player.getOffhandItem()) hand = InteractionHand.OFF_HAND;
        if(stackAtSlot.getItem() instanceof ItemAbstractPassThrough passItem && hand != null){
            ItemStack storedItem = passItem.getStoredItem(stackAtSlot, player, player.level(), hand);
            if(storedItem != null && storedItem == stackToRemove){
                passItem.setStoredItem(stackAtSlot, player, player.level(), hand, ItemStack.EMPTY);
            }
        }
        return stackAtSlot;
    }
}
