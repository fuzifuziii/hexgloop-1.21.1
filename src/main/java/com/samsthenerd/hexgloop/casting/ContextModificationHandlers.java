package com.samsthenerd.hexgloop.casting;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiFunction;
import net.minecraft.data.models.blockstates.PropertyDispatch.TriFunction;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import com.samsthenerd.hexgloop.blocks.BlockSentinelBed;
import com.samsthenerd.hexgloop.items.ItemCopingSaw;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

// registry for various context modifiers, handled by mixins
public class ContextModificationHandlers {
    public static Comparator<Tuple<?, Integer>> comparator = (Tuple<?, Integer> a, Tuple<?, Integer> b) -> {
        return b.getB().compareTo(a.getB());
    };

    public static Queue<Tuple<BiFunction<CastingEnvironment, Boolean, Modification>, Integer>> ENLIGHTENMENT_MODIFIERS = new PriorityQueue<>(comparator);
    public static Queue<Tuple<BiFunction<CastingEnvironment, Boolean, Modification>, Integer>> OVERCAST_MODIFIERS = new PriorityQueue<>(comparator);
    public static Queue<Tuple<TriFunction<CastingEnvironment, Vec3, Boolean, Modification>, Integer>> AMBIT_MODIFIERS = new PriorityQueue<>(comparator);

    public static enum Modification {
        NONE,
        ENABLE,
        DISABLE
    }

    public static void registerEnlightenmentModifier(BiFunction<CastingEnvironment, Boolean, Modification> modifier, int ordering){
        ENLIGHTENMENT_MODIFIERS.add(new Tuple<>(modifier, ordering));
    }

    public static void registerOvercastModifier(BiFunction<CastingEnvironment, Boolean, Modification> modifier, int ordering){
        OVERCAST_MODIFIERS.add(new Tuple<>(modifier, ordering));
    }

    public static void registerAmbitModifier(TriFunction<CastingEnvironment, Vec3, Boolean, Modification> modifier, int ordering){
        AMBIT_MODIFIERS.add(new Tuple<>(modifier, ordering));
    }

    public static void init(){
        registerAmbitModifier(BlockSentinelBed::ambitModifier, 0);
        registerOvercastModifier(ItemCopingSaw::overcastModifer, 100000);
    }
}
