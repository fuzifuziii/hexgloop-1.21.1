package com.samsthenerd.hexgloop.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import com.samsthenerd.hexgloop.HexGloop;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.lib.HexDataComponents;

public class GloopUtils {
    // 1.21 / Hex 0.12: TAG_OVERRIDE_VISUALLY and readIotaTag are gone. The visual override
    // is now HexDataComponents.VISUAL_OVERRIDE (Optional<IotaType<?>>): a present type gives
    // its colour, an empty one is the old rainbow fallback. Otherwise the colour comes from
    // readIota(stack).getType().color(), or HexUtils.ERROR_COLOR when there is no iota.
    // Kept as a local copy of the IotaHolderItem.getColor logic, as before, because
    // Dyebooks' getColor messes up our mappings.
    public static int getIotaColor(ItemStack stack){
        var override = stack.get(HexDataComponents.VISUAL_OVERRIDE.get());
        if (override != null) {
            if (override.isPresent()) {
                return override.get().color();
            }
            return 0xFF000000 | Mth.hsvToRgb(ClientTickCounter.getTotal() * 2 % 360 / 360F, 0.75F, 1F);
        }

        if(!(stack.getItem() instanceof IotaHolderItem iotaHolder)) return 0xFFFFFF;

        var iota = iotaHolder.readIota(stack);
        if (iota == null) {
            return HexUtils.ERROR_COLOR;
        }

        return iota.getType().color();
    }

    private static Supplier<ItemLike>[] CACHED_STAFFS = null;

    public static Supplier<ItemLike>[] getAllStaffs(){
        if(CACHED_STAFFS != null) return CACHED_STAFFS;
        List<Supplier<ItemLike>> staffs = new ArrayList<Supplier<ItemLike>>();
        Named<Item> entryList = BuiltInRegistries.ITEM.getOrCreateTag(HexTags.Items.STAVES);
        HexGloop.logPrint("getting all " + entryList.size() + " staffs");
        for(Holder<Item> entry : entryList){
            entry.unwrap().ifRight(item -> {
                HexGloop.logPrint("found staff: " + item.getDescriptionId());
                staffs.add(() -> item);
            });
            entry.unwrap().ifLeft(key -> {
                HexGloop.logPrint("found staff: " + key.location());
                staffs.add(() -> BuiltInRegistries.ITEM.get(key));
            });
        }
        CACHED_STAFFS = staffs.toArray(new Supplier[0]);
        return CACHED_STAFFS;
    }

    public static String prettyPrintID(ResourceLocation id){
        String prettyPrinted = "";
        String noSymbols = id.getPath().replace("_", " ").replace("/", ", ");
        for(String word : noSymbols.split(" ")){
            prettyPrinted += word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
        }
        if(id.getNamespace() != "minecraft"){
            prettyPrinted += "[" + id.getNamespace() + "]";
        }
        return prettyPrinted;
    }
}
