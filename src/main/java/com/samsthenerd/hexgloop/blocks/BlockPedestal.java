package com.samsthenerd.hexgloop.blocks;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.samsthenerd.hexgloop.blockentities.BlockEntityPedestal;
import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociAtHome;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.IIotaProviderLocus;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public class BlockPedestal extends BlockCircleComponent implements EntityBlock, ILociAtHome, IIotaProviderLocus, IDynamicFlayTarget{
    public final boolean isMirror;
    public final boolean hasMindStorage;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty MINDFUL = BooleanProperty.create("mindful");

    public BlockPedestal(Properties settings, boolean isMirror, boolean hasMindStorage) {
        super(settings);
        this.isMirror = isMirror;
        this.hasMindStorage = hasMindStorage;
        registerDefaultState(this.stateDefinition.any()
                .setValue(ENERGIZED, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(MINDFUL, false));
    }

    public BlockPedestal(Properties settings, boolean isMirror) {
        this(settings, isMirror, false);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new BlockEntityPedestal(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (type != HexGloopBEs.PEDESTAL_BE.get()) return null;
        return (_world, _pos, _state, _be) -> ((BlockEntityPedestal)_be).tick(_world, _pos, _state);
    }
    
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if(world.getBlockEntity(pos) instanceof BlockEntityPedestal be 
        // make sure it's not just changing something with the blockstate
        && newState.getBlock() != this){
            be.onRemoved();
        }
        super.onRemove(state, world, pos, newState, moved);
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
		return super.getStateForPlacement(ctx).setValue(FACING, ctx.getClickedFace());
	}

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntityPedestal be = world.getBlockEntity(pos, HexGloopBEs.PEDESTAL_BE.get()).orElse(null);
        if(be == null) return 0;
        return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)be);
    }

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
        BlockEntityPedestal be = world.getBlockEntity(pos, HexGloopBEs.PEDESTAL_BE.get()).orElse(null);
        if(be != null){
            InteractionResult res = be.use(player, hand, hit);
            if(res == InteractionResult.SUCCESS){
                be.interacted(player);
            }
            return res;
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape( @Nonnull BlockState state )
    {
        return RenderShape.MODEL;
    }

    // from BlockWithEntity

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        super.triggerEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.triggerEvent(type, data);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider)((Object)blockEntity) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(MINDFUL);
    }

    // circle-y stuff
    // Hex 0.12: canEnterFromDirection no longer takes a normalDir or a plain Level
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
        BlockEntityPedestal be = world.getBlockEntity(pos, HexGloopBEs.PEDESTAL_BE.get()).orElse(null);
        if(be != null) return be.getPattern();
        return null;
    }

    public Iota provideIota(BlockPos pos, BlockState bs, Level world, CastingVM harness){
        BlockEntityPedestal be = world.getBlockEntity(pos, HexGloopBEs.PEDESTAL_BE.get()).orElse(null);
        if(be != null && isMirror) return be.getIota();
        return null;
    }

    public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft){
        return bs.getValue(FACING);
    }

    public float particleHeight(BlockPos pos, BlockState bs, Level world){
        return (float)BlockEntityPedestal.HEIGHT;
    }

    public void absorbVillagerMind(Villager sacrifice, BlockPos flayPos, CastingEnvironment ctx){
        ctx.getWorld().getBlockEntity(flayPos, HexGloopBEs.PEDESTAL_BE.get()).ifPresent((be) -> {
            ((BlockEntityPedestal)be).absorbVillagerMind(sacrifice, flayPos, ctx);
        });
    }
    
    // return true if it can be accepted
    public boolean canAcceptMind(Villager sacrifice, BlockPos flayPos, CastingEnvironment ctx){
        return ctx.getWorld().getBlockEntity(flayPos, HexGloopBEs.PEDESTAL_BE.get()).map((be) -> {
            return ((BlockEntityPedestal)be).canAcceptMind(sacrifice, flayPos, ctx);
        }).orElse(false);
    }
}
