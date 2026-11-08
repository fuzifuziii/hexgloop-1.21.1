package com.samsthenerd.hexgloop.casting.truenameclassaction;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.items.ItemGloopifact;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import dev.architectury.platform.Platform;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

// otherwise known as MishapBadCastingSource
public class MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh extends Mishap {
    private List<String> acceptedSources;

    // expects the list to match the source name suffixes from sourceFromCtx
    public MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh(List<String> acceptedSources) {
        super();
        this.acceptedSources = acceptedSources;
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

    public static String sourceFromCtx(CastingEnvironment ctx){
        if(((IContextHelper)(Object)ctx).isKitty()){
            return "inventorty";
        }
        if(ctx instanceof StaffCastEnv){
            return "staff";
        } else if(ctx instanceof CircleCastEnv){
            return "circle";
        } else if(ctx instanceof PackagedItemCastEnv){
            if(Platform.isModLoaded("hexal")){
                if(HexalWispWrapper.isWisp(ctx)){
                    return "wisp";
                }
            }
            ItemStack castHandStack = ctx.getCastingEntity().getItemInHand(ctx.getCastingHand());
            if(castHandStack.getItem() instanceof ItemGloopifact gloopifactItem){
                return "gloopifact";
            }
            return "packaged_hex";
        }
        return "unknown";
    }

    private Component expectedSourcesList(){
        MutableComponent list = Component.literal("");
        int i = 0;
        for(String source : this.acceptedSources){
            list.append(Component.translatable("hexgloop.source_type."+source));
            i++;
            if(i != this.acceptedSources.size()){
                list.append(Component.literal(", "));
            }
        }
        return list;
    }

    @NotNull
    public Component errorMessage(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx) {
        Component errorText;
        Object[] errorArgs;
            errorArgs = new Object[]{this.actionName(errorCtx.getName()), expectedSourcesList(), Component.translatable("hexgloop.source_type."+sourceFromCtx(ctx))};
            errorText = this.error("wrong_casting_source" + (acceptedSources.size() > 1 ? "s" : "" ), errorArgs);
        return errorText;
    }
}
