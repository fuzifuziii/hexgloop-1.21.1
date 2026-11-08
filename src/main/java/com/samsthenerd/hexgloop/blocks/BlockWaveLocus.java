package com.samsthenerd.hexgloop.blocks;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociAtHome;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public class BlockWaveLocus extends BlockCircleComponent implements ILociAtHome{

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public BlockWaveLocus(Properties settings) {
        super(settings);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(ENERGIZED, false)
                .setValue(POWERED, false)
        );
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
        pBuilder.add(POWERED);
    }


    public void waveEnter(BlockPos pos, BlockState bs, Level world, BlockEntityAbstractImpetus impetus){
        world.setBlockAndUpdate(pos, bs.setValue(POWERED, true));
        world.updateNeighborsAt(pos, this);
    }

    public void waveExit(BlockPos pos, BlockState bs, Level world, BlockEntityAbstractImpetus impetus){
        world.setBlockAndUpdate(pos, bs.setValue(POWERED, false));
        world.updateNeighborsAt(pos, this);
    }

    // called when the circle stops, either early or on time.
    public void circleStopped(BlockPos pos, BlockState bs, Level world, BlockEntityAbstractImpetus impetus){
        world.setBlockAndUpdate(pos, bs.setValue(POWERED, false));
        world.updateNeighborsAt(pos, this);
    }

    // not sure if this should be true only when powered ?
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }
}
