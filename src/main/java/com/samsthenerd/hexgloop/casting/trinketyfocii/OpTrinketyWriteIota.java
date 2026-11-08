package com.samsthenerd.hexgloop.casting.trinketyfocii;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.lib.HexItems;

public class OpTrinketyWriteIota implements ConstMediaAction{
    private boolean simulate;
    private Function<CastingEnvironment, List<String>> decideTrinkets;

    // function so that it can do things like change based on which hand is being used
    // simulate so that we can stuff the auditor/accessor spells into these
    // translation key so we can name them externally
    public OpTrinketyWriteIota(Function<CastingEnvironment, List<String>> decideTrinkets, boolean simulate){
        this.decideTrinkets = decideTrinkets;
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
        Iota iotaArg = args.get(0); // assume argc will protect here?
        List<String> trinketsList = decideTrinkets.apply(context);
        if(trinketsList == null || trinketsList.size() == 0){ // shouldn't ever happen
            if(simulate) return List.of(iotaArg, new BooleanIota(false));
            return List.of(iotaArg);
        }
        Map<String, List<ItemStack>> trinketMap = HexGloop.TRINKETY_INSTANCE.getTrinkets(context.getCastingEntity());
        ItemStack foundStack = null;
        String foundTrinket = null;
        for(String trinket : trinketsList){
            if(trinketMap.containsKey(trinket)){
                List<ItemStack> stacks = trinketMap.get(trinket);
                for(ItemStack stack : stacks){
                    if(!stack.isEmpty() && (stack.getItem() instanceof IotaHolderItem)){
                        foundStack = stack;
                        foundTrinket = trinket;
                        break;
                    }
                }
                if(foundStack != null) break;
            }
        }
        if(foundStack == null){
            if(simulate) return List.of(iotaArg, new BooleanIota(false));
            MishapThrowerWrapper.throwMishap(MishapBadTrinketyItem.of(ItemStack.EMPTY, trinketsList.get(0), "iota.write", new Object[0]));
            return List.of(iotaArg); // doesn't actually return but, y'know, mishaps
        }
        boolean canWrite = HexItems.FOCUS.get().canWrite(foundStack, iotaArg);
        if(simulate) return List.of(iotaArg, new BooleanIota(canWrite));
        if(!canWrite){
            MishapThrowerWrapper.throwMishap(MishapBadTrinketyItem.of(foundStack, foundTrinket, "iota.readonly", iotaArg.display()));
            return List.of(iotaArg);
        }
        HexItems.FOCUS.get().writeDatum(foundStack, iotaArg);
        return List.of();
    }

    
}