package com.samsthenerd.hexgloop.neoforge;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.HexGloopClient;
import com.samsthenerd.hexgloop.casting.HexGloopRegisterPatterns;
import com.samsthenerd.hexgloop.misc.TrinketyImplFake;
import com.samsthenerd.hexgloop.neoforge.misc.BundleResourcePackForge;
import com.samsthenerd.hexgloop.neoforge.misc.TrinketyImplForge;

import at.petrak.hexcasting.common.lib.HexRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(HexGloop.MOD_ID)
public class HexGloopForge {
    public HexGloopForge(IEventBus modBus, ModContainer container){
        HexGloop.MOD_BUS = modBus;
        modBus.addListener(this::onClientSetup);
        modBus.addListener(EventPriority.NORMAL, BundleResourcePackForge::setupBuiltInResourcePack);
        modBus.addListener(HexGloopCaps::registerCaps);

        // hexcasting actions live in their own registry now (Hex 0.12)
        modBus.addListener((RegisterEvent event) ->
            event.register(HexRegistries.ACTION, helper ->
                HexGloopRegisterPatterns.register((entry, id) -> helper.register(id, entry))));

        if(FMLEnvironment.dist == Dist.CLIENT){
            HexGloopForgeClient.init(modBus);
        }

        // setup curios
        if(ModList.get().isLoaded("curios")){
            HexGloop.TRINKETY_INSTANCE = new TrinketyImplForge();
        } else {
            HexGloop.TRINKETY_INSTANCE = new TrinketyImplFake();
        }

        HexGloop.GLOOPXPLAT = new GloopXPlatForge();
        HexGloop.onInitialize();
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        HexGloopClient.onInitializeClient();
    }
}
