package com.samsthenerd.hexgloop.items;

import com.samsthenerd.hexgloop.casting.orchard.IOrchard;
import com.samsthenerd.hexgloop.keybinds.HexGloopKeybinds;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker.Label;
import com.samsthenerd.hexgloop.misc.wnboi.LabelProvider;
import com.samsthenerd.hexgloop.misc.wnboi.LabelTypes.PatternLabel;
import com.samsthenerd.hexgloop.misc.wnboi.LabelTypes.PatternLabel.PatternOptions;
import com.samsthenerd.hexgloop.screens.FidgetWheelScreen;
import com.samsthenerd.wnboi.interfaces.KeyboundItem;
import com.samsthenerd.wnboi.screen.AbstractContextWheelScreen;

import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemFidget extends Item implements LabelyItem, KeyboundItem{
    public FidgetSettings fidgetSettings;

    public static ResourceLocation INDEX_PREDICATE = ResourceLocation.fromNamespaceAndPath("hexgloop", "fidget_index");

    public ItemFidget(Properties settings, FidgetSettings fidgetSettings){
        super(settings);
        this.fidgetSettings = fidgetSettings;
    }

    
    public static class FidgetSettings{
        public int slots;
        public double startAngle;
        public int defFillColor;
        public int selFillColor;
        public int defStrokeColor;
        public int selStrokeColor;

        public static HexPattern[] NUMBER_LITERALS = {HexPattern.fromAngleString("aqaa", HexDir.SOUTH_EAST), 
            HexPattern.fromAngleString("aqaaw", HexDir.SOUTH_EAST),
            HexPattern.fromAngleString("aqaawa", HexDir.SOUTH_EAST),
            HexPattern.fromAngleString("aqaawaw", HexDir.SOUTH_EAST),
            HexPattern.fromAngleString("aqaawaa", HexDir.SOUTH_EAST),
            HexPattern.fromAngleString("aqaaed", HexDir.SOUTH_EAST),
            HexPattern.fromAngleString("aqaaqw", HexDir.SOUTH_EAST),
            HexPattern.fromAngleString("aqaawaq", HexDir.SOUTH_EAST),
            HexPattern.fromAngleString("aqaawwaa", HexDir.SOUTH_EAST),
        };

        public FidgetSettings(int slots, double startAngle, int defFillColor, int selFillColor, int defStrokeColor, int selStrokeColor){
            this.slots = slots;
            this.startAngle = startAngle;
            this.defFillColor = defFillColor;
            this.selFillColor = selFillColor;
            this.defStrokeColor = defStrokeColor;
            this.selStrokeColor = selStrokeColor;
        }

        public FidgetSettings(int slots){
            this(slots, Math.PI / 2 + Math.PI / slots, 0xC0_E8E8E8, 0xC0_FFFFFF, 0xFF_BFBFBF, 0xFF_E8E8E8);
        }

        public Label getDefaultLabel(int i, boolean isCurrent, boolean isSelected){
            if(i >= 0 && i < NUMBER_LITERALS.length){
                return new PatternLabel(NUMBER_LITERALS[i+1], new PatternOptions((isCurrent ? selStrokeColor : defStrokeColor), 
                    (isCurrent ? selFillColor : defFillColor) & 0x00_FFFFFF | 0x40_000000, 
                    (isCurrent ? selFillColor : defFillColor) & 0x00_FFFFFF | 0x60_000000, (isCurrent ? selFillColor : defFillColor), 0f, 0f, false));
            }
            return null;
        }

        public Tuple<Integer, Integer> getCurveOptions(int index, boolean isCurrent, boolean isSelected){
            return new Tuple<Integer, Integer>(0, 5);
        }

        // expose this out here for full control?
        public int getColorFill(int index, int vI, int numOuter, int numInner, boolean isInner, boolean isCurrent, boolean isSelected){
            return isCurrent ? selFillColor : defFillColor;
        }

        public int getColorOutline(int index, int vI, boolean isCurrent, boolean isSelected){
            return isCurrent ? selStrokeColor : defStrokeColor;
        }
    }

    @Override
    public KeyMapping getKeyBinding(){
        return HexGloopKeybinds.IOTA_WHEEL_KEYBIND;
    }
    public static FidgetWheelScreen screen;
    public AbstractContextWheelScreen getScreen(){
        Tuple<ItemStack, Boolean> handItemResult = getHandItem();
        if(screen == null){
            ItemStack fidgetStack = handItemResult.getA();
            if(fidgetStack == null) return null;
            screen = new FidgetWheelScreen(new FidgetLabelProvider(fidgetStack), Minecraft.getInstance().screen, fidgetSettings);
        }
        ((FidgetLabelProvider) screen.labelProvider).mainHand = handItemResult.getB();
        return screen;
    }

    public boolean putLabel(ItemStack stack, CompoundTag labelNbt){
        return LabelMaker.putLabel(stack, labelNbt, ItemFidget.getPage(stack));
    }

    public static int getPage(ItemStack stack) {
        Integer selected = stack.get(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get());
        if (selected != null) {
            int index = selected;
            if (index == 0) {
                index = 1;
            }
            return index;
        } else {
            return 1;
        }
    }

    public static int rotatePageIdx(ItemStack stack, boolean increase) {
        int idx = getPage(stack);
        int numSlots = 1;
        if(stack.getItem() instanceof ItemFidget fidgetItem){
            numSlots = fidgetItem.fidgetSettings.slots;
        }
        if (idx != 0) {
            idx += increase ? 1 : -1;
            if(idx <= 0) idx = numSlots;
            if(idx > numSlots) idx = 1;
            idx = Math.max(1, idx);
        }
        idx = Mth.clamp(idx, 0, numSlots);
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
    public Tuple<ItemStack, Boolean> getHandItem(){
        ItemStack mainStack = Minecraft.getInstance().player.getMainHandItem();
        if(mainStack.getItem() instanceof ItemFidget){
            return new Tuple<ItemStack, Boolean>(mainStack, true);
        }
        ItemStack offStack = Minecraft.getInstance().player.getOffhandItem();
        if(offStack.getItem() instanceof ItemFidget){
            return new Tuple<ItemStack, Boolean>(offStack, false);
        }
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if(!world.isClientSide() && user instanceof ServerPlayer serverPlayer){
            ItemStack fidgetStack = user.getItemInHand(hand);
            ((IOrchard)serverPlayer).setOrchardValue(getPage(fidgetStack));
        }
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }

    
    public static class FidgetLabelProvider implements LabelProvider{
        public ItemStack fidgetStack = null;
        protected LabelMaker labelMaker = null;
        public boolean mainHand = true;

        // be careful to only pass this a spellbook
        public FidgetLabelProvider(ItemStack fidgetStack){
            super();
            if(fidgetStack != null && fidgetStack.getItem() instanceof ItemFidget){
                this.fidgetStack = fidgetStack;
                labelMaker = new LabelMaker(fidgetStack);
            }
        }

        public LabelMaker getLabelMaker(){
            return labelMaker;
        }

        public int currentSlot(){
            return ItemFidget.getPage(fidgetStack);
        }

        public int perPage(){
            int numSlots = -1; // idk, not really a great way to handle an error, it should never happen
            if(fidgetStack.getItem() instanceof ItemFidget fidgetItem){
                numSlots = fidgetItem.fidgetSettings.slots;
            }
            return numSlots;
        }

        public int getCount(){
            return perPage(); 
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
    }
}
