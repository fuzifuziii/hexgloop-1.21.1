package com.samsthenerd.hexgloop.blocks;

import com.samsthenerd.hexgloop.casting.ContextModificationHandlers.Modification;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

public class BlockSentinelBed extends Block implements ICatPost{
    public BlockSentinelBed (Properties settings){
        super(settings);
    }

    public static Modification ambitModifier(CastingEnvironment ctx, Vec3 pos, Boolean original){
        ServerPlayer caster = ctx.getCastingEntity() instanceof ServerPlayer sp ? sp : null;
        if(!original && caster != null){
            BlockState state = caster.serverLevel().getBlockState(BlockPos.containing(pos));
            if(state.getBlock() == HexGloopBlocks.SENTINEL_BED_BLOCK.get())
                return Modification.ENABLE;
        }
        return Modification.NONE;
    }
}
