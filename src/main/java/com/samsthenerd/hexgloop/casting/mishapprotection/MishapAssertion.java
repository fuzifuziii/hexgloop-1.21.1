package com.samsthenerd.hexgloop.casting.mishapprotection;

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

public class MishapAssertion extends Mishap {
    Component errorLabel = null;

    public MishapAssertion(Component errorLabel) {
        super();
        this.errorLabel = errorLabel;
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

    public Component makeError(Component actionName, Component label){
        Object[] errorArgs;
        if(label == null){
            errorArgs = new Object[]{actionName};
            return this.error("assertion", errorArgs);
        } else {
            errorArgs = new Object[]{actionName, label};
            return this.error("assertion.labeled", errorArgs);
        }
    }

    @NotNull
    public Component errorMessage(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx) {
        return this.makeError(this.actionName(errorCtx.getName()), errorLabel);
    }

}