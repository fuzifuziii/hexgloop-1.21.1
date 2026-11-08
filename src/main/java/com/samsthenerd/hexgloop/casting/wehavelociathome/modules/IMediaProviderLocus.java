package com.samsthenerd.hexgloop.casting.wehavelociathome.modules;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IMediaProviderLocus extends ILocusModule{
    public ADMediaHolder getMediaHolder(BlockPos pos, Level world, BlockEntityAbstractImpetus impetus);
}
