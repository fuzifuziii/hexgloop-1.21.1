package com.samsthenerd.hexgloop.casting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.blocks.HexGloopBlocks;
import com.samsthenerd.hexgloop.casting.canvas.OpGetBlockColor;
import com.samsthenerd.hexgloop.casting.canvas.OpPutColor;
import com.samsthenerd.hexgloop.casting.dimensions.OpIsInDimension;
import com.samsthenerd.hexgloop.casting.gloopifact.OpReadGloopifact;
import com.samsthenerd.hexgloop.casting.gloopifact.OpSyncRavenmindGloopifact;
import com.samsthenerd.hexgloop.casting.gloopifact.OpWriteGloopifact;
import com.samsthenerd.hexgloop.casting.inventorty.OpGetInventoryLore;
import com.samsthenerd.hexgloop.casting.inventorty.OpGetTypeInSlot;
import com.samsthenerd.hexgloop.casting.inventorty.OpSlotCount;
import com.samsthenerd.hexgloop.casting.inventorty.OpStackTransfer;
import com.samsthenerd.hexgloop.casting.ioticblocks.OpReadBlock;
import com.samsthenerd.hexgloop.casting.ioticblocks.OpWriteBlock;
import com.samsthenerd.hexgloop.casting.mirror.OpBindMirror;
import com.samsthenerd.hexgloop.casting.mishapprotection.OpEvalCatchMishap;
import com.samsthenerd.hexgloop.casting.mishapprotection.OpHahaFunnyAssertQEDGetItLikeTheMathProofLol;
import com.samsthenerd.hexgloop.casting.mishapprotection.OpRevealLastMishap;
import com.samsthenerd.hexgloop.casting.orchard.OpReadOrchard;
import com.samsthenerd.hexgloop.casting.redstone.OpConjureRedstone;
import com.samsthenerd.hexgloop.casting.redstone.OpGetComparator;
import com.samsthenerd.hexgloop.casting.tags.OpCheckTag;
import com.samsthenerd.hexgloop.casting.tags.OpGetTags;
import com.samsthenerd.hexgloop.casting.trinketyfocii.OpTrinketyReadIota;
import com.samsthenerd.hexgloop.casting.trinketyfocii.OpTrinketyWriteIota;
import com.samsthenerd.hexgloop.casting.truenameclassaction.OpAgreeTruenameEULA;
import com.samsthenerd.hexgloop.casting.truenameclassaction.OpGetCoinBinder;
import com.samsthenerd.hexgloop.casting.truenameclassaction.OpRefreshTruename;
import com.samsthenerd.hexgloop.items.HexGloopItems;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import net.minecraft.network.chat.Component;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.actions.spells.OpMakePackagedSpell;
import dev.architectury.registry.registries.RegistrySupplier;

public class HexGloopRegisterPatterns {
    // 1.21 / Hex 0.12: patterns are entries of the hexcasting:action registry, flushed from a RegisterEvent.
    private static final Map<ResourceLocation, ActionRegistryEntry> ACTIONS = new LinkedHashMap<>();
    private static boolean collected = false;

    private static void make(HexPattern pattern, ResourceLocation id, Action action){
        ACTIONS.put(id, new ActionRegistryEntry(pattern, action));
    }

    public static void register(BiConsumer<ActionRegistryEntry, ResourceLocation> r){
        if(!collected){
            registerPatterns();
            collected = true;
        }
        ACTIONS.forEach((id, entry) -> r.accept(entry, id));
    }

    public static void registerPatterns(){
        // 1.21: actions are lazily constructed registry entries; item/block suppliers are only resolved at cast time,
        // so the old "wait for the item to be registered" listeners are no longer needed.
        registerItemDependentPatterns();
        registerTrinketyFociiPatterns();
        registerRedstonePatterns();
        maybeRegisterHexal();
        maybeRegisterMoreIotas();
        // non item dependent stuff: 
        try{
            // orchard patterns
            make(HexPattern.fromAngleString("dqqqqq", HexDir.SOUTH_EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "read_orchard"),
                new OpReadOrchard(false));
            make(HexPattern.fromAngleString("dqqqqqdeeeqdqeee", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "read_orchard_list"),
                new OpReadOrchard(true));
            
            // dimension checks
            make(HexPattern.fromAngleString("aqawqadaqdeeweweweew", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "in_overworld"),
                new OpIsInDimension(Level.OVERWORLD));
            make(HexPattern.fromAngleString("eaqawqadaqdeewewewe", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "in_nether"),
                new OpIsInDimension(Level.NETHER));

            make(HexPattern.fromAngleString("ddeaqq", HexDir.NORTH_EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "opnop_useless"), 
                new OpNop());

            make(HexPattern.fromAngleString("wawaw", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "check_ambit"),
                new OpCheckAmbit());

            // catchy eval
            make(HexPattern.fromAngleString("dweaqqw", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "catchy_eval"),
                OpEvalCatchMishap.INSTANCE);
            make(HexPattern.fromAngleString("dweaqqqqa", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "reveal_mishap"),
                new OpRevealLastMishap(true));

            make(HexPattern.fromAngleString("qed", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "assert"),
                new OpHahaFunnyAssertQEDGetItLikeTheMathProofLol());

            make(HexPattern.fromAngleString("qewdweeddw", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "gloopimind_upload"),
                new OpSyncRavenmindGloopifact(true));
            make(HexPattern.fromAngleString("qedadewdde", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "gloopimind_download"),
                new OpSyncRavenmindGloopifact(false));
            
            // bound mirror pedestal stuff:
            // normal bind: deeeedwwdwdw, north east
            make(HexPattern.fromAngleString("deeeedwwdwdw", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "bind_mirror"),
                new OpBindMirror(false));
            // temp bind  : aqqqqawwawaw, north west
            make(HexPattern.fromAngleString("aqqqqawwawaw", HexDir.NORTH_WEST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "temp_bind_mirror"),
                new OpBindMirror(true));

            // hotbar patterns
            make(HexPattern.fromAngleString("dwewdwe", HexDir.WEST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "set_hotbar_slot"),
                new OpHotbar(false, false));

            make(HexPattern.fromAngleString("qwawqwa", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "get_hotbar_slot"),
                new OpHotbar(false, true));

            make(HexPattern.fromAngleString("qwawqwadawqwa", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "swap_hotbar_slot"),
                new OpHotbar(true, false));
            
            // inventorty patterns
            make(HexPattern.fromAngleString("wawqwaqewdwwdade", HexDir.SOUTH_WEST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "torty_transfer"),
                new OpStackTransfer());
            make(HexPattern.fromAngleString("awewdqdwewaaw", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "torty_count"),
                new OpSlotCount(true));
            make(HexPattern.fromAngleString("ewdqdwewaqae", HexDir.NORTH_WEST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "torty_max_count"),
                new OpSlotCount(false));

            make(HexPattern.fromAngleString("qaeaqeaq", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "torty_inv_sizes"),
                new OpGetInventoryLore(true));

            // gloopifact patterns
            make(HexPattern.fromAngleString("aqqqqqeqadaqw", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "gloopifact_read"),
                new OpReadGloopifact(false));
            make(HexPattern.fromAngleString("deeeeeqqadaqw", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "gloopifact_write"),
                new OpWriteGloopifact(false));
            make(HexPattern.fromAngleString("wwqadaqwwaqqqqq", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "gloopifact_check_read"),
                new OpReadGloopifact(true));
            make(HexPattern.fromAngleString("wwqadaqwwdeeeee", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "gloopifact_check_write"),
                new OpWriteGloopifact(true));
            

            // qwawqwadawqwqwqwqwqw <- simpler sign write with hexagon
            make(HexPattern.fromAngleString("wwedwewdweqawqwqwqwqwqw", HexDir.SOUTH_WEST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "set_label"),
                new OpSetLabel());

            // snack
            make(HexPattern.fromAngleString("eeewdw", HexDir.SOUTH_WEST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "conjure_tasty_treat"),
                new OpConjureTastyTreat());
            
            // for coins really, i mean i guess could be expanded to other stuff later though ?
            make(HexPattern.fromAngleString("qaqqaqqqqq", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "get_item_bound_caster"),
                new OpGetCoinBinder(false));
            make(HexPattern.fromAngleString("qaqqaqqqqqead", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "compare_item_bound_caster"),
                new OpGetCoinBinder(true));
            make(HexPattern.fromAngleString("qaqqwawqwqwqwqwqw", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "cooler_get_item_bound_caster"),
                new OpGetCoinBinder(false, true));
            make(HexPattern.fromAngleString("qaqqwawqwqwqwqwqwead", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "cooler_compare_item_bound_caster"),
                new OpGetCoinBinder(true, true));

            make(HexPattern.fromAngleString("awwqaq", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "truename_agree_eula"),
                new OpAgreeTruenameEULA());

            // op dispense
            make(HexPattern.fromAngleString("wqwaeqqqeddqeqd", HexDir.SOUTH_WEST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "dispense"), 
                new OpDispense());
                
            // op frog eat
            make(HexPattern.fromAngleString("wawadawaewqaw", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "frog_eat"), 
                new OpFrogEat());

            // iotic block read and writes
            make(HexPattern.fromAngleString("aqqqqqeawqwaw", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "block_read"), 
                new OpReadBlock(false));
            make(HexPattern.fromAngleString("deeeeeqdwewewewdw", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "block_write"), 
                new OpWriteBlock(false));
            make(HexPattern.fromAngleString("aqqqqqeawqwawe", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "can_block_read"), 
                new OpReadBlock(true));
            make(HexPattern.fromAngleString("deeeeeqdwewewewdwe", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "can_block_write"), 
                new OpWriteBlock(true));

            make(HexPattern.fromAngleString("edwdqeeeeaaeaeaeaea", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "stonecut"), 
                new OpStoneCut());

            make(HexPattern.fromAngleString("edeaeeeweee", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "put_canvas_color"),
                new OpPutColor());
            make(HexPattern.fromAngleString("qqqqeqwawqwa", HexDir.NORTH_WEST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "get_block_color"),
                new OpGetBlockColor());

        } catch (RuntimeException exn) {
            exn.printStackTrace();
        }
    }

    private static Map<RegistrySupplier<? extends Item>, UncheckedPatternRegister> itemDependentPatternRegisterers = new HashMap<>();

    static {
        itemDependentPatternRegisterers.put(HexGloopItems.CASTING_POTION_ITEM, () -> {
            make(HexPattern.fromAngleString("wawwedewwqqq", HexDir.EAST), 
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "craft/potion"),
                new OpMakePackagedSpell(stack -> stack.is(HexGloopItems.CASTING_POTION_ITEM.get()), () -> HexGloopItems.CASTING_POTION_ITEM.get().getDescription(), MediaConstants.SHARD_UNIT));
        });

        itemDependentPatternRegisterers.put(HexGloopItems.GLOOPIFACT_ITEM, () -> {
            make(HexPattern.fromAngleString("wwaadaqwaweqqwaweewawqwwwwadeeeeeqww", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "craft/gloopifact"),
                new OpMakePackagedSpell(stack -> stack.is(HexGloopItems.GLOOPIFACT_ITEM.get()), () -> HexGloopItems.GLOOPIFACT_ITEM.get().getDescription(), MediaConstants.CRYSTAL_UNIT * 16));
        });

        itemDependentPatternRegisterers.put(HexGloopItems.INVENTORTY_ITEM, () -> {
            make(HexPattern.fromAngleString("waqqqqqwqawqedeqwaq", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "craft/inventorty"),
                new OpMakePackagedSpell(stack -> stack.is(HexGloopItems.INVENTORTY_ITEM.get()), () -> HexGloopItems.INVENTORTY_ITEM.get().getDescription(), MediaConstants.CRYSTAL_UNIT * 4));
        });

        itemDependentPatternRegisterers.put(HexGloopItems.HEX_BLADE_ITEM, () -> {
            make(HexPattern.fromAngleString("waqqqqqwwwaqwwwwaq", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "craft/hex_blade"),
                new OpMakePackagedSpell(stack -> stack.is(HexGloopItems.HEX_BLADE_ITEM.get()), () -> HexGloopItems.HEX_BLADE_ITEM.get().getDescription(), MediaConstants.CRYSTAL_UNIT * 4));
        });

        itemDependentPatternRegisterers.put(HexGloopItems.HEX_PICKAXE_ITEM, () -> {
            make(HexPattern.fromAngleString("wwaqqqqqeaqdewaqweaewqawedqqqeaeq", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "craft/hex_pickaxe"),
                new OpMakePackagedSpell(stack -> stack.is(HexGloopItems.HEX_PICKAXE_ITEM.get()), () -> HexGloopItems.HEX_PICKAXE_ITEM.get().getDescription(), MediaConstants.CRYSTAL_UNIT * 4));
        });

        itemDependentPatternRegisterers.put(HexGloopItems.CASTING_FROG_ITEM, () -> {
            make(HexPattern.fromAngleString("wwaqqqqqeaqdqaqedeqaqdqqeaqwdwqae", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "craft/casting_frog"),
                new OpMakePackagedSpell(stack -> stack.is(HexGloopItems.CASTING_FROG_ITEM.get()), () -> HexGloopItems.CASTING_FROG_ITEM.get().getDescription(), MediaConstants.CRYSTAL_UNIT * 4));
        });

        // craft shoe things: waqqqqqwwaqwdwqaw        
    }

    private static void registerItemDependentPatterns(){
        for(Entry<RegistrySupplier<? extends Item>, UncheckedPatternRegister> entry : itemDependentPatternRegisterers.entrySet()){
            entry.getValue().register();
        }
    }

    private static void registerRedstonePatterns(){
        try {
            // redstone stuff
            make(HexPattern.fromAngleString("ddwwdwwdd", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "read_comparator"),
                new OpGetComparator(false));
            make(HexPattern.fromAngleString("aawwawwaa", HexDir.SOUTH_WEST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "read_redstone"),
                new OpGetComparator(true));

            make(HexPattern.fromAngleString("qqadd", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "conjure_redstone"),
                new OpConjureRedstone());
        } catch (RuntimeException exn) {
            exn.printStackTrace();
        }
    }

    private static void registerTrinketyFociiPatterns(){
        try {
            // pendant read/write
            make(HexPattern.fromAngleString("waaqqqqqe", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "read_pendant"),
                new OpTrinketyReadIota((ctx) -> List.of("necklace"), false));
            make(HexPattern.fromAngleString("wadeeeeeq", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "write_pendant"),
                new OpTrinketyWriteIota((ctx) -> List.of("necklace"), false));
            // pendant checks
            make(HexPattern.fromAngleString("waaqqqqqee", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "check_read_pendant"),
                new OpTrinketyReadIota((ctx) -> List.of("necklace"), true));
            make(HexPattern.fromAngleString("wadeeeeeqe", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "check_write_pendant"),
                new OpTrinketyWriteIota((ctx) -> List.of("necklace"), true));

            // ring basics
            Function<CastingEnvironment, List<String>> standardRingFunc = (ctx) -> {
                if(ctx.getCastingHand() == InteractionHand.MAIN_HAND) {
                    return List.of("offhandring", "mainhandring");
                }
                return List.of("mainhandring", "offhandring");
            };
            make(HexPattern.fromAngleString("aqqqqqeawqwqwqwqwqw", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "read_ring"),
                new OpTrinketyReadIota(standardRingFunc, false));
            make(HexPattern.fromAngleString("deeeeeqdwewewewewew", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "write_ring"),
                new OpTrinketyWriteIota(standardRingFunc, false));
            
            // left hand ring
            make(HexPattern.fromAngleString("aqqweeeeewqq", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "read_left_ring"),
                new OpTrinketyReadIota((ctx) -> List.of("offhandring", "mainhandring"), false));
            make(HexPattern.fromAngleString("deewqqqqqwee", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "write_left_ring"),
                new OpTrinketyWriteIota((ctx) -> List.of("offhandring", "mainhandring"), false));
            
            // right hand ring
            make(HexPattern.fromAngleString("aqqqqqweeeee", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "read_right_ring"),
                new OpTrinketyReadIota((ctx) -> List.of("mainhandring", "offhandring"), false));
            make(HexPattern.fromAngleString("deeeeewqqqqq", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "write_right_ring"),
                new OpTrinketyWriteIota((ctx) -> List.of("mainhandring", "offhandring"), false));
            
            // ring checks
            make(HexPattern.fromAngleString("aqqqqqeawqwqwqwqwqwe", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "check_read_ring"),
                new OpTrinketyReadIota(standardRingFunc, true));
            make(HexPattern.fromAngleString("deeeeeqdwewewewewewq", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "check_write_ring"),
                new OpTrinketyWriteIota(standardRingFunc, true));

            make(HexPattern.fromAngleString("wwwdwwwdwqqaqwedeewawwwawww", HexDir.SOUTH_WEST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "clear_truename"),
                new OpRefreshTruename());
        } catch (RuntimeException exn) {
            exn.printStackTrace();
        }
    }

    private static void maybeRegisterHexal(){
        if(!net.neoforged.fml.ModList.get().isLoaded("hexal")) return;
        try {
            make(HexPattern.fromAngleString("qwaeawqaqded", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "torty_get_type"),
                new OpGetTypeInSlot());
            
        } catch (RuntimeException exn) {
            exn.printStackTrace();
        }
    }

    private static void maybeRegisterMoreIotas(){
        if(!net.neoforged.fml.ModList.get().isLoaded("moreiotas")) return;
        try {
            make(HexPattern.fromAngleString("dweaqqqqd", HexDir.SOUTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "stringify_mishap"),
                new OpRevealLastMishap(false));

            make(HexPattern.fromAngleString("waeaeaeawa", HexDir.EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "torty_inv_names"),
                new OpGetInventoryLore(false));
            // tag stuff
            make(HexPattern.fromAngleString("wwedwe", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "get_tags"),
                new OpGetTags());
            make(HexPattern.fromAngleString("wwedwew", HexDir.NORTH_EAST),
                ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "check_tag"),
                new OpCheckTag());
            
        } catch (RuntimeException exn) {
            exn.printStackTrace();
        }
    }

    @FunctionalInterface
    public static interface UncheckedPatternRegister{
        public void register();
    }
}
