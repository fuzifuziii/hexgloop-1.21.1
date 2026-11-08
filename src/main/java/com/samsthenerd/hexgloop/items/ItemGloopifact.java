package com.samsthenerd.hexgloop.items;

import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.HexDataComponents;

public class ItemGloopifact extends ItemPackagedHex implements IotaHolderItem {
    public ItemGloopifact(Properties settings){
        super(settings);
    }

    public boolean breakAfterDepletion(){
        return false;
    }

    // might make this toggleable
    public boolean canDrawMediaFromInventory(ItemStack stack){
        return true;
    }

    @Override
    public void clearHex(ItemStack stack) {
        if(readIota(stack) == null){ // hopefully protect against clearing the hex before clearing the iota
            super.clearHex(stack);
        }
    }

    // readIota is inherited from IotaHolderItem (HexDataComponents.IOTA_HOLDER_IOTA)

    public boolean writeable(ItemStack stack){
        return hasHex(stack);
    }

    public boolean canWrite(ItemStack stack, @Nullable Iota iota){
        return iota == null || hasHex(stack);
    }

    public void writeDatum(ItemStack stack, @Nullable Iota iota){
        if(iota == null){
            // erase ! although maybe need to handle it so it erases iota before packaged hex
            stack.remove(HexDataComponents.IOTA_HOLDER_IOTA.get());
        } else {
            stack.set(HexDataComponents.IOTA_HOLDER_IOTA.get(), iota);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pIsAdvanced);
    }

    public int cooldown(){
        return 0;
    }
}
