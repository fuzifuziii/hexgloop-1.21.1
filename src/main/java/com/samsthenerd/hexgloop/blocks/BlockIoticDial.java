package com.samsthenerd.hexgloop.blocks;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.samsthenerd.hexgloop.blockentities.BlockEntityDial;
import com.samsthenerd.hexgloop.items.HexGloopItems;

public class BlockIoticDial extends BaseEntityBlock {

    public static final com.mojang.serialization.MapCodec<BlockIoticDial> CODEC = simpleCodec(BlockIoticDial::new);

    @Override
    public com.mojang.serialization.MapCodec<BlockIoticDial> codec(){
        return CODEC;
    }

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty SELECTED = IntegerProperty.create("selected", 0, 6);

    public BlockIoticDial(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(SELECTED, 0)
        );
    }

    public static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    static {
        SHAPES.put(Direction.NORTH, Shapes.box(0, 0, 11.0/16.0, 1, 1, 1));
        SHAPES.put(Direction.SOUTH, Shapes.box(0, 0, 0, 1, 1, 5.0/16.0));
        SHAPES.put(Direction.EAST, Shapes.box(0, 0, 0, 5.0/16.0, 1, 1));
        SHAPES.put(Direction.WEST, Shapes.box(11.0/16.0, 0, 0, 1, 1, 1));
        SHAPES.put(Direction.UP, Shapes.box(0, 0, 0, 1, 5.0/16.0, 1));
        SHAPES.put(Direction.DOWN, Shapes.box(0, 11.0/16.0, 0, 1, 1, 1));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new BlockEntityDial(pos, state);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(SELECTED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return state.getValue(SELECTED);
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape( @Nonnull BlockState state )
    {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if(world.getBlockEntity(pos) instanceof BlockEntityDial dialBE 
        // make sure it's not just changing something with the blockstate
        && newState.getBlock() != this && world instanceof ServerLevel sWorld){
            ItemStack stack = dialBE.getInnerMultiFocus().copy();
            if(!stack.isEmpty()){
                sWorld.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    private static Map<Direction.Axis, FaceCalcinator> faceCalcs = new HashMap<Direction.Axis, FaceCalcinator>();
    
    static {
        faceCalcs.put(Direction.Axis.X, (mirrored, pos) -> mirrored ? new Tuple<>(pos.z, pos.y) : new Tuple<>(1-pos.z, pos.y));
        faceCalcs.put(Direction.Axis.Y, (mirrored, pos) -> mirrored ? new Tuple<>(pos.x, pos.z) : new Tuple<>(pos.x, 1-pos.z));
        faceCalcs.put(Direction.Axis.Z, (mirrored, pos) -> mirrored ? new Tuple<>(1-pos.x, pos.y) : new Tuple<>(pos.x, pos.y));
    };

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return useCommon(state, world, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult res = useCommon(state, world, pos, player, hand, hit);
        return res == InteractionResult.PASS ? net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : net.minecraft.world.ItemInteractionResult.sidedSuccess(world.isClientSide);
    }

    private InteractionResult useCommon(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!(world.getBlockEntity(pos) instanceof BlockEntityDial dialBE)) return InteractionResult.FAIL;
        if(dialBE.getInnerMultiFocus().isEmpty() && player.getItemInHand(hand).getItem() == HexGloopItems.MULTI_FOCUS_ITEM.get()){
            dialBE.setInnerMultiFocus(player.getItemInHand(hand).copy());
            player.getItemInHand(hand).shrink(1);
            return InteractionResult.SUCCESS;
        }
        if(!dialBE.getInnerMultiFocus().isEmpty() && player.isShiftKeyDown()){
            player.getInventory().placeItemBackInInventory(dialBE.getInnerMultiFocus());
            dialBE.setInnerMultiFocus(ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }
        if(dialBE.getInnerMultiFocus().isEmpty()) return InteractionResult.FAIL;
        Direction face = hit.getDirection();
        Vec3 nPos = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        Tuple<Double, Double> coords = faceCalcs.get(face.getAxis()).calc(face.getAxisDirection() == Direction.AxisDirection.NEGATIVE, nPos);
        double r = new Vec3(coords.getA()-0.5, coords.getB()-0.5, 0).length();
        double angle = (Math.atan2(coords.getB()-0.5, coords.getA()-0.5) + (2 * Math.PI)) % (2 * Math.PI);
        if(face == state.getValue(FACING)){
            int sel = (1 + 6 - (int)Math.floor(6 * angle / (2 * Math.PI))) % 6 + 1;
            dialBE.setSelection(sel);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
        // HexGloop.logPrint("(" + coords.getLeft() + ", " + coords.getRight() + ")");
    }

    @FunctionalInterface
    public static interface FaceCalcinator{
        public Tuple<Double, Double> calc(boolean mirrored, Vec3 pos);
    }
}
