package com.samsthenerd.hexgloop.casting.truenameclassaction;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public class MishapClearedTruename extends Mishap {
    Player refdPlayer;

    // expects the list to match the source name suffixes from sourceFromCtx
    public MishapClearedTruename(Player refdPlayer) {
        super();
        this.refdPlayer = refdPlayer;
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

    public Component makeError(Component actionName, Player player){
        Object[] errorArgs;
        errorArgs = new Object[]{actionName, player.getName()};
        return this.error("cleared_truename", errorArgs);
    }

    @NotNull
    public Component errorMessage(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx) {
        return this.makeError(this.actionName(errorCtx.getName()), refdPlayer);
    }

}