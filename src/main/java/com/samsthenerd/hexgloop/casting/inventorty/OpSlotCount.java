package com.samsthenerd.hexgloop.casting.inventorty;

import java.util.List;
import net.minecraft.network.chat.Component;
import com.samsthenerd.hexgloop.casting.inventorty.InventortyUtils.GrabbableStack;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class OpSlotCount implements ConstMediaAction{

    boolean getActual = true;

    public OpSlotCount(boolean getActual){
        this.getActual = getActual;
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
        int maxCount = grabStack.getMaxCount();
        if(grabStack.getStack() != null && !grabStack.getStack().isEmpty())
            maxCount = Math.min(maxCount, grabStack.getStack().getMaxStackSize());
        return List.of(new DoubleIota(getActual ? grabStack.getStack().getCount() : maxCount));
    }

    
}




