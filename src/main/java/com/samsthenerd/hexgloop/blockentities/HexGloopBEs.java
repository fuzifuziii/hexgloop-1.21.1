package com.samsthenerd.hexgloop.blockentities;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.blocks.HexGloopBlocks;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class HexGloopBEs {
    public static DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(HexGloop.MOD_ID, net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE);

    public static RegistrySupplier<BlockEntityType<BlockEntityGloopEnergizer>> GLOOP_ENERGIZER_BE 
        = blockEntities.register(ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "gloop_energizer_tile"), 
        () -> BlockEntityType.Builder.of(BlockEntityGloopEnergizer::new, HexGloopBlocks.GLOOP_ENERGIZER_BLOCK.get()).build(null));

    public static RegistrySupplier<BlockEntityType<BlockEntityPedestal>> PEDESTAL_BE
        = blockEntities.register(
            ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "pedestal_tile"),
            () -> BlockEntityType.Builder.of(BlockEntityPedestal::new, HexGloopBlocks.PEDESTAL_BLOCK.get(), HexGloopBlocks.MIRROR_PEDESTAL_BLOCK.get(), HexGloopBlocks.MIND_PEDESTAL_BLOCK.get()).build(null)
        );

    public static RegistrySupplier<BlockEntityType<BlockEntitySlateChest>> SLATE_CHEST_BE
        = blockEntities.register(
            ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "slate_chest_tile"),
            () -> BlockEntityType.Builder.of(BlockEntitySlateChest::new, HexGloopBlocks.SLATE_CHEST_BLOCK.get(), HexGloopBlocks.GLOOPY_SLATE_CHEST_BLOCK.get()).build(null)
        );

    public static RegistrySupplier<BlockEntityType<BlockEntityConjuredRedstone>> CONJURED_REDSTONE_BE 
        = blockEntities.register(ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "conjured_redstone_tile"), 
        () -> BlockEntityType.Builder.of(BlockEntityConjuredRedstone::new, HexGloopBlocks.CONJURED_REDSTONE_BLOCK.get()).build(null));

    public static RegistrySupplier<BlockEntityType<BlockEntityDial>> DIAL_BE 
        = blockEntities.register(ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "iotic_dial_tile"), 
        () -> BlockEntityType.Builder.of(BlockEntityDial::new, HexGloopBlocks.IOTIC_DIAL_BLOCK.get()).build(null));

    // block entity types that implement Container and should expose an item handler capability (NeoForge)
    public static java.util.List<BlockEntityType<?>> inventoryBlockEntityTypes(){
        java.util.List<BlockEntityType<?>> out = new java.util.ArrayList<>();
        for(var sup : java.util.List.of(GLOOP_ENERGIZER_BE, PEDESTAL_BE, SLATE_CHEST_BE, CONJURED_REDSTONE_BE, DIAL_BE)){
            BlockEntityType<?> type = sup.get();
            out.add(type);
        }
        return out;
    }

    public static void register(){
        blockEntities.register();
    }
}
