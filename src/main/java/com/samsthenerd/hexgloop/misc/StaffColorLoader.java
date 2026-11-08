package com.samsthenerd.hexgloop.misc;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.samsthenerd.hexgloop.HexGloop;

import dev.architectury.registry.ReloadListenerRegistry;

public class StaffColorLoader extends SimpleJsonResourceReloadListener{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final Map<ResourceLocation, Integer> STAFF_COLORS = new HashMap<>();

    // 1.21: DyeableLeatherItem is gone; dye colour lives in DataComponents.DYED_COLOR.
    // Fallback chain: dyed colour -> STAFF_COLORS map -> default pink.
    // Lives here, not in MixinInjectDyeableStaff: Mixin rejects non-private static methods.
    public static int getStaffColor(ItemStack stack) {
        if (stack.has(DataComponents.DYED_COLOR)) {
            return DyedItemColor.getOrDefault(stack, 0xff7e4f);
        }
        ResourceLocation stackId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Integer color = STAFF_COLORS.get(stackId);
        if (color != null) {
            return color;
        }
        return 0xff7e4f;
    }

    public StaffColorLoader() {
        super(GSON, "staffcolors");
    }

    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        STAFF_COLORS.clear(); // maybe ?
        for(Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()){
            ResourceLocation id = entry.getKey();
            JsonObject obj = entry.getValue().getAsJsonObject();
            for(Map.Entry<String, JsonElement> staff_entry : obj.entrySet()){
                JsonPrimitive colorVal = staff_entry.getValue().getAsJsonPrimitive();
                int color = 0;
                if(colorVal.isNumber()){
                    color = colorVal.getAsInt();
                } else if(colorVal.isString()) {
                    String colorStr = colorVal.getAsString();
                    colorStr = colorStr.replace("#", "");
                    try {
                        color = Integer.parseInt(colorStr, 16);
                    } catch (NumberFormatException e) {
                        HexGloop.LOGGER.error("Invalid color value for staff color: " + staff_entry.getKey() + " in " + id.toString());
                        continue;
                    }
                } else {
                    HexGloop.LOGGER.error("Invalid color value for staff color: " + staff_entry.getKey() + " in " + id.toString());
                    continue;
                }
                
                STAFF_COLORS.put(ResourceLocation.parse(staff_entry.getKey()), color);
            }
        }
    }

    // register the event(s)
    public static void init(){
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new StaffColorLoader());
    }
}