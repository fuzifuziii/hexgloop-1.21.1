package com.samsthenerd.hexgloop.items;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.items.ItemFidget.FidgetSettings;
import com.samsthenerd.hexgloop.misc.GloopBanners;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker.Label;
import com.samsthenerd.hexgloop.misc.wnboi.LabelTypes.PatternLabel;
import com.samsthenerd.hexgloop.misc.wnboi.LabelTypes.PatternLabel.PatternOptions;
import com.samsthenerd.wnboi.utils.RenderUtils;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.items.ItemStaff;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class HexGloopItems{
    public static DeferredRegister<Item> items = DeferredRegister.create(HexGloop.MOD_ID, net.minecraft.core.registries.Registries.ITEM);
    // 1.20+: item groups aren't set on Item.Properties anymore. Items created via defaultSettings() are
    // collected here and listed by the creative tab (keeps the 1.19 "which items are in the tab" behaviour).
    private static final java.util.List<RegistrySupplier<? extends Item>> TAB_ITEMS = new java.util.ArrayList<>();


    public static final RegistrySupplier<Item> GLOOP_ITEM = item("gloop", 
        () -> new ItemSimpleMediaProvider(defaultSettings(), MediaConstants.CRYSTAL_UNIT * 2, 950));
    public static final RegistrySupplier<Item> SYNCHRONOUS_GLOOP_ITEM = item("synchronous_gloop", 
        () -> new Item(defaultSettings()));
    public static final RegistrySupplier<ItemGloopifact> GLOOPIFACT_ITEM = item("gloopifact",
        () -> new ItemGloopifact(defaultSettings().stacksTo(1).fireResistant()));
    public static final RegistrySupplier<ItemMultiFocus> MULTI_FOCUS_ITEM = item("multi_focus", 
        () -> new ItemMultiFocus(defaultSettings().stacksTo(1)));
    public static final RegistrySupplier<ItemCastingRing> CASTING_RING_ITEM = item("casting_ring", 
        () -> new ItemCastingRing(defaultSettings().stacksTo(1)));
    public static final RegistrySupplier<ItemCastingPotion> CASTING_POTION_ITEM = item("casting_potion", 
        () -> new ItemCastingPotion(defaultSettings().stacksTo(16)));
    public static final RegistrySupplier<ItemGloopDye> GLOOP_DYE_ITEM = item("gloop_dye", 
        () -> new ItemGloopDye(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemDyeableSpellbook> DYEABLE_SPELLBOOK_ITEM = item("covered_spellbook",
        () -> new ItemDyeableSpellbook(defaultSettings().stacksTo(1).fireResistant()));
    
    // just so i can show a spellbook in rei/patchoulli without actually worrying about it being valid
    public static final RegistrySupplier<Item> FAKE_SPELLBOOK_PLACEHOLDER = item("fake_spellbook_for_rei",
        () -> new Item(new Item.Properties()), false);


    public static final RegistrySupplier<ItemFocus> FOCAL_PENDANT = item("focal_pendant",
        () -> new ItemFocus(defaultSettings().stacksTo(1)));
    public static final RegistrySupplier<ItemFocus> FOCAL_RING = item("focal_ring",
        () -> new ItemFocus(defaultSettings().stacksTo(1).fireResistant()));
    public static final RegistrySupplier<ItemHandMirror> HAND_MIRROR_ITEM = item("hand_mirror",
        () -> new ItemHandMirror(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemCastersCoin> CASTERS_COIN = item("casters_coin",
        () -> new ItemCastersCoin(defaultSettings()));

    public static final RegistrySupplier<ItemHeartPigment> HEART_PIGMENT = item("heart_pigment",
        () -> new ItemHeartPigment(defaultSettings()));


    public static final RegistrySupplier<Item> SLATE_BOWL = item("slate_bowl", 
        () -> new Item(defaultSettings()));

    public static final int HEX_SNACK_MEDIA = (int) MediaConstants.DUST_UNIT / 2;

    public static final RegistrySupplier<Item> HEX_SNACK = item("hex_snack",
        () -> new ItemSimpleMediaProvider(defaultSettings().food((new FoodProperties.Builder()).nutrition(2).saturationModifier(0.5f).fast().build()),
            HEX_SNACK_MEDIA, 1));

    public static final RegistrySupplier<Item> HEXXY_OS = item("hexxyos",
        () -> new ItemHexxyOs(defaultSettings().food((new FoodProperties.Builder()).nutrition(8).saturationModifier(0.8f).build())));

    public static final RegistrySupplier<Item> HEXXY_PLUSH = item("hexxy_plush", () -> new BannerPatternItem(GloopBanners.HERMES_PATTERN_ITEM_KEY, defaultSettings().stacksTo(1)));


    public static final RegistrySupplier<ItemHexSword> HEX_BLADE_ITEM = item("hex_blade", 
        () -> new ItemHexSword(defaultSettings().stacksTo(1).fireResistant())); 
    public static final RegistrySupplier<ItemHexMiningTool> HEX_PICKAXE_ITEM = item("hex_pickaxe", 
        () -> new ItemHexMiningTool(defaultSettings().stacksTo(1).fireResistant(), 2.0F, -2.4f, Set.of(BlockTags.MINEABLE_WITH_PICKAXE), false)); 

    public static final RegistrySupplier<ItemSlateLoader> SLATE_LOADER_ITEM = item("slate_loader",
        () -> new ItemSlateLoader(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<Item> MEPT_LOG_ITEM = item("mept_log",
        () -> new Item(defaultSettings()));

    public static final RegistrySupplier<ItemInventorty> INVENTORTY_ITEM = item("inventorty",
        () -> new ItemInventorty(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemCastingFrog> CASTING_FROG_ITEM = item("casting_frog",
        () -> new ItemCastingFrog(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemEssenceStone> ESSENCE_STONE_ITEM = item("essence_stone",
        () -> new ItemEssenceStone(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemPatternScript> SCRIPT_ITEM = item("script",
        () -> new ItemPatternScript(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemCopingSaw> COPING_SAW_ITEM = item("coping_saw",
        () -> new ItemCopingSaw(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemLibraryCard> LIBRARY_CARD_ITEM = item("library_card",
        () -> new ItemLibraryCard(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<Item> EMPTY_JAR_ITEM = item("empty_jar",
        () -> new Item(defaultSettings().stacksTo(16)));

    public static final RegistrySupplier<ItemMindJar> MIND_JAR_ITEM = item("mind_jar",
        () -> new ItemMindJar(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemCosmeticEnergizer> COSMETIC_ENERGIZER_ITEM = item("cosmetic_energizer",
        () -> new ItemCosmeticEnergizer(defaultSettings().stacksTo(1)));

    public static final RegistrySupplier<ItemSlateCanvas> SLATE_CANVAS_ITEM = item("slate_canvas",
        () -> new ItemSlateCanvas(defaultSettings()));

    // fidgets -- todo: maybe move these out of here and into their own file

    public static final RegistrySupplier<ItemFidget> COPPER_PEN_FIDGET = item("copper_pen_fidget", 
        () -> new ItemFidget(defaultSettings().stacksTo(1), 
        new FidgetSettings(2, Math.PI*0.75, 0xC0_C15A36, 0xC0_CFA0F2, 0xFF_6D3422, 0xFF_54398A){
            @Override
            public Tuple<Integer, Integer> getCurveOptions(int index, boolean isCurrent, boolean isSelected){
                if(isCurrent || isSelected)
                    return new Tuple<Integer, Integer>(RenderUtils.buildCurveOptions(1, 1, false, false), 2);
                return new Tuple<Integer, Integer>(RenderUtils.buildCurveOptions(0, 0, false, false), 10);
            }
        }));

    public static final RegistrySupplier<ItemDyeableFidget> DREIDEL_FIDGET = item("dreidel_fidget", 
        () -> new ItemDyeableFidget(defaultSettings().stacksTo(1), 
        new FidgetSettings(4, Math.PI*0.75, 0xC0_49acff, 0xC0_ffe44a, 0xFF_3254ab, 0xFF_f2a600)
        ));

    public static final RegistrySupplier<ItemFidget> RAINBOW_AMOGUS_FIDGET = item("rainbow_amogus_fidget", 
        () -> new ItemFidget(defaultSettings().stacksTo(1), new FidgetSettings(6){
            @Override
            public Label getDefaultLabel(int i, boolean isCurrent, boolean isSelected){
                if(i == 0){
                    return new PatternLabel(HexPattern.fromAngleString("dewdeqawwqwwedwewdweqaqedaqw", HexDir.EAST), 
                        new PatternOptions(0xFF_FFFFFF,//(isCurrent ? DARK_AMOGUS[0] : MID_AMOGUS[0]), 
                        (isCurrent ? MID_AMOGUS[0] : LIGHT_AMOGUS[0]) & 0x00_FFFFFF | 0x40_000000, 
                        (isCurrent ? MID_AMOGUS[0] : LIGHT_AMOGUS[0]) & 0x00_FFFFFF | 0x60_000000, 
                        // , 0x40_BFBFBF, 0x60_BFBFBF,
                        (isCurrent ? MID_AMOGUS[0] : LIGHT_AMOGUS[0]), 0f, 0f, false));
                }
                return super.getDefaultLabel(i, isCurrent, isSelected);
            }

            // picked from the amogus texture
            public static int[] DARK_AMOGUS = {0xFF_990001, 0xFF_8C3D00, 0xFF_8A8C00, 0xFF_218C00, 0xFF_00678B, 0xFF_4D008C};
            public static int[] MID_AMOGUS = {0xFF_CC1413, 0xFF_E56F17, 0xFF_E2E518, 0xFF_47E519, 0xFF_15AEE5, 0xFF_8916E6};
            public static int[] LIGHT_AMOGUS = {0xFF_FF6666, 0xFF_FFA865, 0xFF_FCFF65, 0xFF_8AFF66, 0xFF_66D6FF, 0xFF_BA66FF};

            @Override
            public int getColorFill(int index, int vI, int numOuter, int numInner, boolean isInner, boolean isCurrent, boolean isSelected){
                return (isCurrent ? MID_AMOGUS[index] : LIGHT_AMOGUS[index]) & 0x00_FFFFFF | 0xC0_000000;
            }
    
            @Override
            public int getColorOutline(int index, int vI, boolean isCurrent, boolean isSelected){
                return isCurrent ? DARK_AMOGUS[index] : MID_AMOGUS[index];
            }
        }));

    public static final String[] NEW_STAFF_IDS = {"bone_staff", "quartz_staff", "carrot_staff", "bee_staff", "rod_staff",
        "ice_staff", "blaze_staff", "wither_staff", "pumpkin_staff", "candy_cane_staff", "menorah_staff", "christmas_tree_staff", "lovely_staff", "longinus_staff", "owl_staff", "ghost_staff", "celestial_staff"};
    static {
        for(String id : NEW_STAFF_IDS){
            item(id, () -> new ItemStaff(defaultSettings().stacksTo(1)));
        }
    }
    

    public static <T extends Item> RegistrySupplier<T> item(String name, Supplier<T> item) {
		return item(name, item, true);
	}

    public static <T extends Item> RegistrySupplier<T> item(String name, Supplier<T> item, boolean inTab) {
		RegistrySupplier<T> reg = items.register(ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, name), item);
        if(inTab) TAB_ITEMS.add(reg);
		return reg;
	}

    public static Item.Properties defaultSettings(){
        return new Item.Properties();
    }

    public static DeferredRegister<CreativeModeTab> tabs = DeferredRegister.create(HexGloop.MOD_ID, net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> HEX_GLOOP_GROUP = tabs.register("general", () -> CreativeTabRegistry.create(builder -> builder
        .title(net.minecraft.network.chat.Component.translatable("itemGroup.hexgloop.general"))
        .icon(() -> GLOOP_ITEM.get().getDefaultInstance())
        .displayItems((params, output) -> {
            for(RegistrySupplier<? extends Item> item : TAB_ITEMS){
                output.accept(item.get());
            }
        })));

    public static void register(){
        items.register();
        tabs.register();
    }
}
