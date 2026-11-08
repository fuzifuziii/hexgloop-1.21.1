package com.samsthenerd.hexgloop.casting.truenameclassaction;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.misc.worldData.AgreeTruenameEULAState;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class OpAgreeTruenameEULA implements ConstMediaAction {

    public static List<String> expectedSources = List.of("staff");

    public OpAgreeTruenameEULA(){
    }

    @Override
    public int getArgc(){ return 0;}

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        if(!(context instanceof StaffCastEnv)){ // if it's not a staff need to do special handling
            MishapThrowerWrapper.throwMishap(new MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh(expectedSources));
        }
        ServerPlayer player = (ServerPlayer) context.getCastingEntity();
        if(player == null) return new ArrayList<Iota>();
        AgreeTruenameEULAState eula = AgreeTruenameEULAState.getServerState(context.getWorld().getServer());
        // toggle it so you can unagree
        eula.setAgreement(player.getUUID(), !eula.checkAgreement(player.getUUID()));
        return new ArrayList<Iota>();
    }

}