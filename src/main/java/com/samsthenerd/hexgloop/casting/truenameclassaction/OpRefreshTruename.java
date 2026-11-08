package com.samsthenerd.hexgloop.casting.truenameclassaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.misc.worldData.TruenameLockState;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import dev.architectury.platform.Platform;

public class OpRefreshTruename implements ConstMediaAction {

    public static List<String> expectedSources = List.of("staff", "packaged_hex");

    public OpRefreshTruename(){
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
            if(!(context instanceof PackagedItemCastEnv)){
                // throw mishap, it's something else
                MishapThrowerWrapper.throwMishap(new MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh(expectedSources));
            } else {
                // need to check if it's a wisp or not
                if(Platform.isModLoaded("hexal")){
                    if(HexalWispWrapper.isWisp(context)){
                        // it's a wisp so don't let this cast it
                        MishapThrowerWrapper.throwMishap(new MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh(expectedSources));
                    }
                }
            }

        }
        ServerPlayer player = (ServerPlayer) context.getCastingEntity();
        if(player == null) return new ArrayList<Iota>();
        UUID newUuid = UUID.randomUUID();
        TruenameLockState.getServerState(context.getWorld().getServer()).setLockUUID(player.getUUID(), newUuid);
        return new ArrayList<Iota>();
    }

}