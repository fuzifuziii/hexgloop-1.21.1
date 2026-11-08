package com.samsthenerd.hexgloop.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

// for blocks that cats should sit on !
public interface ICatPost {
    default boolean ifItFits(LevelReader world, BlockPos pos){
        return true;
    }
}
