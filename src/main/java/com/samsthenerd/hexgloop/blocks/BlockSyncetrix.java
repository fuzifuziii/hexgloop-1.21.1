package com.samsthenerd.hexgloop.blocks;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociAtHome;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.IRedirectLocus;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public class BlockSyncetrix extends BlockCircleComponent implements ILociAtHome, IRedirectLocus{

    public static DirectionProperty FACING = BlockStateProperties.FACING;
    public static final int SYNC_RANGE = 16;
    
    public BlockSyncetrix(Properties settings) {
        super(settings);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(ENERGIZED, false)
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }


    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos,
        BlockState bs, ServerLevel world){
        return true;
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, Level world){
        return EnumSet.noneOf(Direction.class);
    }

    // no normal exits: the next position comes from IRedirectLocus.forceNextPos via the loci-at-home mixin,
    // so Continue with the (empty) possibleExitDirections set, not Stop
    @Override
    public ICircleComponent.ControlFlow acceptControlFlow(CastingImage image, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerLevel world) {
        var exits = possibleExitDirections(pos, bs, world);
        exits.remove(enterDir.getOpposite());
        return new ICircleComponent.ControlFlow.Continue(image, exits.stream().map(d -> exitPositionFromDirection(pos, d)).toList());
    }

    public BlockPos forceNextPos(BlockPos currentPos, BlockState currentState, Level world, 
        List<BlockPos> trackedBlocks, Set<BlockPos> knownBlocks){
        BlockPos immediatePos = currentPos.relative(currentState.getValue(FACING));
        BlockState immediateState = world.getBlockState(immediatePos);
        // try to find an immediate block if there is one
        if(immediateState.getBlock() instanceof BlockCircleComponent component){
            HexGloop.logPrint("found immediate block: " + immediateState.getBlock());
            for(Direction dir : Direction.values()){
                HexGloop.logPrint("checking direction: " + dir);
                if(dir.equals(currentState.getValue(FACING))){
                    HexGloop.logPrint("skipping opposite direction");
                    continue; // don't get it if you have to flip
                }
                if(world instanceof ServerLevel sLevel && component.canEnterFromDirection(currentState.getValue(FACING).getOpposite(), immediatePos, immediateState, sLevel)){
                    HexGloop.logPrint("found valid path");
                    return immediatePos;
                }
                HexGloop.logPrint("no valid path");
            }
        }
        // find another syncetrix
        for(int i = 1; i <= SYNC_RANGE; i++){
            BlockPos checkPos = currentPos.relative(currentState.getValue(FACING), i);
            BlockState checkState = world.getBlockState(checkPos);
            if(checkState.getBlock() instanceof BlockSyncetrix){
                return checkPos;
            }
        }
        return null; 
    }

    // it's not really a directional block
    public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft){
        return bs.getValue(FACING);
    }

    public HexPattern getPattern(BlockPos pos, BlockState bs, Level world){
        return null;
    }

    public float particleHeight(BlockPos pos, BlockState bs, Level world){
        return 0.75f;
    }


}
