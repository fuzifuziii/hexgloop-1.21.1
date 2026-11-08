package com.samsthenerd.hexgloop.mixins.wnboi;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import com.samsthenerd.hexgloop.items.LabelyItem;

import at.petrak.hexcasting.common.items.storage.ItemSpellbook;

@Mixin(ItemSpellbook.class)
public class MixinLabelySpellbook implements LabelyItem{
    @Nullable
    public boolean putLabel(ItemStack stack, CompoundTag labelNbt){
        int index = ItemSpellbook.getPage(stack, 0);
        return putLabel(stack, index, labelNbt);
    }
}
