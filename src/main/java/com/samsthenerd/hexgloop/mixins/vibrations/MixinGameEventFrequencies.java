package com.samsthenerd.hexgloop.mixins.vibrations;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json - SculkSensorBlock.FREQUENCIES is gone;
// frequencies are data-driven now (see HexGloopGameEvents.addGameEventFreq).

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.gameevent.GameEvent;

// register all our frequencies
@Mixin(SculkSensorBlock.class)
public interface MixinGameEventFrequencies {
    // @ModifyArg(method = "<clinit>", 
    //     at= @At(value = "INVOKE", opcode = Opcodes.INVOKESTATIC, target = "it/unimi/dsi/fastutil/objects/Object2IntMaps.unmodifiable (Lit/unimi/dsi/fastutil/objects/Object2IntMap;)Lit/unimi/dsi/fastutil/objects/Object2IntMap;"))
    // private static Object2IntMap<GameEvent> modifyVibrationFreqsMap(Object2IntMap<GameEvent> map) {
    //     HexGloop.logPrint("try to add freqs");
    //     for(Supplier<GameEvent> ge : HexGloopGameEvents.ourFreqs.keySet()){
    //         map.put(ge.get(), HexGloopGameEvents.ourFreqs.getInt(ge));
    //     }
    //     return map;
    // }

    // make it a mutable map
    // @Redirect(method = "<clinit>", at= @At(value = "INVOKE_ASSIGN", 
    //     target="it/unimi/dsi/fastutil/objects/Object2IntMaps.unmodifiable (Lit/unimi/dsi/fastutil/objects/Object2IntMap;)Lit/unimi/dsi/fastutil/objects/Object2IntMap;"))
    // private static Object2IntMap<GameEvent> modifyVibrationFreqsMap(Object2IntMap<GameEvent> map) {
    //     return map;
        // Object2IntMap<GameEvent> openMap = new Object2IntOpenHashMap<GameEvent>();
        // for(GameEvent ge : map.keySet()){
        //     openMap.put(ge, map.getInt(ge));
        // }
        // return openMap;
    // }

    // @Final
	// @Mutable
	// @Shadow
	// public static Object2IntMap<GameEvent> FREQUENCIES;

    @Accessor("FREQUENCIES")
    @Mutable
    public static void setFrequencies(Object2IntMap<GameEvent> frequencies){
        throw new AssertionError();
    }
}
