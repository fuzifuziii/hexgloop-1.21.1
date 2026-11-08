package com.samsthenerd.hexgloop.mixins.lociathome;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.casting.inventorty.InventortyUtils.KittyContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

@Mixin(CastingEnvironment.class)
public class MixinContextHelper implements IContextHelper{
    private ItemStack kittyStack = null;

    public void setKitty(ItemStack kitty){
        kittyStack = kitty;
    }

    public ItemStack getKitty(){
        return kittyStack;
    }

    private SlotAccess cursorRef = null;

    public void setCursorRef(SlotAccess cursorRef){
        this.cursorRef = cursorRef;
    }

    public SlotAccess getCursorRef(){
        return cursorRef;
    }

    private KittyContext kittyContext = null;

    public void setKittyContext(KittyContext kCtx){
        kittyContext = kCtx;
    }

    public KittyContext getKittyContext(){
        return kittyContext;
    }

    private ItemStack frogStack = null;
    
    public void setFrog(ItemStack frog){
        frogStack = frog;
    }

    public ItemStack getFrog(){
        return frogStack;
    }

    // moving to this in the future but don't feel like rewriting to use it for frog and cat rn bleh - should probably also move it to a different folder

    private ItemStack castingStack = null;

    public ItemStack getCastingItem(){
        return castingStack;
    }

    public void setCastingItem(ItemStack stack){
        castingStack = stack;
    }
}
