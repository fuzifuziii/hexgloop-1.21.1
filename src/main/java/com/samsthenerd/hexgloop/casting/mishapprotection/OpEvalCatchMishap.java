package com.samsthenerd.hexgloop.casting.mishapprotection;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.mojang.datafixers.util.Either;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;

import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate;
import at.petrak.hexcasting.api.casting.eval.vm.FrameFinishEval;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.network.chat.Component;

public class OpEvalCatchMishap implements Action {
   @NotNull
   public static final OpEvalCatchMishap INSTANCE = new OpEvalCatchMishap();

   public static FrameFinishEval CATCHY_INSTANCE = ((ICatchyFrameEval)(Object)
        ((ICatchyFrameEval)(Object)(FrameFinishEval.INSTANCE)).initFromInstance()).setCatchy(true);

   private OpEvalCatchMishap() {
   }

   // mostly yoinked from OpEval
   @Override
   public OperationResult operate(@NotNull CastingEnvironment env, @NotNull CastingImage image, @NotNull SpellContinuation continuation) {
        TreeList<Iota> stack = image.getStack();
        if (stack.isEmpty()) {
            MishapThrowerWrapper.throwMishap(new MishapNotEnoughArgs(1, 0));
            return null; // unreachable
        }
        Either<Iota, TreeList<Iota>> instrs = OperatorUtils.getEvaluatable(stack, stack.size() - 1, stack.size());
        TreeList<Iota> newStack = stack.init();
        // false if it's a single iota
        SpellContinuation newCont = !instrs.left().isPresent()
            && (
                !(continuation instanceof SpellContinuation.NotDone) // true only if it's done
                || !(((SpellContinuation.NotDone)continuation).getFrame() instanceof FrameFinishEval) // current frame isn't a finish frame
            )
            ? continuation.pushFrame(FrameFinishEval.INSTANCE) : continuation;
        newCont = newCont.pushFrame(CATCHY_INSTANCE);
        // wraps a single iota in a list or returns just the list
        TreeList<Iota> instrsList = instrs.map((single) -> TreeList.from(List.of(single)), (list) -> list);
        FrameEvaluate frame = new FrameEvaluate(instrsList, true);
        CastingImage newImage = MishapThrowerWrapper.withStack(image, newStack).withUsedOp();
        return new OperationResult(newImage, List.of(), newCont.pushFrame(frame), HexEvalSounds.HERMES.get());
   }
}
