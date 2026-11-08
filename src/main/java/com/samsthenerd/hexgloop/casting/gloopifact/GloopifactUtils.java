package com.samsthenerd.hexgloop.casting.gloopifact;

import java.util.List;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.casting.truenameclassaction.HexalWispWrapper;
import com.samsthenerd.hexgloop.casting.truenameclassaction.MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh;
import com.samsthenerd.hexgloop.items.ItemGloopifact;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import dev.architectury.platform.Platform;

public class GloopifactUtils {
    public static List<String> expectedSources = List.of("gloopifact");

    public static Tuple<ItemStack, ItemGloopifact> assertGloopcasting(CastingEnvironment context){
        ItemStack castHandStack = context.getCastingEntity().getItemInHand(context.getCastingHand());
        // make sure we're casting from atleast some packaged hex
        if(!(context instanceof PackagedItemCastEnv)
        || (Platform.isModLoaded("hexal") && HexalWispWrapper.isWisp(context))
        || !(castHandStack.getItem() instanceof ItemGloopifact gloopifactItem)){
            MishapThrowerWrapper.throwMishap(new MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh(expectedSources));
            return null;
        }
        return new Tuple<>(castHandStack, gloopifactItem);
    }
}
