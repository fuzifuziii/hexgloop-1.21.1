package com.samsthenerd.hexgloop.blocks;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;
import com.samsthenerd.hexgloop.items.HexGloopItems;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class HexGloopBlocks {
    public static DeferredRegister<Block> blocks = DeferredRegister.create(HexGloop.MOD_ID, net.minecraft.core.registries.Registries.BLOCK);

    public static final RegistrySupplier<BlockGloopEnergizer> GLOOP_ENERGIZER_BLOCK = block("gloop_energizer", 
        () -> new BlockGloopEnergizer(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).requiresCorrectToolForDrops().strength(25.0f, 1200.0f).sound(SoundType.NETHERITE_BLOCK)));

    public static final RegistrySupplier<BlockPedestal> PEDESTAL_BLOCK = block("pedestal", 
        () -> new BlockPedestal(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.DEEPSLATE_TILES), false));
    public static final RegistrySupplier<BlockPedestal> MIRROR_PEDESTAL_BLOCK = block("mirror_pedestal", 
        () -> new BlockPedestal(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.DEEPSLATE_TILES), true));
    public static final RegistrySupplier<BlockPedestal> MIND_PEDESTAL_BLOCK = block("mind_pedestal", 
        () -> new BlockPedestal(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.DEEPSLATE_TILES), false, true));

    
    public static final RegistrySupplier<BlockSlateChest> SLATE_CHEST_BLOCK = block("slate_chest", 
        () -> new BlockSlateChest(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.DEEPSLATE_TILES), 
        () -> HexGloopBEs.SLATE_CHEST_BE.get(),
        false));
    public static final RegistrySupplier<BlockSlateChest> GLOOPY_SLATE_CHEST_BLOCK = block("gloopy_slate_chest", 
        () -> new BlockSlateChest(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.DEEPSLATE_TILES), 
        () -> HexGloopBEs.SLATE_CHEST_BE.get(),
        true));

    public static final RegistrySupplier<BlockIoticDial> IOTIC_DIAL_BLOCK = block("iotic_dial", 
        () -> new BlockIoticDial(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.AMETHYST)));

    public static final RegistrySupplier<BlockConjuredRedstone> CONJURED_REDSTONE_BLOCK = block("conjured_redstone", 
        () -> new BlockConjuredRedstone(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(1.5f, 6.0f).sound(SoundType.AMETHYST)),
        new Item.Properties()); // don't put it in item group

    public static final RegistrySupplier<BlockAccelerator> ACCELERATOR_BLOCK = block("accelerator", 
        () -> new BlockAccelerator(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.COPPER).lightLevel(state ->{
            return state.getValue(BlockCircleComponent.ENERGIZED) ? 7 : 0;
        })));

    public static final RegistrySupplier<BlockWaveLocus> REDSTONE_WAVE_LOCUS_BLOCK = block("wave_locus", 
        () -> new BlockWaveLocus(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.DEEPSLATE_TILES)
        .lightLevel(state ->{
            return state.getValue(BlockCircleComponent.ENERGIZED) ? 7 : 0;
        })));

    public static final RegistrySupplier<BlockSyncetrix> SYNCETRIX_BLOCK = block("syncetrix",
        () -> new BlockSyncetrix(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.DEEPSLATE_TILES)
        .lightLevel(state ->{
            return state.getValue(BlockCircleComponent.ENERGIZED) ? 10 : 0;
        })));

    public static final RegistrySupplier<BlockSentinelBed> SENTINEL_BED_BLOCK = block("sentinel_bed", 
        () -> new BlockSentinelBed(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4f, 6.0f).sound(SoundType.DEEPSLATE_TILES)));

    public static final RegistrySupplier<Block> HEXXED_GLASS_BLOCK = block("hexxed_glass", 
        () -> new BlockHexxedGlass(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(0.3f, 0.3f).sound(SoundType.GLASS).noOcclusion()));

    public static final RegistrySupplier<Block> CHARGED_AMETHYST_BLOCK = block("charged_amethyst_block", 
        () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.AMETHYST)));

    public static final RegistrySupplier<Block> GLOOP_BLOCK = block("gloop_block", 
        () -> new SlimeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_WART_BLOCK).requiresCorrectToolForDrops().strength(5.0f, 6.0f).noOcclusion().friction(0.8F).sound(SoundType.SLIME_BLOCK)));

    public static final RegistrySupplier<Block> THINKING_CARPET_BLOCK = block("thinking_carpet", 
        () -> new BlockThinkingCarpet(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(0.1f).sound(SoundType.WOOL)));

    public static final RegistrySupplier<BlockSlateLamp> SLATE_LAMP_BLOCK = block("slate_lamp", 
        () -> new BlockSlateLamp(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4f, 4f).sound(SoundType.AMETHYST)
        .lightLevel(state ->{
            return state.getValue(BlockSlateLamp.LIT) || state.getValue(BlockCircleComponent.ENERGIZED) ? 15 : 0;
        })));

    public static final RegistrySupplier<Block> ENLIGHTENMENT_GATE_BLOCK = block("enlightenment_gate", 
        () -> new BlockEnlightenedCollision(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(0.3f, 6.0f).sound(SoundType.AMETHYST)
            .noOcclusion().dynamicShape().isSuffocating((BlockState var1, BlockGetter var2, BlockPos var3) -> false).isViewBlocking((BlockState var1, BlockGetter var2, BlockPos var3) -> false), false));
    public static final RegistrySupplier<Block> ENLIGHTENMENT_BRIDGE_BLOCK = block("enlightenment_bridge", 
        () -> new BlockEnlightenedCollision(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(0.3f, 6.0f).sound(SoundType.AMETHYST)
        .noOcclusion().dynamicShape().isSuffocating((BlockState var1, BlockGetter var2, BlockPos var3) -> false).isViewBlocking((BlockState var1, BlockGetter var2, BlockPos var3) -> false), true));

    public static BlockBehaviour.Properties defaultSettings(){
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).destroyTime(1.3f);
    }

    public static <T extends Block> RegistrySupplier<T> block(String name, Supplier<T> block) {
        return block(name, block, HexGloopItems.defaultSettings(), true);
	}

    public static <T extends Block> RegistrySupplier<T> block(String name, Supplier<T> block, Item.Properties settings) {
        return block(name, block, settings, false);
	}

    public static <T extends Block> RegistrySupplier<T> block(String name, Supplier<T> block, Item.Properties settings, boolean inTab) {
        RegistrySupplier<T> blockRegistered = blockNoItem(name, block);
        HexGloopItems.item(name, () -> new BlockItem(blockRegistered.get(), settings), inTab);
		return blockRegistered;
	}

    public static <T extends Block> RegistrySupplier<T> blockNoItem(String name, Supplier<T> block) {
		return blocks.register(ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, name), block);
	}

    public static void register(){
        blocks.register();
    }
}
