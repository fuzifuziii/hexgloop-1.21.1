package com.samsthenerd.hexgloop.items;

import java.util.UUID;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;

public class ItemHeartPigment extends Item implements PigmentItem{
    public ItemHeartPigment(Properties properties) {
        super(properties);
    }

    @Override
    public ColorProvider provideColor(ItemStack stack, UUID owner) {
        return new ColorProvider() {
            @Override
            protected int getRawColor(float time, Vec3 position) {
                return ADPigment.morphBetweenColors(getColors(), new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);
            }
        };
    }

    public int[] getColors(){
        return new int[]{0x69eb96, 0xeb69d7};
    }

}
