package com.samsthenerd.hexgloop.casting;

import javax.annotation.Nullable;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.casting.inventorty.InventortyUtils.KittyContext;
import com.samsthenerd.hexgloop.items.HexGloopItems;

public interface IContextHelper {
    // stuff for kitty casting:

    public default boolean isKitty(){
        return getKitty() != null;
    }

    public void setKittyContext(KittyContext kCtx);

    public KittyContext getKittyContext();

    public ItemStack getKitty();

    public void setKitty(ItemStack kitty);

    public void setCursorRef(SlotAccess cursorRef);

    public SlotAccess getCursorRef();

    // for frog casting:

    public void setFrog(ItemStack frog);

    public ItemStack getFrog();

    @Nullable
    public default FrogVariant getFrogType(){
        ItemStack frog = getFrog();
        if(frog == null){
            return null;
        }
        return HexGloopItems.CASTING_FROG_ITEM.get().getFrogVariant(frog);
    }

    public default boolean isFrogCasting(){
        return getFrog() != null;
    }

    public ItemStack getCastingItem();

    public void setCastingItem(ItemStack stack);
}
