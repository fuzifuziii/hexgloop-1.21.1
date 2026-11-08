package com.samsthenerd.hexgloop.items;

import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class ItemSlateCanvas extends MapItem implements IotaHolderItem {
    public ItemSlateCanvas(Properties settings) {
        super(settings);
    }

    // don't do updates
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide) {
            MapItemSavedData mapState = getSavedData(stack, world); // does this do anything ?
        }
    }

    // just a way to get a new one
    public static Tuple<MapItemSavedData, Integer> createMapState(Level world){
        ItemStack normalMapStack = MapItem.create(world, 0, 0, (byte) 0, false, false);
        MapItemSavedData mapState = MapItem.getSavedData(normalMapStack, world);
        MapId mapIdComp = normalMapStack.get(DataComponents.MAP_ID);
        Integer id = mapIdComp == null ? null : mapIdComp.id();
        return new Tuple<MapItemSavedData, Integer>(mapState, id);
    }

    public static void setCanvasMapId(ItemStack stack, int mapId){
        stack.set(DataComponents.MAP_ID, new MapId(mapId));
    }

    @Override
    @Nullable
    public Iota readIota(ItemStack stack){
        return null;
    }

    @Override
    public boolean writeable(ItemStack stack){
        return true;
    }

    @Override
    public boolean canWrite(ItemStack stack, @Nullable Iota iota){
        return iota == null;
    }

    // just using this to allow erasing the map
    @Override
    public void writeDatum(ItemStack stack, @Nullable Iota iota){
        if(iota == null){
            stack.remove(DataComponents.MAP_ID);
        }
    }
}
