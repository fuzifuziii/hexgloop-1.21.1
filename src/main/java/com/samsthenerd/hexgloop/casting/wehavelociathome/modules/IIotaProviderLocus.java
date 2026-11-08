package com.samsthenerd.hexgloop.casting.wehavelociathome.modules;

import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IIotaProviderLocus extends ILocusModule{
    // note: you can just return a HexPattern if you want to give a pattern but need the harness
    public Iota provideIota(BlockPos pos, BlockState bs, Level world, CastingVM harness);
}
