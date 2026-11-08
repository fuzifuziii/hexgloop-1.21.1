package com.samsthenerd.hexgloop;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Pair;
import com.samsthenerd.hexgloop.blockentities.BERConjuredRedstone;
import com.samsthenerd.hexgloop.blockentities.BERHexChest;
import com.samsthenerd.hexgloop.blockentities.BlockEntityDial;
import com.samsthenerd.hexgloop.blockentities.BlockEntityGloopEnergizer;
import com.samsthenerd.hexgloop.blockentities.BlockEntityPedestal;
import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;
import com.samsthenerd.hexgloop.blocks.HexGloopBlocks;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.items.ItemCastersCoin;
import com.samsthenerd.hexgloop.items.ItemCastingFrog;
import com.samsthenerd.hexgloop.items.ItemEssenceStone;
import com.samsthenerd.hexgloop.items.ItemFidget;
import com.samsthenerd.hexgloop.items.ItemGloopDye;
import com.samsthenerd.hexgloop.items.ItemGloopifact;
import com.samsthenerd.hexgloop.items.ItemHandMirror;
import com.samsthenerd.hexgloop.items.ItemHexSword;
import com.samsthenerd.hexgloop.items.ItemHexTool;
import com.samsthenerd.hexgloop.items.ItemLibraryCard;
import com.samsthenerd.hexgloop.items.ItemSlateLoader;
import com.samsthenerd.hexgloop.keybinds.HexGloopKeybinds;
import com.samsthenerd.hexgloop.misc.StaffColorLoader;
import com.samsthenerd.hexgloop.misc.clientgreatbook.GreatBook;
import com.samsthenerd.hexgloop.network.HexGloopNetwork;
import com.samsthenerd.hexgloop.network.ServerSideCheckClient;
import com.samsthenerd.hexgloop.utils.GloopUtils;
import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.core.registries.BuiltInRegistries;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

public class HexGloopClient {
    public static Random random = new Random();

    public static boolean isServerSide = false;

    public static Tesselator newTess;

    public static void onInitializeClient() {
        HexGloop.logPrint("Initializing HexGloopClient");
        addToTextureAtlas();
        HexGloopNetwork.registerClientSideOnly();
        registerModelPredicates();
        registerColorProviders();
        registerScryingDisplayers();
        StaffColorLoader.init();
        HexGloopKeybinds.registerKeybinds();

        ServerSideCheckClient.registerDisconnectUpdate();

        GreatBook.registerLoadEvent();
        registerRenderers();

        newTess = new Tesselator();
    }

    // TODO(1.21 port): ClientTextureStitchEvent is gone. Atlases are now JSON-defined:
    //  - chest sprites hexgloop:entity/chest/{gloopy_slate_chest,slate_chest} -> assets/minecraft/atlases/chests.json
    //  - patchouli lectern book sprites (entity/lectern_<book path>) were added dynamically per Patchouli book;
    //    now need static entries in assets/minecraft/atlases/blocks.json (hexgloop book only if such a texture exists).
    private static void addToTextureAtlas(){
    }

    private static void registerRenderers(){
        BlockEntityRendererRegistry.register(HexGloopBEs.CONJURED_REDSTONE_BE.get(), BERConjuredRedstone::new);
        BlockEntityRendererRegistry.register(HexGloopBEs.SLATE_CHEST_BE.get(), BERHexChest::new);
        RenderTypeRegistry.register(RenderType.translucent(), HexGloopBlocks.CONJURED_REDSTONE_BLOCK.get(), HexGloopBlocks.HEXXED_GLASS_BLOCK.get(),
            HexGloopBlocks.ENLIGHTENMENT_BRIDGE_BLOCK.get(), HexGloopBlocks.ENLIGHTENMENT_GATE_BLOCK.get(), HexGloopBlocks.GLOOP_BLOCK.get());

        RenderTypeRegistry.register(RenderType.cutout(), HexGloopBlocks.IOTIC_DIAL_BLOCK.get());
    }

    private static void registerColorProviders(){
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if(tintIndex == 1){
                Iota iota = HexGloopItems.MULTI_FOCUS_ITEM.get().readIota(stack);
                if(iota == null){
                    return 0xFFFFFF; //white
                }
                return iota.getType().color();
            }
            return 0xFFFFFF; //white
		}, HexGloopItems.MULTI_FOCUS_ITEM.get());

        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            FrozenPigment colorizer = HexGloopItems.CASTING_POTION_ITEM.get().getColorizer(stack);
            if(tintIndex == 0 || tintIndex >= 5 || colorizer == null){
                return 0xFFFFFF; //white
            }
            return tintsFromColorizer(colorizer, tintIndex-1, 4);
        }, HexGloopItems.CASTING_POTION_ITEM.get());

        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if(tintIndex == 1 || tintIndex > 4){ // base
                return 0xFFFFFF; //white
            }
            int color = ItemGloopDye.getDyeColor(stack);
            if(tintIndex == 0){
                return color;
            }
            if(tintIndex == 2){ // red
                return color & 0xFF0000;
            }
            if(tintIndex == 3){ // green
                return color & 0x00FF00;
            }
            if(tintIndex == 4){ // blue
                return color & 0x0000FF;
            }
            return 0xFFFFFF; //white
        }, HexGloopItems.GLOOP_DYE_ITEM.get());

        ItemLike[] NEW_FOCII = {HexGloopItems.FOCAL_PENDANT.get(), HexGloopItems.FOCAL_RING.get(), HexGloopItems.CASTERS_COIN.get(), 
            HexGloopItems.GLOOPIFACT_ITEM.get()};
        
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if(tintIndex == 1){
                return GloopUtils.getIotaColor(stack);
            }
            return 0xFF_FFFFFF;
        }, NEW_FOCII);

        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if(tintIndex == 1){
                return dyedColor(stack);
            }
            if(tintIndex == 2){
                return GloopUtils.getIotaColor(stack);
            }
            return 0xFF_FFFFFF;
        }, HexGloopItems.DYEABLE_SPELLBOOK_ITEM);

        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if(tintIndex == 0){
                return dyedColor(stack);
            }
            return 0xFF_FFFFFF;
        }, HexGloopItems.DREIDEL_FIDGET);

        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if(tintIndex == 1){
                return HexGloopItems.ESSENCE_STONE_ITEM.get().getColors(stack).getA();
            }
            if(tintIndex == 2){
                return HexGloopItems.ESSENCE_STONE_ITEM.get().getColors(stack).getB();
            }
            return 0xFF_FFFFFF;
        }, HexGloopItems.ESSENCE_STONE_ITEM);

        int emptyColor = 0x382f40;

        ColorHandlerRegistry.registerBlockColors((state, world, pos, tintIndex) -> {
            if(!(world.getBlockEntity(pos) instanceof BlockEntityDial dialBE)){
                return emptyColor;
            }
            ItemStack stack = dialBE.getInnerMultiFocus().copy();
            if(stack.isEmpty()){
                return emptyColor;
            }
            if(tintIndex == 0){
                Iota iota = HexGloopItems.MULTI_FOCUS_ITEM.get().readIota(stack);
                if(iota == null){
                    return emptyColor;
                }
                return iota.getType().color();
            }
            int selIndex = tintIndex + (tintIndex >= dialBE.getSelection() ? 1 : 0);
            Iota slotIota = HexGloopItems.MULTI_FOCUS_ITEM.get().readSlotIota(stack, selIndex);
            if(slotIota == null){
                return emptyColor;
            }
            return slotIota.getType().color();
        }, HexGloopBlocks.IOTIC_DIAL_BLOCK);
    }

    private static int dyedColor(ItemStack stack){
        return net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack, 0xFFFFFF);
    }

    public static int tintsFromColorizer(FrozenPigment colorizer, int tintIndex, int sections){
        float time = Minecraft.getInstance().level.getGameTime();
        double section = 5.0 * tintIndex;
        return colorizer.getColorProvider().getColor(time, new Vec3(section, 0, 0));
    }

    private static void registerModelPredicates(){
        ItemPropertiesRegistry.register(HexGloopItems.MULTI_FOCUS_ITEM.get(), ResourceLocation.parse("selected"), 
            (ItemStack itemStack, ClientLevel clientWorld, LivingEntity livingEntity, int i) -> {
            int slot = ItemSpellbook.getPage(itemStack, -1);
            if(slot < 1 || HexGloopItems.MULTI_FOCUS_ITEM.get().readIota(itemStack) == null){
                return 0.0F;
            }
            return (float)((1/6.0)*(slot));
        });

        ItemPropertiesRegistry.register(HexGloopItems.MULTI_FOCUS_ITEM.get(), ResourceLocation.parse("sealed"), 
            (ItemStack itemStack, ClientLevel clientWorld, LivingEntity livingEntity, int i) -> {
            return ItemSpellbook.isSealed(itemStack) ? 1.0F : 0.0F;
        });

        ItemPropertiesRegistry.register(HexGloopItems.CASTING_POTION_ITEM.get(), ResourceLocation.parse("colorized"),
            (ItemStack itemStack, ClientLevel clientWorld, LivingEntity livingEntity, int i) -> {
                FrozenPigment colorizer = HexGloopItems.CASTING_POTION_ITEM.get().getColorizer(itemStack);
                return (colorizer == null) ? 0.0F : 1.0F;
            }
        );

        ItemPropertiesRegistry.register(HexGloopItems.CASTING_POTION_ITEM.get(), ResourceLocation.parse("hashex"),
            (ItemStack itemStack, ClientLevel clientWorld, LivingEntity livingEntity, int i) -> {
                return (HexGloopItems.CASTING_POTION_ITEM.get().hasHex(itemStack)) ? 1.0F : 0.0F;
            }
        );

        ClampedItemPropertyFunction focusModelProvider = (stack, level, holder, holderID) -> {
            if(!(stack.getItem() instanceof IotaHolderItem iotaHolder)) return 0;
            if (iotaHolder.readIota(stack) == null && !StackNBT.hasString(stack, "VisualOverride") /* TODO(1.21 port): IotaHolderItem.TAG_OVERRIDE_VISUALLY removed in hex 0.12 */) {
                return 0;
            }
            // so it works for dyebook too
            if(stack.getItem() instanceof ItemSpellbook && ItemSpellbook.isSealed(stack)){
                return 1;
            }
            if (ItemFocus.isSealed(stack)) {
                return 1;
            }
            return 0.5f;
        };

        ItemPropertiesRegistry.register(HexGloopItems.FOCAL_PENDANT.get(), ItemFocus.OVERLAY_PRED, focusModelProvider);
        ItemPropertiesRegistry.register(HexGloopItems.FOCAL_RING.get(), ItemFocus.OVERLAY_PRED, focusModelProvider);
        ItemPropertiesRegistry.register(HexGloopItems.DYEABLE_SPELLBOOK_ITEM.get(), ItemFocus.OVERLAY_PRED, focusModelProvider);

        ItemPropertiesRegistry.register(HexGloopItems.CASTERS_COIN.get(), ItemCastersCoin.OVERLAY_PRED,
            (ItemStack itemStack, ClientLevel clientWorld, LivingEntity livingEntity, int i) -> {
                return (HexGloopItems.CASTERS_COIN.get().isBound(itemStack)) ? 1.0F : 0.0F;
            }
        );

        ItemPropertiesRegistry.register(HexGloopItems.GLOOPIFACT_ITEM.get(), ItemFocus.OVERLAY_PRED, (stack, level, holder, holderID) -> {
            ItemGloopifact gloopifactItem = HexGloopItems.GLOOPIFACT_ITEM.get();
            if(!gloopifactItem.hasHex(stack)){
                return 0;
            }
            if(gloopifactItem.readIota(stack) == null){
                return 0.5f;
            }
            return 1f;
        });

        ItemPropertiesRegistry.register(HexGloopItems.HAND_MIRROR_ITEM.get(), ItemHandMirror.MIRROR_ACTIVATED_PRED, (stack, level, holder, holderID) -> {
            return HexGloopItems.HAND_MIRROR_ITEM.get().isMirrorActivated(stack) ? 1 : 0;
        });

        ItemPropertiesRegistry.register(HexGloopItems.SLATE_LOADER_ITEM.get(), ItemSlateLoader.ACTIVATED_PRED, (stack, level, holder, holderID) -> {
            return HexGloopItems.SLATE_LOADER_ITEM.get().hasPatterns(stack) ? 1 : 0;
        });

        ItemPropertiesRegistry.register(HexGloopItems.SLATE_CANVAS_ITEM.get(), ItemSlateLoader.ACTIVATED_PRED, (stack, level, holder, holderID) -> {
            return stack.get(net.minecraft.core.component.DataComponents.MAP_ID) != null ? 1 : 0;
        });

        ItemPropertiesRegistry.register(HexGloopItems.INVENTORTY_ITEM.get(), ItemPackagedHex.HAS_PATTERNS_PRED, (stack, level, holder, holderID) -> {
            return HexGloopItems.INVENTORTY_ITEM.get().hasHex(stack) ? 1 : 0;
        });

        ItemPropertiesRegistry.register(HexGloopItems.CASTING_FROG_ITEM.get(), ItemPackagedHex.HAS_PATTERNS_PRED, (stack, level, holder, holderID) -> {
            return HexGloopItems.CASTING_FROG_ITEM.get().hasHex(stack) ? 1 : 0;
        });
        ItemPropertiesRegistry.register(HexGloopItems.CASTING_FROG_ITEM.get(), ItemCastingFrog.FROG_VARIANT_PREDICATE, (stack, level, holder, holderID) -> {
            var variant = HexGloopItems.CASTING_FROG_ITEM.get().getFrogVariant(stack);
            if(variant == null){
                Level world = level != null ? level : Minecraft.getInstance().level;
                BlockPos pos = (holder != null ? holder : Minecraft.getInstance().player).blockPosition();
                variant = ItemCastingFrog.getFrogVariantFromWorld(world, pos);
            }
            // i *guess* it makes sense to just have this be an if statement like this but it's a bit silly 
            var variantKey = variant == null ? null : BuiltInRegistries.FROG_VARIANT.getResourceKey(variant).orElse(null);
            if(variantKey == FrogVariant.COLD){
                return 0; // green
            }
            if(variantKey == FrogVariant.TEMPERATE){
                return 0.5f; // orange
            }
            if(variantKey == FrogVariant.WARM){
                return 1; // white
            }
            return 0.5f; // idk default 
        });

        ItemPropertiesRegistry.register(HexGloopItems.ESSENCE_STONE_ITEM.get(), ItemEssenceStone.ESSENCE_PREDICATE, (stack, level, holder, holderID) -> {
            return HexGloopItems.ESSENCE_STONE_ITEM.get().hasEssence(stack) ? 1 : 0;
        });

        ItemPropertiesRegistry.register(HexGloopItems.SCRIPT_ITEM.get(), ItemFocus.OVERLAY_PRED, (stack, level, holder, holderID) -> {
            return HexGloopItems.SCRIPT_ITEM.get().readIota(stack) == null ? 0 : 1;
        });

        ItemPropertiesRegistry.register(HexGloopItems.LIBRARY_CARD_ITEM.get(), ItemLibraryCard.DIMENSION_PREDICATE, (stack, level, holder, holderID) -> {
            return HexGloopItems.LIBRARY_CARD_ITEM.get().getPredicateValue(HexGloopItems.LIBRARY_CARD_ITEM.get().getDimension(stack));
        });

        ClampedItemPropertyFunction HEX_TOOL_PROVIDER = (stack, level, holder, holderID) -> {
            if(!(stack.getItem() instanceof ItemHexTool hexTool)) return 0;
            if(!hexTool.hasHex(stack)){
                return 0;
            } else {
                return hexTool.hasMediaToUse(stack) ? 1 : 0.5f;
            }
        };

        ItemPropertiesRegistry.register(HexGloopItems.HEX_BLADE_ITEM.get(), ItemHexSword.TOOL_STATUS_PREDICATE, HEX_TOOL_PROVIDER);
        ItemPropertiesRegistry.register(HexGloopItems.HEX_PICKAXE_ITEM.get(), ItemHexSword.TOOL_STATUS_PREDICATE, HEX_TOOL_PROVIDER);
        
        ClampedItemPropertyFunction FIDGET_INDEX_PROVIDER = (stack, level, holder, holderID) -> {
            if(!(stack.getItem() instanceof ItemFidget fidget)) return 0;
            
            int index = ItemFidget.getPage(stack)-1;
            return ((float)index) / (float) fidget.fidgetSettings.slots;
        };
        ItemPropertiesRegistry.register(HexGloopItems.COPPER_PEN_FIDGET.get(), ItemFidget.INDEX_PREDICATE, FIDGET_INDEX_PROVIDER);
        ItemPropertiesRegistry.register(HexGloopItems.DREIDEL_FIDGET.get(), ItemFidget.INDEX_PREDICATE, FIDGET_INDEX_PROVIDER);
        ItemPropertiesRegistry.register(HexGloopItems.RAINBOW_AMOGUS_FIDGET.get(), ItemFidget.INDEX_PREDICATE, FIDGET_INDEX_PROVIDER);
    }

    
    public static DecimalFormat DUST_FORMAT = new DecimalFormat("###,###.##");

    private static void pedestalDisplay(List<Pair<ItemStack, Component>> lines,
        BlockState state, BlockPos pos, Player observer,
        Level world,
        Direction hitFace){
            
        BlockEntityPedestal be = world.getBlockEntity(pos, HexGloopBEs.PEDESTAL_BE.get()).orElse(null);
        if(be == null) return;
        ItemStack stack = be.getItem(0);
        if(stack.isEmpty()){
            MutableComponent text = Component.literal("Empty");
            Style stlye = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
            text.setStyle(stlye);
            lines.add(new Pair<>(ItemStack.EMPTY, text));
        } else {
            lines.add(new Pair<>(stack, stack.getHoverName()));
            ADIotaHolder iotaHolder = IXplatAbstractions.INSTANCE.findDataHolder(stack);
            if(iotaHolder == null) return;
            Iota iota = iotaHolder.readIota();
            if(iota == null) return;
            if(!(iota instanceof PatternIota) && state.getBlock() != HexGloopBlocks.MIRROR_PEDESTAL_BLOCK.get()){
                return;
            }
            {
                Component iotaDesc = iota.display();
                ItemStack slateIcon = new ItemStack(HexItems.SLATE.get());
                HexItems.SLATE.get().writeDatum(slateIcon, new PatternIota(HexPattern.fromAngleString("", HexDir.EAST)));
                lines.add(new Pair<>(slateIcon, iotaDesc));
            }
        }
    }

    private static void registerScryingDisplayers(){
        ScryingLensOverlayRegistry.addDisplayer(HexGloopBlocks.GLOOP_ENERGIZER_BLOCK.get(), 
        (lines, state, pos, observer, world, direction) -> {
            if(world.getBlockEntity(pos) instanceof BlockEntityGloopEnergizer energizer){
                if (energizer.getMedia() < 0) {
                    lines.add(new Pair<>(new ItemStack(HexItems.AMETHYST_DUST.get()), ItemCreativeUnlocker.infiniteMedia(world)));
                } else {
                    var dustCount = (float) energizer.getMedia() / (float) MediaConstants.DUST_UNIT;
                    var dustCmp = Component.translatable("hexcasting.tooltip.media",
                    DUST_FORMAT.format(dustCount));
                    lines.add(new Pair<>(new ItemStack(HexItems.AMETHYST_DUST.get()), dustCmp));
                }
                lines.add(new Pair<>(new ItemStack(Items.WATER_BUCKET), Component.literal(energizer.getNumConnected() + " blocks")));
                // lines.add(new Pair<>(energizer.getLatestResult(), Text.literal("Latest result")));
            }
        });

        ScryingLensOverlayRegistry.addDisplayer(HexGloopBlocks.PEDESTAL_BLOCK.get(), HexGloopClient::pedestalDisplay);
        ScryingLensOverlayRegistry.addDisplayer(HexGloopBlocks.MIRROR_PEDESTAL_BLOCK.get(), HexGloopClient::pedestalDisplay);
        ScryingLensOverlayRegistry.addDisplayer(HexGloopBlocks.MIND_PEDESTAL_BLOCK.get(), HexGloopClient::pedestalDisplay);
        
        ScryingLensOverlayRegistry.addDisplayer(HexGloopBlocks.IOTIC_DIAL_BLOCK.get(), 
        (lines, state, pos, observer, world, direction) -> {
            if(world.getBlockEntity(pos) instanceof BlockEntityDial dialBE){
                ItemStack stack = dialBE.getInnerMultiFocus();
                if(stack.isEmpty()) return;
                Iota tag = HexGloopItems.MULTI_FOCUS_ITEM.get().readIota(stack);
                if(tag == null) return;
                Component iotaDesc = tag.display();
                lines.add(new Pair<>(stack, iotaDesc));
            }
        });
    }
}
