package com.samsthenerd.hexgloop.mixins.mishapprotection;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.casting.mishapprotection.ICatchyFrameEval;

import at.petrak.hexcasting.api.casting.eval.vm.FrameFinishEval;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import kotlin.Pair;

@Mixin(FrameFinishEval.class)
public class MixinCatchyFrameEval implements ICatchyFrameEval {
    private boolean catchy = false;

    @Invoker("<init>")
    static FrameFinishEval init() {
        throw new AssertionError();
    }

    public FrameFinishEval initFromInstance(){
        return init();
    }

    public boolean isCatchy(){
        return catchy;
    }

    public FrameFinishEval setCatchy(boolean catchy){
        this.catchy = catchy;
        return (FrameFinishEval)(Object)this;
    }

    @Inject(method="breakDownwards(Lat/petrak/hexcasting/api/utils/TreeList;)Lkotlin/Pair;", at=@At("HEAD"), cancellable=true, remap = false)
    public void cancelBreakDownwardsForCatchy(at.petrak.hexcasting.api.utils.TreeList<Iota> stack, CallbackInfoReturnable<Pair<Boolean, at.petrak.hexcasting.api.utils.TreeList<Iota>>> cir){
        if(isCatchy()){
            cir.setReturnValue(new Pair<>(false, stack));
        }
    }

    // TODO(1.21 port): evaluate() no longer calls CollectionsKt.toList, so the "push false for catchy frames"
    // behaviour has to be re-implemented against CastingImage/CastResult.
}
