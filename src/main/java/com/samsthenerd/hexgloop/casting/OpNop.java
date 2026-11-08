package com.samsthenerd.hexgloop.casting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class OpNop implements ConstMediaAction {
    public OpNop(){
    }

    @Override
    public int getArgc(){ return 0;}

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        // literally nothing
        return new ArrayList<Iota>();
    }

}

