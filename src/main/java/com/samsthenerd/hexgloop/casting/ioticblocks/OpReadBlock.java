package com.samsthenerd.hexgloop.casting.ioticblocks;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import com.samsthenerd.hexgloop.blocks.iotic.IoticHandler;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock;

public class OpReadBlock implements ConstMediaAction {
    private boolean simulate;

    public OpReadBlock(boolean simulate){
        this.simulate = simulate;
    }

    @Override
    public int getArgc(){ return 1;}

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){

        BlockPos blockPos = OperatorUtils.getBlockPos(args, 0, getArgc());

        ADIotaHolder holder = IoticHandler.findIotaHolder(context.getWorld(), blockPos);
        if(holder == null){
            if(simulate){
                return List.of(new BooleanIota(false));
            } else {
                MishapThrowerWrapper.throwMishap(MishapBadBlock.of(blockPos, "iota.read"));
                return new ArrayList<>();
            }
        }
        Iota iota = holder.readIota();

        boolean canRead = iota != null;
        if(simulate) return List.of(new BooleanIota(canRead));
        if(!canRead){
            MishapThrowerWrapper.throwMishap(MishapBadBlock.of(blockPos, "iota.read"));
            return new ArrayList<>();
        }
        return List.of(iota);
    }
   
}
