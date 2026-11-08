package com.samsthenerd.hexgloop.casting.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import ram.talia.moreiotas.api.casting.iota.StringIota;


// requires moreiotas and is better with hexal
public class OpGetTags implements ConstMediaAction{

    public OpGetTags(){

    }

    @Override
    public int getArgc(){ return 1;}

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        Iota inputIota = args.get(0);
        List<Iota> tagList = new ArrayList<>();
        for(String tag : TagUtils.getTagChecker(inputIota, context).getTags()){
            StringIota tagIota = null;
            try{
                tagIota = StringIota.make(tag);
                tagList.add(tagIota);
            } catch(MishapInvalidIota e){}
        }
        return List.of(new ListIota(tagList));
    }

    
}

