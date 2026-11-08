package com.samsthenerd.hexgloop.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.items.tooltips.ScriptTooltipData;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.samsthenerd.hexgloop.utils.StackNBT;
import at.petrak.hexcasting.client.gui.PatternTooltipComponent;

public class ItemPatternScript extends Item implements IotaHolderItem{

    public static final String TAG_DATA = "data";
    public static final int MAX_PATTERN_COUNT = 16;

    public ItemPatternScript(Properties settings){
        super(settings);
    }

    // erase or check for if it only has patterns
    @Override
    public boolean canWrite(ItemStack stack, @Nullable Iota iota){
        return iota == null || patternsOnly(iota) != null;
    }

    public Iota patternsOnly(Iota iota){
        if(iota instanceof PatternIota){
            return iota;
        }
        if(iota instanceof ListIota lIota){
            // limit number of patterns
            if(lIota.getList().size() > MAX_PATTERN_COUNT){
                return null;
            }
            for(Iota inIota : lIota.getList()){
                // null if there's a non-pattern iota in there
                if(!(inIota instanceof PatternIota)){
                    return null;
                }
            }
            return iota;
        }
        return null;
    }

    @Override
    public Iota readIota(ItemStack stack){
        return StackNBT.getIota(stack, TAG_DATA);
    }

    @Override
    public boolean writeable(ItemStack stack){
        return true;
    }

    @Override
    public void writeDatum(ItemStack stack, @Nullable Iota iota){
        if (iota == null) {
            StackNBT.remove(stack, TAG_DATA);
        } else {
            StackNBT.putIota(stack, TAG_DATA, patternsOnly(iota));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        try{
            Iota storedIota = readIota(stack);
            if(storedIota instanceof ListIota lIota){
                List<HexPattern> patterns = new ArrayList<>();
                for(Iota iota : lIota.getList()){
                    if(iota instanceof PatternIota pIota){
                        patterns.add(pIota.getPattern());
                    }
                }
                return Optional.of(new ScriptTooltipData(patterns, PatternTooltipComponent.ANCIENT_BG));
            }
        } catch (Exception e){

        }
        return Optional.empty();
    }
}
