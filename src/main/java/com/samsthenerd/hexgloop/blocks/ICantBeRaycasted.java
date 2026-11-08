package com.samsthenerd.hexgloop.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

// indicates that the block shouldn't be returned as a raycast result
public interface ICantBeRaycasted {
    public default boolean passThrough(Level world, BlockPos pos){
        return true;
    }
}
