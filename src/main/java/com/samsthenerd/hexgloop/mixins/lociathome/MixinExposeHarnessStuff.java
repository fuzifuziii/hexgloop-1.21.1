package com.samsthenerd.hexgloop.mixins.lociathome;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json - CastingVM#handleParentheses is gone in Hex 0.12;
// parenthesis handling moved into the eval frames. No callers of this invoker remain in the codebase.

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.vm.FunctionalData;
import at.petrak.hexcasting.api.casting.iota.Iota;
import kotlin.Pair;

@Mixin(CastingVM.class)
public interface MixinExposeHarnessStuff {
    @Invoker(value="handleParentheses", remap = false)
    public Pair<FunctionalData, ResolvedPatternType> invokehandleParentheses(Iota iota);
}
