package com.samsthenerd.hexgloop.casting.mirror;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

public class MishapICanOnlyCodeSoMuchPlsDontDupe extends Mishap{
    public MishapICanOnlyCodeSoMuchPlsDontDupe() {
        super();
    }

    @NotNull
    public FrozenPigment accentColor(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(errorCtx, "errorCtx");
        return this.dyeColor(DyeColor.BROWN);
    }

    public TreeList<Iota> execute(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx, @NotNull TreeList<Iota> stack) {
        return stack;
    }

    @NotNull
    public Component errorMessage(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx) {
        Component errorText;
        Object[] errorArgs;
            errorArgs = new Object[]{this.actionName(errorCtx.getName())};
            errorText = this.error("mirror_limits", errorArgs);
        return errorText;
    }
}
