package com.samsthenerd.hexgloop.casting.gloopifact;


import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.items.ItemGloopifact;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem;

public class OpWriteGloopifact implements ConstMediaAction {
    public static List<String> expectedSources = List.of("gloopifact");
    private boolean simulate;
    
    public OpWriteGloopifact(boolean simulate){
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
        Tuple<ItemStack, ItemGloopifact> gloopifactLore = GloopifactUtils.assertGloopcasting(context);
        ItemStack castHandStack = gloopifactLore.getA();
        ItemGloopifact gloopifactItem = gloopifactLore.getB();

        Iota iota = args.get(0);

        boolean canWrite = gloopifactItem.canWrite(castHandStack, iota);
        if(simulate) return List.of(new BooleanIota(canWrite));
        if(!canWrite){
            MishapThrowerWrapper.throwMishap(MishapBadOffhandItem.of(castHandStack, "iota.write"));
            return List.of();
        }
        gloopifactItem.writeDatum(castHandStack, iota);
        return new ArrayList<>();
    }
   
}