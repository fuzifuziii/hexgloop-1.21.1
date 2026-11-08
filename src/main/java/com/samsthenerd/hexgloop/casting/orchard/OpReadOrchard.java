package com.samsthenerd.hexgloop.casting.orchard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;

public class OpReadOrchard implements ConstMediaAction{
    private boolean asList;

    public OpReadOrchard(boolean asList){
        this.asList = asList;
    }

    @Override
    public int getArgc(){ return 0;}

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        ServerPlayer player = (ServerPlayer) context.getCastingEntity();
        if(player == null) return new ArrayList<Iota>();
        if(asList){
            List<Double> orchardList = ((IOrchard)player).getOrchardList();
            List<Iota> iotadList = orchardList.stream().map(val -> new DoubleIota(val)).collect(Collectors.toList());
            ListIota orchardIotaList = new ListIota(iotadList);
            return List.of(orchardIotaList);
        } else {
            return List.of(new DoubleIota(((IOrchard)player).getOrchardValue()));
        }
    }

    
}
