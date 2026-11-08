package com.samsthenerd.hexgloop.blocks;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.samsthenerd.hexgloop.blockentities.BlockEntityPedestal;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociAtHome;
import com.samsthenerd.hexgloop.casting.wehavelociathome.LociUtils;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.ISpeedLocus;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public class BlockAccelerator extends BlockCircleComponent implements ILociAtHome, ISpeedLocus{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockAccelerator(Properties settings) {
        super(settings);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(ENERGIZED, false)
                .setValue(FACING, Direction.NORTH));
    }

    public static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    static {
        SHAPES.put(Direction.NORTH, Shapes.box(0, 0, 0.25, 1, 1, 1));
        SHAPES.put(Direction.SOUTH, Shapes.box(0, 0, 0, 1, 1, 0.75));
        SHAPES.put(Direction.EAST, Shapes.box(0, 0, 0, 0.75, 1, 1));
        SHAPES.put(Direction.WEST, Shapes.box(0.25, 0, 0, 1, 1, 1));
        SHAPES.put(Direction.UP, Shapes.box(0, 0, 0, 1, 0.75, 1));
        SHAPES.put(Direction.DOWN, Shapes.box(0, 0.25, 0, 1, 1, 1));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return super.getStateForPlacement(ctx).setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
	}

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return state.getValue(ENERGIZED) ? 15 : 0;
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape( @Nonnull BlockState state )
    {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    // circle-y stuff
    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos,
        BlockState bs, ServerLevel world){
        var thisNormal = this.normalDir(pos, bs, world);
        return enterDir != thisNormal;
    }

    // yoinked from BlockSlate
    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, Level world){
        var allDirs = EnumSet.allOf(Direction.class);
        var normal = this.normalDir(pos, bs, world);
        allDirs.remove(normal);
        allDirs.remove(normal.getOpposite());
        return allDirs;
    }

    @Override
    public ICircleComponent.ControlFlow acceptControlFlow(CastingImage image, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerLevel world) {
        var exits = possibleExitDirections(pos, bs, world);
        exits.remove(enterDir.getOpposite());
        return new ICircleComponent.ControlFlow.Continue(image, exits.stream().map(d -> exitPositionFromDirection(pos, d)).toList());
    }

    @Nullable
    public HexPattern getPattern(BlockPos pos, BlockState bs, Level world){
        return null;
    }

    public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft){
        return bs.getValue(FACING);
    }

    public float particleHeight(BlockPos pos, BlockState bs, Level world){
        return (float)BlockEntityPedestal.HEIGHT;
    }

    // accelerate !
    @Override
    public double modifyTickDelay(int blocksAgo, double currentModifier, int originalSpeed, 
        int similarBlockCount, List<BlockPos> trackedBlocks, Level world, BlockEntityAbstractImpetus impetus){
        
        if(blocksAgo > 10) return 1;
        double currentSpeed = 1 / currentModifier;
        double speedIncrease = Math.max((10 - blocksAgo) * 0.2, 0);
        if(blocksAgo == 0){
            // handle cost - 1 dust * newSpeed-1 + 1 shard for every whole 100% over 3x
            double costDust = MediaConstants.DUST_UNIT * (currentSpeed + speedIncrease - 1);
            double costShard = Math.floor(currentSpeed + speedIncrease - 3) * MediaConstants.SHARD_UNIT;
            double cost = costDust + costShard; // every 100% over 3x costs a shard

            // HexGloop.logPrint("entered accelerator #" + similarBlockCount + " with: " +
            //     "\n\tentering speed of " + currentSpeed + "x" +
            //     "\n\tincrease of " + speedIncrease + "x" + 
            //     "\n\texiting with speed: " + currentSpeed + speedIncrease + "x" + 
            //     "\n\tcost: " + costDust + " + " + costShard + " = " + cost + " media");
            
            if(LociUtils.withdrawCircleMedia(impetus, (int)Math.min(cost, LociUtils.getCircleMedia(impetus)), true, false) > 0){
                // didn't fully withdraw - put a delay in as punishment
                return ISpeedLocus.modifierForTarget(0.5, currentModifier);
            }
            return -1; // instant activate the next block
        }
        return ISpeedLocus.modifierForTarget(speedIncrease+currentSpeed, currentModifier);
    }
}
