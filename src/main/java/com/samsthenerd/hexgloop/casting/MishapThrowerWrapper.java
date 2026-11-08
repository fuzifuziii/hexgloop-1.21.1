package com.samsthenerd.hexgloop.casting;

import at.petrak.hexcasting.api.casting.mishaps.Mishap;

/*
 * Mishap is an unchecked exception in Hex 0.12, so this is just a thin helper now.
 */
public class MishapThrowerWrapper {
    public static void throwMishap(Mishap mishap){
        throw mishap;
    }

    /** CastingImage with only the stack replaced (everything else kept). */
    public static at.petrak.hexcasting.api.casting.eval.vm.CastingImage withStack(
            at.petrak.hexcasting.api.casting.eval.vm.CastingImage image,
            at.petrak.hexcasting.api.utils.TreeList<at.petrak.hexcasting.api.casting.iota.Iota> stack){
        return image.copy(stack, image.getParenCount(), image.getParenthesized(), image.getEscapeNext(),
            image.getSimulateNext(), image.getOpsConsumed(), image.getComponents());
    }

    public static at.petrak.hexcasting.api.casting.eval.OperationResult result(
            at.petrak.hexcasting.api.casting.eval.vm.CastingImage image,
            at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation continuation){
        return new at.petrak.hexcasting.api.casting.eval.OperationResult(image.withUsedOp(), java.util.List.of(), continuation,
            at.petrak.hexcasting.common.lib.hex.HexEvalSounds.NORMAL_EXECUTE.get());
    }
}
