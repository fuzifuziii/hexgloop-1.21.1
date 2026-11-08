package com.samsthenerd.hexgloop.neoforge.misc;

import com.samsthenerd.hexgloop.HexGloop;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class BundleResourcePackForge {
    // 1.21: AddPackFindersEvent has a helper for packs bundled inside the mod jar.
    public static void setupBuiltInResourcePack(final AddPackFindersEvent event) {
        event.addPackFinders(
            ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "resourcepacks/dyeablestaffs"),
            PackType.CLIENT_RESOURCES,
            Component.literal("Dyeable Staffs"),
            PackSource.BUILT_IN,
            false,
            Pack.Position.BOTTOM);
    }
}
