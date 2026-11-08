package com.samsthenerd.hexgloop.misc;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.mixins.vibrations.MixinGameEventFrequencies;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class HexGloopGameEvents {
    // use non deferred so that we can get them to register before the sculk sensor block
    public static final DeferredRegister<GameEvent> gameEvents = DeferredRegister.create(HexGloop.MOD_ID, net.minecraft.core.registries.Registries.GAME_EVENT);
    // public static final Registrar<GameEvent> gameEvents = HexGloop.REGISTRIES.get().get(Registry.GAME_EVENT_KEY);
    
    public static final Object2IntMap<Supplier<GameEvent>> ourFreqs = new Object2IntOpenHashMap<Supplier<GameEvent>>();
    
    public static final RegistrySupplier<GameEvent> CASTING_EVENT = gameEvent("casting_event", 16, 2);

    public static void register(){
        gameEvents.register();
    }

    public static void addGameEventFreq(GameEvent ge, int freq){
        // 1.21: SculkSensorBlock.VIBRATION_FREQUENCY_FOR_EVENT and the FREQUENCIES field are gone;
        // vibration frequencies are data-driven via the neoforge game_event/vibration_frequencies data map, so there is nothing to mutate at runtime here.
    }

    public static RegistrySupplier<GameEvent> gameEvent(String id, int range){
        return gameEvent(id, range, 1);
    }

    public static RegistrySupplier<GameEvent> gameEvent(String id, int range, int freq){
        HexGloop.logPrint("try to register " + id + " with range " + range + " and freq " + freq);
		RegistrySupplier<GameEvent> ourEvent = gameEvents.register(ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, id), 
				() -> new GameEvent(range));
        ourFreqs.put(ourEvent, freq);
        ourEvent.listen((event) -> {
            addGameEventFreq(event, freq);
        });
        return ourEvent;
	}
}
