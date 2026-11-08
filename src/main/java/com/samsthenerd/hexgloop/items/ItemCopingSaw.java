package com.samsthenerd.hexgloop.items;

import com.samsthenerd.hexgloop.casting.ContextModificationHandlers.Modification;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

public class ItemCopingSaw extends Item{
    public ItemCopingSaw(Properties settings){
        super(settings);
    }

    public static Modification overcastModifer(CastingEnvironment ctx, Boolean original){
        if(ctx.getCastingEntity() instanceof net.minecraft.world.entity.player.Player caster){
            Inventory pInv = caster.getInventory();
            for(int i = 0; i < Inventory.getSelectionSize(); i++){
                if(pInv.getItem(i).getItem() == HexGloopItems.COPING_SAW_ITEM.get()){
                    return Modification.DISABLE;
                }
            }
        }
        return Modification.NONE;
    }
}
