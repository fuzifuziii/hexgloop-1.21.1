package com.samsthenerd.hexgloop.blocks.iotic;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IIoticProvider {
    public ADIotaHolder getIotaHolder(Level world, BlockPos pos);
}
