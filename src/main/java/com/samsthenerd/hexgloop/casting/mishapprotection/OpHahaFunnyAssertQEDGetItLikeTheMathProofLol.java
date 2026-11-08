package com.samsthenerd.hexgloop.casting.mishapprotection;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;

import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;

public class OpHahaFunnyAssertQEDGetItLikeTheMathProofLol implements Action {

    public OpHahaFunnyAssertQEDGetItLikeTheMathProofLol(){
    }

    @Override
    public OperationResult operate(CastingEnvironment env, CastingImage image, SpellContinuation continuation){
        TreeList<Iota> stack = image.getStack();
        if(stack.size() < 1){
            MishapThrowerWrapper.throwMishap(new MishapNotEnoughArgs(1, 0));
        }
        Iota firstArg = stack.get(stack.size()-1);
        stack = stack.init(); // remove the first iota, whatever it is
        Component mishapLabelText = null;
        boolean shouldMishap = false;
        if(firstArg instanceof BooleanIota bIota){
            shouldMishap = !bIota.getBool();
        } else {
            mishapLabelText = firstArg.display();
            if(stack.size() < 1){
                MishapThrowerWrapper.throwMishap(new MishapNotEnoughArgs(2, 1));
            }
            Iota secondArg = stack.get(stack.size()-1);
            stack = stack.init(); // remove the second iota too if we're checking it
            if(secondArg instanceof BooleanIota bIota2){
                shouldMishap = !bIota2.getBool();
            } else {
                MishapThrowerWrapper.throwMishap(MishapInvalidIota.ofType(secondArg, 0, "boolean"));
            }
        }
        if(shouldMishap){
            MishapThrowerWrapper.throwMishap(new MishapAssertion(mishapLabelText));
        }
        return MishapThrowerWrapper.result(MishapThrowerWrapper.withStack(image, stack), continuation);
    }
}
