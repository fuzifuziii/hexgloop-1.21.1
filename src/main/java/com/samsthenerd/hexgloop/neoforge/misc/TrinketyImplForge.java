package com.samsthenerd.hexgloop.neoforge.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.misc.ITrinkety;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

// it says trinkety but it's curios for neoforge
// 1.21 Curios: slots are declared by datapack (data/hexgloop/curios/...), not by IMC anymore.
public class TrinketyImplForge implements ITrinkety{
    public TrinketyImplForge(){
    }

    @Override
    public boolean isCastingRingEquipped(LivingEntity player){
        return CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.isEquipped(HexGloopItems.CASTING_RING_ITEM.get()))
            .orElse(false);
    }

    public Map<String, List<ItemStack>> getTrinkets(LivingEntity player){
        Map<String, List<ItemStack>> trinketMap = new HashMap<>();
        List<SlotResult> curiosList = CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.findCurios(stack -> true)).orElse(List.of());
        for(SlotResult sr : curiosList){
            String slotId = sr.slotContext().identifier();
            if(standardizedSlots.containsKey(slotId)){
                slotId = standardizedSlots.get(slotId);
            }
            trinketMap.computeIfAbsent(slotId, k -> new ArrayList<>()).add(sr.stack());
        }
        return trinketMap;
    }
}
