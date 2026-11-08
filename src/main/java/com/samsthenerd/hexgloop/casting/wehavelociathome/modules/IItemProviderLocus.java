package com.samsthenerd.hexgloop.casting.wehavelociathome.modules;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IItemProviderLocus extends ILocusModule {
    public NonNullList<ItemStack> getProvidedItems(BlockPos pos, Level world, BlockEntityAbstractImpetus impetus);
}
