package com.samsthenerd.hexgloop.blocks;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import com.samsthenerd.hexgloop.blockentities.BlockEntityConjuredRedstone;
import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.blocks.BlockConjured;

public class BlockConjuredRedstone extends BlockConjured{

    public BlockConjuredRedstone(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        Optional<BlockEntityConjuredRedstone> maybeBE = world.getBlockEntity(pos, HexGloopBEs.CONJURED_REDSTONE_BE.get());
        if(maybeBE.isPresent()){
            return maybeBE.get().getPower();
        }
        return 0;
    }

    // yoinked from BlockConjured -- need to override stuff to use our BE

    @Override
    public BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        BlockState result = super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        // For some reason the block doesn't play breaking noises. So we fix that!
        pPlayer.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1f, 1f);
        return result;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState,
        BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return BlockConjuredRedstone::tick;
        } else {
            return null;
        }
    }

    private static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (t instanceof BlockEntityConjuredRedstone conjured) {
            conjured.particleEffect();
        }
    }

    @Override
    public void stepOn(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Entity pEntity) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile instanceof BlockEntityConjuredRedstone bec) {
            bec.walkParticle(pEntity);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new BlockEntityConjuredRedstone(pPos, pState);
    }

    public static void setColor(Level world, BlockPos pPos, FrozenPigment colorizer) {
        BlockEntity blockentity = world.getBlockEntity(pPos);
        if (blockentity instanceof BlockEntityConjuredRedstone tile) {
            tile.setColorizer(colorizer);
        }
    }

    public static void setPower(Level world, BlockPos pPos, int power) {
        BlockEntity blockentity = world.getBlockEntity(pPos);
        if (blockentity instanceof BlockEntityConjuredRedstone tile) {
            tile.setPower(power);
            world.updateNeighborsAt(pPos, world.getBlockState(pPos).getBlock());
        }
    }

    @SoftImplement("forge")
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2,
                                     LivingEntity entity, int numberOfParticles) {
        return addLandingEffects(state1, worldserver, pos, entity, numberOfParticles);
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerLevel worldserver, BlockPos pos,
                                     LivingEntity entity, int numberOfParticles) {
        BlockEntity tile = worldserver.getBlockEntity(pos);
        if (tile instanceof BlockEntityConjuredRedstone bec) {
            bec.landParticle(entity, numberOfParticles);
        }
        return true;
    }
}