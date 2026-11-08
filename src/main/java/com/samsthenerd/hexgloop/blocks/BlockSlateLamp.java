package com.samsthenerd.hexgloop.blocks;

import java.util.EnumSet;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public class BlockSlateLamp extends BlockCircleComponent {
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public BlockSlateLamp(Properties settings) {
        super(settings);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(ENERGIZED, false)
                .setValue(LIT, false)
        );
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue(LIT, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClientSide) {
            return;
        }
        boolean bl = state.getValue(LIT);
        if (bl != world.hasNeighborSignal(pos)) {
            if (bl) {
                world.scheduleTick(pos, this, 4);
            } else {
                world.setBlock(pos, (BlockState)state.cycle(LIT), Block.UPDATE_CLIENTS);
            }
        }
    }

    // idk why it's saying it can't override ?
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT).booleanValue() && !world.hasNeighborSignal(pos)) {
            world.setBlock(pos, (BlockState)state.cycle(LIT), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos,
        BlockState bs, ServerLevel world){
        return true;
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, Level world){
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public ICircleComponent.ControlFlow acceptControlFlow(CastingImage image, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerLevel world) {
        var exits = possibleExitDirections(pos, bs, world);
        exits.remove(enterDir.getOpposite());
        return new ICircleComponent.ControlFlow.Continue(image, exits.stream().map(d -> exitPositionFromDirection(pos, d)).toList());
    }

    // it's not really a directional block
    public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft){
        return Direction.UP;
    }

    public HexPattern getPattern(BlockPos pos, BlockState bs, Level world){
        return null;
    }

    public float particleHeight(BlockPos pos, BlockState bs, Level world){
        return 0.6f;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(LIT);
    }
}
