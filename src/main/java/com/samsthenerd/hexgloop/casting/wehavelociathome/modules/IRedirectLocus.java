package com.samsthenerd.hexgloop.casting.wehavelociathome.modules;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IRedirectLocus extends ILocusModule {
    public BlockPos forceNextPos(BlockPos currentPos, BlockState currentState, Level world, 
        List<BlockPos> trackedBlocks, Set<BlockPos> knownBlocks);
}
