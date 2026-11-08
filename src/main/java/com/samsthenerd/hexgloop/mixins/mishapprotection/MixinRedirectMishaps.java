package com.samsthenerd.hexgloop.mixins.mishapprotection;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.casting.mishapprotection.ICatchyFrameEval;
import com.samsthenerd.hexgloop.casting.mishapprotection.IMishapStorage;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;

@Mixin(CastingVM.class)
public class MixinRedirectMishaps {

    // 0.12: there is no executeIotas/ControllerInfo anymore. queueExecuteAndWrapIotas is the
    // driver loop; it calls ContinuationFrame.evaluate(next, world, this) once per frame and
    // then performSideEffects(result.getSideEffects()). Mishaps do not throw out of executeInner
    // either - they come back as an OperatorSideEffect.DoMishap in the CastResult, which carries
    // the real Mishap.Context (pattern + action name). So wrap that evaluate call: intercept the
    // DoMishap before the driver performs it (performing it would run Mishap.execute on the stack).
    @WrapOperation(method = "queueExecuteAndWrapIotas(Ljava/util/List;Lnet/minecraft/server/level/ServerLevel;)Lat/petrak/hexcasting/api/casting/eval/ExecutionClientView;",
    at = @At(value = "INVOKE", target = "Lat/petrak/hexcasting/api/casting/eval/vm/ContinuationFrame;evaluate(Lat/petrak/hexcasting/api/casting/eval/vm/SpellContinuation;Lnet/minecraft/server/level/ServerLevel;Lat/petrak/hexcasting/api/casting/eval/vm/CastingVM;)Lat/petrak/hexcasting/api/casting/eval/CastResult;"))
    public CastResult executeIotasWrap(ContinuationFrame frame, SpellContinuation spCont, ServerLevel sWorld, CastingVM harness,
    Operation<CastResult> original) {
        Mishap caughtMishap = null;
        Mishap.Context mishapCtx = null;
        CastResult result = null;
        try{
            result = original.call(frame, spCont, sWorld, harness);
            if(result != null){
                for(OperatorSideEffect sideEffect : result.getSideEffects()){
                    if(sideEffect instanceof OperatorSideEffect.DoMishap mishapEffect){
                        caughtMishap = mishapEffect.getMishap();
                        mishapCtx = mishapEffect.getErrorCtx();
                        break;
                    }
                }
            }
        } catch (Throwable e){
            if(e instanceof Mishap mishap){
                caughtMishap = mishap;
            } else {
                throw e;
            }
        }
        if(caughtMishap == null){
            return result;
        }
        // mishap, need to handle it. IMishapStorage lives on the CastingEnvironment
        // (MixinStoreLastMishapContext), and Mishap.errorMessage is protected in 0.12 -
        // errorMessageWithName(env, ctx) is the public entry point.
        CastingEnvironment env = harness.getEnv();
        HexPattern pattern = mishapCtx != null ? mishapCtx.getPattern() : null;
        Component operatorName = mishapCtx != null ? mishapCtx.getName() : null;
        Mishap.Context ctxForMessage = mishapCtx != null ? mishapCtx
            : new Mishap.Context(pattern != null ? pattern : HexPattern.fromAngleString("", HexDir.WEST), operatorName);
        ((IMishapStorage)(Object)env).setLastMishap(caughtMishap.errorMessageWithName(env, ctxForMessage));
        // want to go back until we can find a catchy frame
        boolean done = false;
        SpellContinuation newCont;
        for(newCont = spCont; !done && newCont instanceof SpellContinuation.NotDone; newCont = ((SpellContinuation.NotDone)newCont).getNext()) {
            ContinuationFrame cFrame = ((SpellContinuation.NotDone)newCont).getFrame();
            if(((Object)cFrame) instanceof ICatchyFrameEval cEval) {
                done = cEval.isCatchy();
            }
        }
        if(done){ // found a catchy frame to back up to: push true and resume there, without the DoMishap side effect
            CastingImage newImage = result != null && result.getNewData() != null ? result.getNewData() : harness.getImage();
            TreeList<Iota> stack = newImage.getStack().appended(new BooleanIota(true));
            newImage = MishapThrowerWrapper.withStack(newImage, stack);
            return new CastResult(
                result != null ? result.getCast() : null,
                newCont,
                newImage,
                List.of(),
                ResolvedPatternType.EVALUATED,
                HexEvalSounds.NOTHING.get()
            );
        } else { // no catchy frame to back up to
            MishapThrowerWrapper.throwMishap(caughtMishap);
            return null; // unreachable
        }
    }
}
