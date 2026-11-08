package com.samsthenerd.hexgloop.items;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker;

public interface LabelyItem {
    @Nullable
    public default boolean putLabel(ItemStack stack, int index, CompoundTag labelNbt){
        return LabelMaker.putLabel(stack, labelNbt, index);
    }

    // put label to the current selection
    @Nullable
    public boolean putLabel(ItemStack stack, CompoundTag labelNbt);
}
