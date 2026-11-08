package com.samsthenerd.hexgloop.misc.wnboi;

import java.util.Map;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class SpellbookIotaProvider implements IotaProvider{
    ItemStack spellbook = null;
    protected LabelMaker labelMaker = null;
    public boolean mainHand = true;
    public static final RandomSource RANDOM = RandomSource.create();

    // be careful to only pass this a spellbook
    public SpellbookIotaProvider(ItemStack _spellbook){
        super();
        if(_spellbook != null && _spellbook.getItem() instanceof ItemSpellbook){
            spellbook = _spellbook;
            labelMaker = new LabelMaker(spellbook);
        }
    }

    public void updateItemStack(ItemStack newSpellbook){
        spellbook = newSpellbook;
        labelMaker = new LabelMaker(spellbook);
    }

    @Override
    public int getCount(){
        return ItemSpellbook.MAX_PAGES;
    }

    @Override
    public int perPage(){
        return 8;
    }

    @Override
    public LabelMaker getLabelMaker(){
        return labelMaker;
    }

    // -1 if empty
    @Override
    public int currentSlot(){
        return ItemSpellbook.getPage(spellbook, -1);
    }

    // Hex 0.12: TAG_PAGES is gone; pages live in HexDataComponents.SPELLBOOK_PAGES (Map<String, Iota>).
    // Encode the iota with the codec helper so callers still get a CompoundTag.
    @Override
    public CompoundTag getIotaNBT(int index){
        int idx = index + 1;
        String key = String.valueOf(idx);
        Map<String, Iota> pages = spellbook.get(HexDataComponents.SPELLBOOK_PAGES.get());
        if (pages == null) {
            return null;
        }
        Iota iota = pages.get(key);
        if (iota == null) {
            return null;
        }
        Tag encoded = StackNBT.encodeIota(iota, null);
        return encoded instanceof CompoundTag compound ? compound : null;
    }

    @Override
    public void toSlot(int index){
        int current = currentSlot()-1;
        int dist = Math.abs(index - current);
        int invert = (dist == (index - current)) ? -1 : 1;
        for(int i = 0; i < dist; i++){
            // Hex 0.12: MsgShiftScrollSyn -> MsgShiftScrollC2S(double, double, boolean)
            IClientXplatAbstractions.INSTANCE.sendPacketToServer(
                    new MsgShiftScrollC2S(mainHand ? invert*dist : 0, !mainHand ? invert*dist : 0, true));
        }
    }

    @Override
    public RandomSource getRNG(){
        return RANDOM;
    }

    // Hex 0.12: TAG_PAGE_NAMES is gone; names live in HexDataComponents.SPELLBOOK_PAGE_NAMES
    // (Map<String, Component>) already decoded, no fromJson needed.
    @Override
    public Component getName(int index){
        int idx = index + 1;
        String key = String.valueOf(idx);
        Map<String, Component> names = spellbook.get(HexDataComponents.SPELLBOOK_PAGE_NAMES.get());
        if (names == null) {
            return null;
        }
        return names.get(key);
    }

}
