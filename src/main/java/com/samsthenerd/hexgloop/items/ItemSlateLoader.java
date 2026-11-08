package com.samsthenerd.hexgloop.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import com.samsthenerd.hexgloop.HexGloop;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.TreeList;
import com.samsthenerd.hexgloop.utils.StackNBT;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.minecraft.world.item.Item;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexItems;

public class ItemSlateLoader extends ItemAbstractPassThrough implements IotaHolderItem {
    public static final int MAX_STACK_SIZE = 64; // Item.MAX_STACK_SIZE was removed in 1.21
    public static final String TAG_PATTERN_LIST = "pattern_list";
    public static final String TAG_SLATE_COUNT = "slate_count";
    public static final ResourceLocation ACTIVATED_PRED = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "is_activated");

    public ItemSlateLoader(Properties pProperties) {
        super(pProperties);
    }

    public ItemStack getStoredItem(ItemStack stack, LivingEntity ent, Level world, InteractionHand hand){
        if(getSlateCount(stack) > 0){
            return getCurrentSlate(stack);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack setStoredItem(ItemStack stack, LivingEntity ent, Level world, InteractionHand hand, ItemStack storedItem){
        // means it got used
        int slateCount = getSlateCount(stack);
        if(slateCount > 0 && storedItem.isEmpty()){
            setSlateCount(stack, --slateCount);
        }
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(world, user, hand);
        if(result.getResult().consumesAction()){
            dequeuePattern(result.getObject(), world);
        }
        return result;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        HexGloop.logPrint("slate loader count in use on block: " + getSlateCount(context.getItemInHand()));
        InteractionResult result = super.useOn(context);
        if(result.consumesAction()){
            dequeuePattern(context.getItemInHand(), context.getLevel());
        }
        HexGloop.logPrint("slate loader updated count in use on block (result): " + getSlateCount(context.getItemInHand()));
        HexGloop.logPrint("slate loader updated count in use on block (hand): " + getSlateCount(context.getPlayer().getItemInHand(context.getHand())));
        return result;
    }

    public boolean shouldPassTools(ItemStack stack){
        return false;
    }

    public ItemStack getStoredItemCopy(ItemStack stack){
        if(getSlateCount(stack) > 0){
            return getCurrentSlate(stack);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Iota readIota(ItemStack stack){
        return StackNBT.getIota(stack, TAG_PATTERN_LIST);
    }

    @Override
    public boolean writeable(ItemStack stack){
        return true;
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return datum instanceof ListIota || datum == null;
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        if (this.canWrite(stack, datum)) {
            if (datum instanceof ListIota list) {
                List<Iota> iotaList = new ArrayList<>();
                // strip out non pattern elements
                list.getList().forEach(iElem -> {
                    iotaList.add(iElem instanceof PatternIota ? iElem : new NullIota());
                });
                if(iotaList.size() > 0){
                    StackNBT.putIota(stack, TAG_PATTERN_LIST, new ListIota(iotaList));
                    setCurrentPattern(stack, iotaList.get(0) instanceof PatternIota ? ((PatternIota) iotaList.get(0)).getPattern() : null);
                    return;
                }
            }
        }
        erasePatterns(stack);
    }

    public boolean hasPatterns(ItemStack stack){
        return StackNBT.contains(stack, TAG_PATTERN_LIST);
    }

    public int getSlateCount(ItemStack stack){
        return StackNBT.getInt(stack, TAG_SLATE_COUNT, 0);
    }

    public void setSlateCount(ItemStack stack, int count){
        StackNBT.putInt(stack, TAG_SLATE_COUNT, count);
    }

    public void dequeuePattern(ItemStack stack, Level world){
        if(world instanceof ServerLevel sWorld && hasPatterns(stack)){
            Iota storedIota = readIota(stack);
            if(storedIota instanceof ListIota list){
                // yay alwinfy magic
                TreeList<Iota> newList = list.getList().tail();
                writeDatum(stack, new ListIota(newList));
            }
        }
    }

    // 1.21: the current pattern lives in hex's PATTERN component (same one a placed slate item uses)
    private void erasePatterns(ItemStack stack){
        stack.remove(HexDataComponents.PATTERN.get());
        StackNBT.remove(stack, TAG_PATTERN_LIST);
    }

    private void setCurrentPattern(ItemStack stack, @Nullable HexPattern pattern){
        if(pattern == null){
            stack.remove(HexDataComponents.PATTERN.get());
        } else {
            stack.set(HexDataComponents.PATTERN.get(), pattern);
        }
    }

    public HexPattern getCurrentPattern(ItemStack stack){
        return stack.get(HexDataComponents.PATTERN.get());
    }

    public ItemStack getCurrentSlate(ItemStack stack){
        ItemStack slateStack = new ItemStack(HexItems.SLATE.get());
        HexPattern pattern = getCurrentPattern(stack);
        if(pattern != null){
            HexItems.SLATE.get().writeDatum(slateStack, new PatternIota(pattern));
        }
        return slateStack;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
        ItemStack slotStack = slot.getItem();
        if(clickType != ClickAction.SECONDARY){
            return false;
        }
        if(slotStack.getItem() == HexItems.SLATE.get()){
            int amtToTake = Math.min(slotStack.getCount(), MAX_STACK_SIZE - getSlateCount(stack));
            if(amtToTake > 0){
                setSlateCount(stack, getSlateCount(stack) + amtToTake);
                slotStack.shrink(amtToTake);
                return true;
            }
        } else if(slotStack.isEmpty()){
            ItemStack newSlateStack = new ItemStack(HexItems.SLATE.get());
            newSlateStack.setCount(getSlateCount(stack));
            slot.set(newSlateStack);
            setSlateCount(stack, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if(clickType != ClickAction.SECONDARY){
            return false;
        }
        if(otherStack.getItem() == HexItems.SLATE.get()){
            int amtToTake = Math.min(otherStack.getCount(), MAX_STACK_SIZE - getSlateCount(stack));
            if(amtToTake > 0){
                setSlateCount(stack, getSlateCount(stack) + amtToTake);
                otherStack.shrink(amtToTake);
                return true;
            }
        } else if(otherStack.isEmpty()){
            ItemStack newSlateStack = new ItemStack(HexItems.SLATE.get());
            newSlateStack.setCount(getSlateCount(stack));
            cursorStackReference.set(newSlateStack);
            setSlateCount(stack, 0);
            return true;
        }
        return false;
    }

    public boolean isBarVisible(ItemStack stack) {
        return getSlateCount(stack) > 0 || hasPatterns(stack);
    }

    public int getBarWidth(ItemStack stack) {
        return 13*getSlateCount(stack) / MAX_STACK_SIZE;
    }

    public int getBarColor(ItemStack stack) {
        float p = getSlateCount(stack) / (float) MAX_STACK_SIZE;
        int emptycolor = 0x5a391c;
        int fullcolor = 0xffbc5e;
        int newColor = 0;
        for(int i = 0; i < 3; i++){
            int empty = (int) (((emptycolor >> (8*i)) & 0xFF) * (1-p));
            int full = (int) (((fullcolor >> (8*i)) & 0xFF) * (p));
            newColor |= (Math.min(empty + full, 0xFF) << (8*i));
        }
        return newColor;
        // return 0x564661; // light-ish slate color ?
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(Component.literal(Integer.toString(getSlateCount(stack))).append("/64"));
        if(hasPatterns(stack)){
            Iota stored = readIota(stack);
            if(stored != null) tooltip.add(stored.display());
        }
    }

    public Component getName(ItemStack stack) {
        if(hasPatterns(stack)){
            Iota iota = HexItems.SLATE.get().readIota(getCurrentSlate(stack));
            return Component.translatable(this.getDescriptionId(stack) + ".written", 
                iota != null ? iota.display() : Component.nullToEmpty(" ")
            );
        } else {
            return Component.translatable(this.getDescriptionId(stack));
        }
    }
}
