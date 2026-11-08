package com.samsthenerd.hexgloop.mixins.lociathome;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestBlockEntity.class)
public interface MixinExposeChestInventory {
    @Accessor("items")
    public void setInventory(NonNullList<ItemStack> inv);
}
