package com.samsthenerd.hexgloop.casting.redstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.samsthenerd.hexgloop.blocks.BlockConjuredRedstone;
import com.samsthenerd.hexgloop.blocks.HexGloopBlocks;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.RenderedSpell;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import kotlin.Triple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class OpConjureRedstone implements SpellAction{
    private static net.minecraft.world.entity.player.Player castingPlayer(at.petrak.hexcasting.api.casting.eval.CastingEnvironment env){
        return env.getCastingEntity() instanceof net.minecraft.world.entity.player.Player p ? p : null;
    }

    private static final Random RANDOM = new Random();
    @Override
    public int getArgc(){ return 2;}

    @Override
    public boolean hasCastingSound(CastingEnvironment context){ return true;}

    @Override
    public boolean awardsCastingStat(CastingEnvironment context){ return true;}





    @Override
    public SpellAction.Result execute(List<? extends Iota> args, CastingEnvironment context){
        try{
        BlockPos pos = OperatorUtils.getBlockPos(args, 0, getArgc());
        context.assertPosInRange(pos);

        int power = OperatorUtils.getInt(args, 1, getArgc());



        if (!context.getWorld().mayInteract(castingPlayer(context), pos))
            return null;

        BlockPlaceContext placeContext = new BlockPlaceContext(context.getWorld(), castingPlayer(context), 
            context.getCastingHand(), ItemStack.EMPTY, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        BlockState worldState = context.getWorld().getBlockState(pos);
        if (!worldState.canBeReplaced(placeContext))
            // return null;
            throw MishapBadBlock.of(pos, "replaceable");

        List<ParticleSpray> particles = new ArrayList<>();
        particles.add(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0, 1));

        return new SpellAction.Result(
            new Spell(pos, power),
            MediaConstants.DUST_UNIT,
            particles
        , 1L);
        } catch (MishapBadBlock e){
            MishapThrowerWrapper.throwMishap(e);
            return null;
        }
    }

    private class Spell implements RenderedSpell {
        BlockPos pos;
        int power;
        public Spell(BlockPos pos, int power) {
            this.pos = pos;
            this.power = power;
        }

        @Override
        public void cast(CastingEnvironment context) {
            if (!context.canEditBlockAt(pos))
                return;
            
            Block block = HexGloopBlocks.CONJURED_REDSTONE_BLOCK.get();

            FrozenPigment colorizer = context.getPigment();

            ItemStack conjuredRSItemStack = new ItemStack(HexGloopBlocks.CONJURED_REDSTONE_BLOCK.get().asItem());

            BlockPlaceContext placeContext = new BlockPlaceContext(context.getWorld(), castingPlayer(context), 
                context.getCastingHand(), conjuredRSItemStack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));


            BlockState worldState = context.getWorld().getBlockState(pos);
            if (worldState.canBeReplaced(placeContext)) {
                if (!IXplatAbstractions.INSTANCE.isPlacingAllowed(context.getWorld(), pos, new ItemStack(HexGloopBlocks.CONJURED_REDSTONE_BLOCK.get().asItem()), castingPlayer(context)))
                    return;

                BlockState state = block.getStateForPlacement(placeContext);
                if (state != null) {
                    context.getWorld().setBlockAndUpdate(pos, state);

                    if (context.getWorld().getBlockState(pos).getBlock() instanceof BlockConjuredRedstone) {
                        BlockConjuredRedstone.setColor(context.getWorld(), pos, colorizer);
                        BlockConjuredRedstone.setPower(context.getWorld(), pos, power);
                    }
                }
            }
        }
    }

    
}