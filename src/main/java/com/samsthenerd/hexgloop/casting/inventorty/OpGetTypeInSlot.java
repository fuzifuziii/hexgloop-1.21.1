package com.samsthenerd.hexgloop.casting.inventorty;

import java.util.List;
import net.minecraft.network.chat.Component;
import com.samsthenerd.hexgloop.casting.inventorty.InventortyUtils.GrabbableStack;
import com.samsthenerd.hexgloop.compat.hexal.HexalMaybeIotas;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import dev.architectury.platform.Platform;

// should be safe to include without mod loaded checks, but should probably only register it if it's loaded
public class OpGetTypeInSlot implements ConstMediaAction{

    public OpGetTypeInSlot(){
    }

    @Override
    public int getArgc(){ return 1; }

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        InventortyUtils.assertKittyCasting(context);
        GrabbableStack grabStack = InventortyUtils.getStackFromGrabbable(args.get(0), context, 0, getArgc());
        Iota typeIota = new NullIota();
        if(!grabStack.getStack().isEmpty() && Platform.isModLoaded("hexal")){
            typeIota = HexalMaybeIotas.getTypeIota(grabStack.getStack().getItem());
        }
        return List.of(typeIota);
    }

    
}




