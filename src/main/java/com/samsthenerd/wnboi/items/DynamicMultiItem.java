package com.samsthenerd.wnboi.items;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.samsthenerd.wnboi.interfaces.KeyboundItem;
import com.samsthenerd.wnboi.mixin.MixinItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

// this is currently not the most polished code ever. use with caution
public abstract class DynamicMultiItem extends Item implements KeyboundItem{
    public int numItems;
    public static String ITEM_COUNT_KEY = "multi_item_count";
    public static String ITEM_STACK_KEY = "multi_item_stack";
    public static String SELECTED_SLOT_KEY = "multi_item_selected";

    // meant so that stuff works like a bundle
    protected boolean actAsBag = false;
    protected int bagStackSize = -1;

    public DynamicMultiItem(Properties settings, int numItems) {
        super(settings);
        this.numItems = numItems;
    }

    // can override if you want more dynamic behavior
    public int getNumItems(ItemStack stack){
        return this.numItems;
    }

    // baseStack is the multiItem and innerStack is the thing that is getting stacked
    public int getMaxStackSize(ItemStack baseStack, ItemStack innerStack){
        int maxStackSize = innerStack.getMaxStackSize();
        if(actAsBag && bagStackSize > 0)
            maxStackSize = bagStackSize;
        return maxStackSize;
    }

    @Override
    public ItemStack getDefaultInstance(){
        ItemStack stack = super.getDefaultInstance();
        CompoundTag stackNBT = new CompoundTag();
        stackNBT.putInt(ITEM_COUNT_KEY, numItems);
        ListTag itemList = new ListTag();
        for(int i = 0; i < numItems; i++){
            itemList.add(saveStack(ItemStack.EMPTY));
        }
        stackNBT.put(ITEM_STACK_KEY, itemList);
        stackNBT.putInt(SELECTED_SLOT_KEY, 0);
        StackNBT.set(stack, stackNBT);
        return stack;
    }

    // TODO(1.21 port): stacks need a registry provider to (de)serialize; use the live server's, else the client's level, else empty
    public static HolderLookup.Provider registries(){
        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if(server != null) return server.registryAccess();
        if(net.neoforged.fml.loading.FMLEnvironment.dist.isClient()){
            net.minecraft.client.multiplayer.ClientLevel lvl = net.minecraft.client.Minecraft.getInstance().level;
            if(lvl != null) return lvl.registryAccess();
        }
        return RegistryAccess.EMPTY;
    }

    public static Tag saveStack(ItemStack stack){
        return stack.saveOptional(registries());
    }

    public static ItemStack loadStack(Tag tag){
        return ItemStack.parseOptional(registries(), (CompoundTag) tag);
    }

    private static ListTag getItemList(ItemStack baseStack){
        return StackNBT.getOrEmpty(baseStack).getList(ITEM_STACK_KEY, Tag.TAG_COMPOUND);
    }

    // puts the addStack into the slot in the baseStack  - doesn't touch the addStack
    public ItemStack putStack(ItemStack baseStack, ItemStack addStack, int slotIndex){
        if(!(baseStack.getItem() instanceof DynamicMultiItem) || slotIndex >= numItems || slotIndex < 0)
            return baseStack;
        ListTag itemList = getItemList(baseStack);
        itemList.set(slotIndex, saveStack(addStack));
        StackNBT.put(baseStack, ITEM_STACK_KEY, itemList);
        return baseStack;
    }

    // used to check if an itemstack can be added with the given slot stack of the basestack
    // returns <if you can put it, final count for slotStack, remaining count for addStack>
    public Triple<Boolean, Integer, Integer> canPutStack(ItemStack baseStack, ItemStack addStack, ItemStack slotStack){
        int maxStackSize = getMaxStackSize(baseStack, addStack);
        if(slotStack.isEmpty()){
            return new ImmutableTriple<Boolean, Integer, Integer>(true, addStack.getCount(), 0);
        } else if(ItemStack.isSameItemSameComponents(slotStack, addStack) && slotStack.getCount() + addStack.getCount() <= maxStackSize){
            int amountLeftOver = Math.max(slotStack.getCount() + addStack.getCount() - maxStackSize, 0);
            int slotAmount = Math.min(maxStackSize, slotStack.getCount() + addStack.getCount());
            return new ImmutableTriple<Boolean, Integer, Integer>(true, slotAmount, amountLeftOver);
        }
        return new ImmutableTriple<Boolean, Integer, Integer>(false, slotStack.getCount(), addStack.getCount());
    }

    // puts this stack into the first non empty slot or where it can fit - sets the addStack count to proper leftover amount
    public ItemStack tryPutStack(ItemStack baseStack, ItemStack addStack){
        if(!(baseStack.getItem() instanceof DynamicMultiItem))
            return baseStack;
        ListTag itemList = getItemList(baseStack);
        
        for(int i = 0; i < numItems; i++){
            ItemStack slotStack = loadStack(itemList.getCompound(i));
            Triple<Boolean, Integer, Integer> canPutResult = canPutStack(baseStack, addStack, slotStack);
            if(!canPutResult.getLeft())
                continue;
            slotStack.setCount(canPutResult.getMiddle());
            itemList.set(i, saveStack(addStack));
            addStack.setCount(canPutResult.getRight());
        }
        StackNBT.put(baseStack, ITEM_STACK_KEY, itemList);
        return baseStack;
    }

    public static ItemStack getCurrentSlotStack(ItemStack baseStack){
        ListTag itemList = getItemList(baseStack);
        int slot = StackNBT.getInt(baseStack, SELECTED_SLOT_KEY);
        return loadStack(itemList.getCompound(slot));
    }

    // only overriding useOnBlock for now, since other uses likely require mixins with more fine tuning
    // see #Item for others that you can add

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getItemInHand().getItem().useOn(new MultiItemUsageContext(context));
    }

    // helper class so we can gaslight minecraft about what itemstack we're holding
    public class MultiItemUsageContext extends UseOnContext{
        public MultiItemUsageContext(UseOnContext ctx){
            super(ctx.getPlayer(), ctx.getHand(), ((MixinItemUsageContext)ctx).invokeGetHitResult());
        }

        @Override
        public ItemStack getItemInHand(){
            if(super.getItemInHand().getItem() instanceof DynamicMultiItem )
                return getCurrentSlotStack(super.getItemInHand());
            return super.getItemInHand();
        }
    }

}
