package com.samsthenerd.hexgloop.casting.redstone;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class OpGetComparator implements ConstMediaAction {
    private boolean onlyRedstone;

    // if onlyRedstone it will only read actual redstone power, not comparator values
    public OpGetComparator(boolean onlyRedstone){
        this.onlyRedstone = onlyRedstone;
    }

    @Override
    public int getArgc(){ return 1;}

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        BlockPos pos = OperatorUtils.getBlockPos(args, 0, getArgc());
        context.assertPosInRange(pos);
        int power = context.getWorld().getBestNeighborSignal(pos);
        if(!onlyRedstone){
            BlockState state = context.getWorld().getBlockState(pos);
            if(state.hasAnalogOutputSignal()){
                power = state.getAnalogOutputSignal(context.getWorld(), pos);
            }
        }
        return List.of(new DoubleIota(power));
    }

}
