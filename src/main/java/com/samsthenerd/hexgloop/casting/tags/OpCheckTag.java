package com.samsthenerd.hexgloop.casting.tags;

import java.util.List;
import net.minecraft.network.chat.Component;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import ram.talia.moreiotas.api.OperatorUtilsKt;


// requires moreiotas and is better with hexal
public class OpCheckTag implements ConstMediaAction{

    public OpCheckTag(){

    }

    @Override
    public int getArgc(){ return 2;}

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        Iota inputIota = args.get(0);
        String tag = OperatorUtilsKt.getString(args, 1, getArgc());
        boolean hasTag = TagUtils.getTagChecker(inputIota, context).hasTag(tag);
        return List.of(new BooleanIota(hasTag));
    }

    
}

