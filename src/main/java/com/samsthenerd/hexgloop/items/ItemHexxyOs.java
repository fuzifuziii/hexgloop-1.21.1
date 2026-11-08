package com.samsthenerd.hexgloop.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemHexxyOs extends Item {// extends ItemSimpleMediaProvider{

    public ItemHexxyOs(Properties settings) {
        // super(settings, mediaAmt, true, priority);
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        ItemStack itemStack = super.finishUsingItem(stack, world, user);
        if (user instanceof Player && ((Player)user).getAbilities().instabuild) {
            return itemStack;
        }
        if(user instanceof Player player && stack.getCount() > 0){
            player.getInventory().placeItemBackInInventory(new ItemStack(HexGloopItems.SLATE_BOWL.get()));
            return itemStack;
        }
        return new ItemStack(HexGloopItems.SLATE_BOWL.get());
    }

    /*public boolean shouldUseOwnWithdrawLogic(ItemStack stack){
        return true;
    }

    public int withdrawMedia(ItemStack stack, int cost, boolean simulate) {
        int mediaHere = getMedia(stack);
        if (cost < 0) {
            cost = mediaHere;
        }
        int realCost = Math.min(cost, mediaHere);
        if (!simulate) {
            int amountToTake = (int) Math.ceil(realCost / (double)mediaAmt);
            Entity stackHolder = stack.getHolder();
            if(stackHolder != null && stackHolder.getWorld() instanceof ServerWorld sWorld){
                ItemStack newStack = new ItemStack(HexGloopItems.SLATE_BOWL.get());
                newStack.setCount(amountToTake);
                ItemEntity newEnt = new ItemEntity(sWorld, stack.getHolder().getX(), stack.getHolder().getY(), stack.getHolder().getZ(), newStack);
                sWorld.spawnEntity(newEnt);
            }
            stack.decrement(amountToTake);
        }
        return realCost;
    }*/
}
