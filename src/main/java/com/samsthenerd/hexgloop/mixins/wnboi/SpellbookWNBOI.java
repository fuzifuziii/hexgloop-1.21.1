package com.samsthenerd.hexgloop.mixins.wnboi;

import org.spongepowered.asm.mixin.Mixin;

import com.samsthenerd.hexgloop.keybinds.HexGloopKeybinds;
import com.samsthenerd.hexgloop.misc.SpellbookScreenInterface;
import com.samsthenerd.hexgloop.misc.wnboi.SpellbookIotaProvider;
import com.samsthenerd.hexgloop.screens.IotaWheelScreen;
import com.samsthenerd.wnboi.interfaces.KeyboundItem;
import com.samsthenerd.wnboi.screen.AbstractContextWheelScreen;

import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemSpellbook.class)
public class SpellbookWNBOI implements KeyboundItem, SpellbookScreenInterface{
    @Override
    public KeyMapping getKeyBinding(){
        return HexGloopKeybinds.IOTA_WHEEL_KEYBIND;
    }

    @Override
    public void clearScreen(){
        IotaWheelScreen.CURRENT = null;
    }

    ItemStack spellbook = null;
    @Override
    public AbstractContextWheelScreen getScreen(){
        Tuple<ItemStack, Boolean> spellbookResult = getSpellbook();
        spellbook = spellbookResult.getA();
        Screen oldScreen = Minecraft.getInstance().screen;
        if(IotaWheelScreen.CURRENT == null || !(IotaWheelScreen.CURRENT.iotaProvider instanceof SpellbookIotaProvider)){
            IotaWheelScreen.CURRENT = new IotaWheelScreen(new SpellbookIotaProvider(spellbook), oldScreen);
        }
        ((SpellbookIotaProvider) IotaWheelScreen.CURRENT.iotaProvider).updateItemStack(spellbook);
        ((SpellbookIotaProvider) IotaWheelScreen.CURRENT.iotaProvider).mainHand = spellbookResult.getB();
        IotaWheelScreen.CURRENT.onPage = (IotaWheelScreen.CURRENT.iotaProvider.currentSlot()-1) / IotaWheelScreen.CURRENT.iotaProvider.perPage();
        
        return IotaWheelScreen.CURRENT;
    }
    public Tuple<ItemStack, Boolean> getSpellbook(){
        ItemStack mainStack = Minecraft.getInstance().player.getMainHandItem();
        if(mainStack.getItem() instanceof ItemSpellbook){
            return new Tuple<ItemStack, Boolean>(mainStack, true);
        }
        ItemStack offStack = Minecraft.getInstance().player.getOffhandItem();
        if(offStack.getItem() instanceof ItemSpellbook){
            return new Tuple<ItemStack, Boolean>(offStack, false);
        }
        return null;
    }
}
