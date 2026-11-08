package com.samsthenerd.hexgloop.items;

import java.util.List;

import javax.annotation.Nullable;

import com.samsthenerd.hexgloop.keybinds.HexGloopKeybinds;
import com.samsthenerd.hexgloop.misc.wnboi.IotaProvider;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker;
import com.samsthenerd.hexgloop.screens.IotaWheelScreen;
import com.samsthenerd.wnboi.interfaces.KeyboundItem;
import com.samsthenerd.wnboi.screen.AbstractContextWheelScreen;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import com.samsthenerd.hexgloop.utils.StackNBT;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

// it's functionally just a restricted spellbook (that's not end locked)
public class ItemMultiFocus extends Item implements KeyboundItem, IotaHolderItem, LabelyItem{
    public final static int MAX_FOCI_SLOTS = 6;

    ItemStack multifocus = null;

    @Override
    public KeyMapping getKeyBinding(){
        return HexGloopKeybinds.IOTA_WHEEL_KEYBIND;
    }

    public ItemMultiFocus(Properties settings){
        super(settings);
    }
    @Override
    public AbstractContextWheelScreen getScreen(){
        Tuple<ItemStack, Boolean> handItemResult = getHandItem();
        multifocus = handItemResult.getA();
        Screen oldScreen = Minecraft.getInstance().screen;
        if(IotaWheelScreen.CURRENT == null || !(IotaWheelScreen.CURRENT.iotaProvider instanceof MultiFocusIotaProvider)){
            // HexGloop.logPrint("multifocus is" + (multifocus == null ? "null" : multifocus.getName().toString()));
            IotaWheelScreen.CURRENT = new IotaWheelScreen(new MultiFocusIotaProvider(multifocus), oldScreen);
        }
        ((MultiFocusIotaProvider) IotaWheelScreen.CURRENT.iotaProvider).updateItemStack(multifocus);
        ((MultiFocusIotaProvider) IotaWheelScreen.CURRENT.iotaProvider).mainHand = handItemResult.getB();
        IotaWheelScreen.CURRENT.onPage = (IotaWheelScreen.CURRENT.iotaProvider.currentSlot()-1) / IotaWheelScreen.CURRENT.iotaProvider.perPage();
        return IotaWheelScreen.CURRENT;
    }
    public Tuple<ItemStack, Boolean> getHandItem(){
        ItemStack mainStack = Minecraft.getInstance().player.getMainHandItem();
        if(mainStack.getItem() instanceof ItemMultiFocus){
            return new Tuple<ItemStack, Boolean>(mainStack, true);
        }
        ItemStack offStack = Minecraft.getInstance().player.getOffhandItem();
        if(offStack.getItem() instanceof ItemMultiFocus){
            return new Tuple<ItemStack, Boolean>(offStack, false);
        }
        return null;
    }

    @Override
    public void writeDatum(ItemStack stack, @Nullable Iota iota){
        HexItems.SPELLBOOK.get().writeDatum(stack, iota);
    }

    public boolean writeable(ItemStack stack){
        return !ItemSpellbook.isSealed(stack);
    }

    public boolean canWrite(ItemStack stack, @Nullable Iota iota){
        return iota == null || !ItemSpellbook.isSealed(stack);
    }

    @Override
    @Nullable
    public Iota readIota(ItemStack stack){
        return HexItems.SPELLBOOK.get().readIota(stack);
    }

    @Nullable
    public Iota readSlotIota(ItemStack stack, int index){
        return stack.getOrDefault(HexDataComponents.SPELLBOOK_PAGES.get(), Map.<String, Iota>of()).get(String.valueOf(index));
    }

    public static int setPageIdx(ItemStack stack, int indexRaw){
        int originalIdx = ItemSpellbook.getPage(stack, 0);
        int index = Mth.clamp(indexRaw, 1, MAX_FOCI_SLOTS);
        if(originalIdx == 0){ return 0; };
        for(int i = 0; i < Math.abs(index - originalIdx); i++){
            boolean increase = index > originalIdx;
            rotatePageIdx(stack, increase);
        }
        return index;
    }

    public static int rotatePageIdx(ItemStack stack, boolean increase) {
        int idx = ItemSpellbook.getPage(stack, 0);
        if (idx != 0) {
            idx += increase ? 1 : -1;
            if(idx <= 0) idx = MAX_FOCI_SLOTS;
            if(idx > MAX_FOCI_SLOTS) idx = 1;
            idx = Math.max(1, idx);
        }
        idx = Mth.clamp(idx, 0, MAX_FOCI_SLOTS);
        stack.set(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get(), idx);

        Map<String, Component> names = stack.getOrDefault(HexDataComponents.SPELLBOOK_PAGE_NAMES.get(), Map.<String, Component>of());
        int shiftedIdx = Math.max(1, idx);
        Component name = names.get(String.valueOf(shiftedIdx));
        if (name != null) {
            stack.set(DataComponents.CUSTOM_NAME, name);
        } else {
            stack.remove(DataComponents.CUSTOM_NAME);
        }

        return idx;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        int index = ItemSpellbook.getPage(stack, 0);
        stack.set(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get(), index);

        int shiftedIdx = Math.max(1, index);
        String nameKey = String.valueOf(shiftedIdx);
        Map<String, Component> names = new HashMap<>(stack.getOrDefault(HexDataComponents.SPELLBOOK_PAGE_NAMES.get(), Map.<String, Component>of()));
        Component custom = stack.get(DataComponents.CUSTOM_NAME);
        if (custom != null) {
            names.put(nameKey, custom);
        } else {
            names.remove(nameKey);
        }
        stack.set(HexDataComponents.SPELLBOOK_PAGE_NAMES.get(), names);
    }

    // render tooltips the same way spellbook does
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag isAdvanced) {
        HexItems.SPELLBOOK.get().appendHoverText(stack, context, tooltip, isAdvanced);
    }

    @Nullable
    public boolean putLabel(ItemStack stack, CompoundTag labelNbt){
        int index = ItemSpellbook.getPage(stack, 0);
        return putLabel(stack, index, labelNbt);
    }

    public class MultiFocusIotaProvider implements IotaProvider{
        protected ItemStack multifocus = null;
        protected LabelMaker labelMaker = null;
        public boolean mainHand = true;
        public static final RandomSource RANDOM = RandomSource.create();

        // be careful to only pass this a spellbook
        public MultiFocusIotaProvider(ItemStack _multifocus){
            super();
            if(_multifocus != null && _multifocus.getItem() instanceof ItemMultiFocus){
                multifocus = _multifocus;
                labelMaker = new LabelMaker(multifocus);
            }
        }

        public void updateItemStack(ItemStack newMulitFocus){
            multifocus = newMulitFocus;
            labelMaker = new LabelMaker(multifocus);
        }

        @Override
        public int getCount(){
            return MAX_FOCI_SLOTS;
        }

        @Override
        public int perPage(){
            return Math.min(MAX_FOCI_SLOTS, 8);
        }

        // -1 if empty
        @Override
        public int currentSlot(){
            return ItemSpellbook.getPage(multifocus, -1);
        }

        @Override
        public LabelMaker getLabelMaker(){
            return labelMaker;
        }

        @Override
        public CompoundTag getIotaNBT(int index){
            Iota iota = readSlotIota(multifocus, index + 1);
            if(iota == null) return null;
            return StackNBT.encodeIota(iota, null) instanceof CompoundTag ct ? ct : null;
        }

        @Override
        public void toSlot(int index){
            int current = currentSlot()-1;
            // HexGloop.logPrint("going to slot " + index + " from slot " + current);
            int dist = Math.abs(index - current);
            int invert = (dist == (index - current)) ? -1 : 1;
            for(int i = 0; i < dist; i++){
                IClientXplatAbstractions.INSTANCE.sendPacketToServer(
                        new MsgShiftScrollC2S(mainHand ? invert*dist : 0, !mainHand ? invert*dist : 0, true));
            }
        }

        @Override
        public RandomSource getRNG(){
            return RANDOM;
        }

        @Override
        public Component getName(int index){
            return multifocus.getOrDefault(HexDataComponents.SPELLBOOK_PAGE_NAMES.get(), Map.<String, Component>of()).get(String.valueOf(index + 1));
        }
    }
}
