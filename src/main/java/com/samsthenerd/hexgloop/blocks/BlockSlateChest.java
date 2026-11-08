package com.samsthenerd.hexgloop.blocks;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.DoubleBlockCombiner.BlockType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.samsthenerd.hexgloop.blockentities.BlockEntitySlateChest;
import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociAtHome;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociHandler;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.IItemProviderLocus;
import com.samsthenerd.hexgloop.mixins.lociathome.MixinWeirdChestStatics;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;

// can't extend ChestBlock since we want it to be a circle component
// so much of this is just yoinked from ChestBlock
public class BlockSlateChest extends BlockCircleComponent implements SimpleWaterloggedBlock, EntityBlock, ILociAtHome, IItemProviderLocus {
    public static final DirectionProperty FACING;
    public static final EnumProperty<ChestType> CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED;
    // no idea what these are ??
    public static final int field_31057 = 1;
    protected static final int field_31058 = 1;
    protected static final int field_31059 = 14;
    protected static final VoxelShape DOUBLE_NORTH_SHAPE;
    protected static final VoxelShape DOUBLE_SOUTH_SHAPE;
    protected static final VoxelShape DOUBLE_WEST_SHAPE;
    protected static final VoxelShape DOUBLE_EAST_SHAPE;
    protected static final VoxelShape SINGLE_SHAPE;
    protected final Supplier<BlockEntityType<? extends ChestBlockEntity>> entityTypeRetriever;

    private boolean gloopy;

    public BlockSlateChest(Properties settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, boolean gloopy){
        super(settings);
        entityTypeRetriever = supplier;
        this.gloopy = gloopy;
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(ENERGIZED, false));
    }

    public boolean isGloopy(){
        return gloopy;
    }

    public NonNullList<ItemStack> getProvidedItems(BlockPos pos, Level world, BlockEntityAbstractImpetus impetus){
        ILociHandler lociHandler = ILociHandler.get(impetus);
        if(!isGloopy() || lociHandler == null || !lociHandler.getCastedBlocks().contains(pos)) return NonNullList.createWithCapacity(0);
        BlockEntitySlateChest chest = world.getBlockEntity(pos, HexGloopBEs.SLATE_CHEST_BE.get()).orElse(null);
        if(chest != null){
            return chest.getInvStackListPublic();
        }
        return NonNullList.createWithCapacity(0);
    }

    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        super.triggerEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity == null ? false : blockEntity.triggerEvent(type, data);
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerLevel world) {
        var thisNormal = this.normalDir(pos, bs, world);
        return enterDir != thisNormal;
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, Level world) {
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

    // i mean it's not super important for these to be at any specific circle location anyways
    @Override
    public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft) {
        return Direction.UP;
    }

    @Override
    public float particleHeight(BlockPos pos, BlockState bs, Level world) {
        return 15f / 16f;
    }

    public static DoubleBlockCombiner.BlockType getDoubleBlockType(BlockState state) {
        ChestType chestType = (ChestType)state.getValue(CHEST_TYPE);
        if (chestType == ChestType.SINGLE) {
            return BlockType.SINGLE;
        } else {
            return chestType == ChestType.RIGHT ? BlockType.FIRST : BlockType.SECOND;
        }
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if ((Boolean)state.getValue(WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        // not sure if we should have this at all ? want it to only be single though
        /*
        if (neighborState.isOf(this) && direction.getAxis().isHorizontal()) {
            ChestType chestType = (ChestType)neighborState.get(CHEST_TYPE);
            if (state.get(CHEST_TYPE) == ChestType.SINGLE && chestType != ChestType.SINGLE && state.get(FACING) == neighborState.get(FACING) && getFacing(neighborState) == direction.getOpposite()) {
                return (BlockState)state.with(CHEST_TYPE, chestType.getOpposite());
            }
        } else if (getFacing(state) == direction) {
            return (BlockState)state.with(CHEST_TYPE, ChestType.SINGLE);
        }
        */

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(CHEST_TYPE) == ChestType.SINGLE) {
            return SINGLE_SHAPE;
        } else {
            switch (getFacing(state).ordinal()-1) {
                case 1:
                default:
                return DOUBLE_NORTH_SHAPE;
                case 2:
                return DOUBLE_SOUTH_SHAPE;
                case 3:
                return DOUBLE_WEST_SHAPE;
                case 4:
                return DOUBLE_EAST_SHAPE;
            }
        }
    }

    public static Direction getFacing(BlockState state) {
        Direction direction = (Direction)state.getValue(FACING);
        return state.getValue(CHEST_TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        ChestType chestType = ChestType.SINGLE;
        Direction direction = ctx.getHorizontalDirection().getOpposite();
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        boolean bl = ctx.isSecondaryUseActive();
        Direction direction2 = ctx.getClickedFace();
        if (direction2.getAxis().isHorizontal() && bl) {
            Direction direction3 = this.getNeighborChestDirection(ctx, direction2.getOpposite());
            if (direction3 != null && direction3.getAxis() != direction2.getAxis()) {
                direction = direction3;
                //chestType = direction3.rotateYCounterclockwise() == direction2.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }

        // if (chestType == ChestType.SINGLE && !bl) {
        //     if (direction == this.getNeighborChestDirection(ctx, direction.rotateYClockwise())) {
        //         chestType = ChestType.LEFT;
        //     } else if (direction == this.getNeighborChestDirection(ctx, direction.rotateYCounterclockwise())) {
        //         chestType = ChestType.RIGHT;
        //     }
        // }

        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction)).setValue(CHEST_TYPE, chestType)).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    private Direction getNeighborChestDirection(BlockPlaceContext ctx, Direction dir) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(dir));
        return blockState.is(this) && blockState.getValue(CHEST_TYPE) == ChestType.SINGLE ? (Direction)blockState.getValue(FACING) : null;
    }

    // 1.21: ItemStack.hasCustomHoverName() and ChestBlockEntity.setCustomName() are gone.
    // The custom name now comes from DataComponents.CUSTOM_NAME and is applied by
    // BaseContainerBlockEntity.applyImplicitComponents() on placement, like vanilla chests.

    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Container) {
                Containers.dropContents(world, pos, (Container)blockEntity);
                world.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, world, pos, newState, moved);
        }
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            MenuProvider namedScreenHandlerFactory = this.getMenuProvider(state, world, pos);
            if (namedScreenHandlerFactory != null) {
                player.openMenu(namedScreenHandlerFactory);
                player.awardStat(this.getOpenStat());
                PiglinAi.angerNearbyPiglins(player, true);
            }

            return InteractionResult.CONSUME;
        }
    }

    protected Stat<ResourceLocation> getOpenStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }

    public BlockEntityType<? extends ChestBlockEntity> getExpectedEntityType() {
        return (BlockEntityType)this.entityTypeRetriever.get();
    }

    @Nullable
    public static Container getInventory(BlockSlateChest block, BlockState state, Level world, BlockPos pos, boolean ignoreBlocked) {
        return (Container)((Optional)block.getBlockEntitySource(state, world, pos, ignoreBlocked).apply(MixinWeirdChestStatics.getInventoryRetriever())).orElse((Object)null);
    }

    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> getBlockEntitySource(BlockState state, Level world, BlockPos pos, boolean ignoreBlocked) {
        BiPredicate<LevelAccessor, BlockPos> biPredicate;
        if (ignoreBlocked) {
            biPredicate = (worldx, posx) -> {
                return false;
            };
        } else {
            biPredicate = BlockSlateChest::isChestBlocked;
        }

        return DoubleBlockCombiner.combineWithNeigbour((BlockEntityType)this.entityTypeRetriever.get(), BlockSlateChest::getDoubleBlockType, BlockSlateChest::getFacing, FACING, state, world, pos, biPredicate);
    }

    // based on https://github.com/Jiingy/Jineric-Mod/blob/93c23bc13b1b96468532109a33ef411b21cc8c72/src/main/java/jingy/jineric/block/JinericChestBlock.java#L112 since vanilla code wasn't available for god knows why
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> NAME_RETRIEVER = new DoubleBlockCombiner.Combiner<>() {
        // i don't know why it's from both. our chests don't do that,,
        public Optional<MenuProvider> acceptDouble(ChestBlockEntity chestBE, ChestBlockEntity chestBE2) {
            final Container inventory = new CompoundContainer(chestBE, chestBE2);
            return Optional.of(new MenuProvider() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                if(chestBE instanceof BlockEntitySlateChest slateChestBE && chestBE.canOpen(playerEntity)){
                    return slateChestBE.createMenu(i, playerInventory);
                }
                return null;
            }
                @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                if (chestBE.hasCustomName()) {
                    return chestBE.getDisplayName();
                } else {
                    BlockState blockState = chestBE.getBlockState();

                    return (chestBE2.hasCustomName() ? chestBE2.getDisplayName() : blockState.getBlock().getName());
                }
                }
            });
        }

        public Optional<MenuProvider> acceptSingle(ChestBlockEntity chestBlockEntity) {
            return Optional.of(chestBlockEntity);
        }
        public Optional<MenuProvider> acceptNone() {
            return Optional.empty();
        }
    };

    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return (MenuProvider)((Optional)this.getBlockEntitySource(state, world, pos, false).apply(MixinWeirdChestStatics.getNameRetriever())).orElse((Object)null);
    }

    public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> getAnimationProgressRetriever(LidBlockEntity progress) {
        return ChestBlock.opennessCombiner(progress);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntitySlateChest(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide && type == this.getExpectedEntityType() ? BlockSlateChest::clientTick : null;
    }

    // this wants it to pass in any block entity
    public static void clientTick(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if(blockEntity instanceof ChestBlockEntity cbe){
            ChestBlockEntity.lidAnimateTick(world, pos, state, cbe);
        }
     }

    public static boolean isChestBlocked(LevelAccessor world, BlockPos pos) {
        return hasBlockOnTop(world, pos) || hasCatOnTop(world, pos);
    }

    private static boolean hasBlockOnTop(BlockGetter world, BlockPos pos) {
        BlockPos blockPos = pos.above();
        return world.getBlockState(blockPos).isRedstoneConductor(world, blockPos);
    }

    private static boolean hasCatOnTop(LevelAccessor world, BlockPos pos) {
        List<Cat> list = world.getEntitiesOfClass(Cat.class, new AABB((double)pos.getX(), (double)(pos.getY() + 1), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 2), (double)(pos.getZ() + 1)));
        if (!list.isEmpty()) {
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
                Cat catEntity = (Cat)var3.next();
                if (catEntity.isInSittingPose()) {
                return true;
                }
            }
        }

        return false;
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(getInventory(this, state, world, pos, false));
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, CHEST_TYPE, WATERLOGGED, ENERGIZED});
    }

    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockEntity).recheckOpen();
        }

    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        CHEST_TYPE = BlockStateProperties.CHEST_TYPE;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        DOUBLE_NORTH_SHAPE = Block.box(1.0, 0.0, 0.0, 15.0, 14.0, 15.0);
        DOUBLE_SOUTH_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 16.0);
        DOUBLE_WEST_SHAPE = Block.box(0.0, 0.0, 1.0, 15.0, 14.0, 15.0);
        DOUBLE_EAST_SHAPE = Block.box(1.0, 0.0, 1.0, 16.0, 14.0, 15.0);
        SINGLE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    }
}
