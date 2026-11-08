package com.samsthenerd.hexgloop.blocks;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.samsthenerd.hexgloop.blockentities.BlockEntityGloopEnergizer;
import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;

public class BlockGloopEnergizer extends BaseEntityBlock{
    public static final com.mojang.serialization.MapCodec<BlockGloopEnergizer> CODEC = simpleCodec(BlockGloopEnergizer::new);

    @Override
    public com.mojang.serialization.MapCodec<BlockGloopEnergizer> codec(){
        return CODEC;
    }

    public static final int BOILER_RADIUS = 4;

    public BlockGloopEnergizer(Properties settings) {
        super(settings);
    }

    public static Set<BlockPos> getAdjacentWater(Level world, BlockPos startPos){
        Set <BlockPos> waterBlocks = new HashSet<BlockPos>();
        Stack<BlockPos> queue = new Stack<BlockPos>();
        queue.push(startPos);
        while(!queue.empty()){
            BlockPos pos = queue.pop();
            for(Direction dir : Direction.values()){
                BlockPos newPos = pos.relative(dir);
                if(world.isWaterAt(newPos) && !waterBlocks.contains(newPos) && newPos.distManhattan(startPos) <= BOILER_RADIUS){
                    waterBlocks.add(newPos);
                    queue.push(newPos);
                }
            }
        }
        return waterBlocks;
    }

    // null if none exists
    public static BlockPos getNearestEnergizer(Level world, BlockPos startPos){
        Set <BlockPos> waterBlocks = new HashSet<BlockPos>();
        Stack<BlockPos> queue = new Stack<BlockPos>();
        if(world.getBlockState(startPos).getBlock() instanceof BlockGloopEnergizer){
            return startPos;
        }
        queue.push(startPos);
        while(!queue.empty()){
            BlockPos pos = queue.pop();
            for(Direction dir : Direction.values()){
                BlockPos newPos = pos.relative(dir);
                if(world.getBlockState(newPos).getBlock() instanceof BlockGloopEnergizer){
                    return newPos;
                }
                if(world.isWaterAt(newPos) && !waterBlocks.contains(newPos) && newPos.distManhattan(startPos) <= BOILER_RADIUS){
                    waterBlocks.add(newPos);
                    queue.push(newPos);
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new BlockEntityGloopEnergizer(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (type != HexGloopBEs.GLOOP_ENERGIZER_BE.get()) return null;
        return (_world, _pos, _state, _be) -> ((BlockEntityGloopEnergizer)_be).tick(_world, _pos, _state);
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape( @Nonnull BlockState state )
    {
        return RenderShape.MODEL;
    }
}
