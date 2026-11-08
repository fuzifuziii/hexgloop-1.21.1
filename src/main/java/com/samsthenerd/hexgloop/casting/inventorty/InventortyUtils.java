package com.samsthenerd.hexgloop.casting.inventorty;

import net.minecraft.core.registries.BuiltInRegistries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.casting.truenameclassaction.MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;

public class InventortyUtils {
    public static KittyContext assertKittyCasting(CastingEnvironment ctx){
        if(!((IContextHelper)(Object)ctx).isKitty()){
            MishapThrowerWrapper.throwMishap(new MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh(List.of("inventorty")));
            return null;
        }
        return ((IContextHelper)(Object)ctx).getKittyContext();
    }

    public static final int CURSOR_REF_SLOT = -2;

    // idx and argc just so we can throw proper mishaps
    @Nullable
    public static GrabbableStack getStackFromGrabbable(Iota grabbable, CastingEnvironment ctx, int idx, int argc){
        KittyContext kCtx = ((IContextHelper)(Object)ctx).getKittyContext();
        if(grabbable instanceof DoubleIota dIota){
            int grabSlot = (int)Math.floor(dIota.getDouble());
            if(grabSlot == CURSOR_REF_SLOT){
                SlotAccess cursorRef = ((IContextHelper)(Object)ctx).getCursorRef();
                if(cursorRef != null)
                    return new GrabbableGeneric(cursorRef::get, cursorRef::set);
            }
            if(grabSlot < 0 && -grabSlot % 1000 == 0){ // want to get an auto fill slot
                int inventoryIndex = (-grabSlot / 1000)-1;
                if(inventoryIndex >= kCtx.getInventoryCount()){
                    // mishap can't find slot here ?
                    return null;
                }
                return new AutoGrabbable(inventoryIndex);
            }
            Slot slot = kCtx.getSlot(grabSlot);
            if(slot == null){
                // mishap bad slot here too
                MishapThrowerWrapper.throwMishap(MishapInvalidIota.ofType(grabbable, argc == 0 ? idx : argc - (idx + 1), "kittygrabbable"));
                return null;
            }
            return new GrabbableSlot(slot);
        }
        if(grabbable instanceof EntityIota eIota){
            if(eIota.getEntity(ctx.getWorld()) instanceof ItemEntity itemEnt && itemEnt.distanceTo(ctx.getCastingEntity()) < 4.0f){
                return new GrabbableEnt(itemEnt);
            }
        }

        MishapThrowerWrapper.throwMishap(MishapInvalidIota.ofType(grabbable, argc == 0 ? idx : argc - (idx + 1), "kittygrabbable"));
        return null;
    }

    // a little class for us to store in the casting context
    public static class KittyContext{
        private List<Container> additionalInventories;
        private Inventory playerInv;

        // should it index by accessible slots or by total inventory slots ? i think accessible would be safer but could mess up the order ?
        private Map<Container, List<Slot>> slots;

        public KittyContext(Inventory playerInv){
            this.playerInv = playerInv;
            this.additionalInventories = new ArrayList<>();
            this.slots = new HashMap<>();
        }

        // always includes inventory
        public int getInventoryCount(){
            return additionalInventories.size() + 1;
        }

        // 0 is *always* player inventory
        public Container getInventory(int index){
            if(index == 0)
                return playerInv;
            return additionalInventories.get(index - 1);
        }

        public void addSlot(Slot slot){
            if(!slots.containsKey(slot.container)){
                slots.put(slot.container, new ArrayList<>(List.of(slot)));
                if(slot.container != playerInv){
                    additionalInventories.add(slot.container);
                    // need to see if the sort order is consistent enough for same sized inventories
                    additionalInventories.sort((a, b) -> b.getContainerSize() - a.getContainerSize());
                }
                // Object inv = slot.inventory instanceof Nameable nameableInv ? nameableInv.getName() : slot.inventory;
                // HexGloop.logPrint("adding: " + slot.inventory + "th inventory: " + inv.toString());
            } else {
                List<Slot> theseSlots = slots.get(slot.container);
                theseSlots.add(slot);
                // keep them in order i guess
                theseSlots.sort((a, b) -> a.getContainerSlot() - b.getContainerSlot());
            }
        }

        @Nullable
        public Slot getSlot(int fullIndex){
            int invIndex = fullIndex / 1000;
            int slotIndex = fullIndex % 1000;
            if(invIndex >= getInventoryCount() || invIndex < 0)
                return null;
            List<Slot> theseSlots = getSlots(invIndex);
            if(slotIndex >= theseSlots.size() || slotIndex < 0)
                return null;
            return theseSlots.get(slotIndex);
        }

        public List<Slot> getSlots(int invIndex){
            return slots.get(getInventory(invIndex));
        }

        public int getInventoryIndex(Container inv){
            if(inv == playerInv)
                return 0;
            return additionalInventories.indexOf(inv) + 1;
        }

        public int getFullSlotIndex(Slot slot){
            int invIndex = getInventoryIndex(slot.container);
            if(invIndex == -1)
                return -1;
            int slotIndex = getSlots(invIndex).indexOf(slot);
            if(slotIndex == -1)
                return -1;
            return invIndex * 1000 + slotIndex;
        }
    }

    public static abstract class GrabbableStack{
        // VV these are mostly from Slot, but implement them for other types as well

        public abstract ItemStack getStack();

        public abstract boolean canTake(Player player);

        public abstract boolean canInsert(ItemStack stack);

        public abstract ItemStack takeStack(int amount, Player player);

        public abstract ItemStack insertStack(ItemStack stack);

        public abstract int getMaxCount();
    }

    public static class GrabbableSlot extends GrabbableStack{
        public Slot slot;

        public GrabbableSlot(Slot slot){
            this.slot = slot;
        }

        public ItemStack getStack(){
            return slot.getItem();
        }

        public boolean canTake(Player player){
            return slot.mayPickup(player);
        }

        public boolean canInsert(ItemStack stack){
            return slot.mayPlace(stack);
        }

        public ItemStack takeStack(int amount, Player player){
            return slot.safeTake(amount, amount, player);
        }

        public ItemStack insertStack(ItemStack stack){
            return slot.safeInsert(stack);
        }

        public int getMaxCount(){
            return slot.getMaxStackSize();
        }
    }

    public static class GrabbableGeneric extends GrabbableStack{
        public Supplier<ItemStack> getter;
        public Consumer<ItemStack> setter;
        
        public GrabbableGeneric(Supplier<ItemStack> getter, Consumer<ItemStack> setter){
            this.getter = getter;
            this.setter = setter;
        }

        public ItemStack getStack(){
            return getter.get();
        }

        public boolean canTake(Player player){
            return true;
        }

        public boolean canInsert(ItemStack stack){
            return ItemStack.isSameItemSameComponents(stack, getter.get());
        }

        public ItemStack takeStack(int amount, Player player){
            return getter.get().split(amount);
        }

        public ItemStack insertStack(ItemStack stack){
            if(getter.get().isEmpty()){
                setter.accept(stack);
                return ItemStack.EMPTY;
            }
            int transferAmt = Math.min(stack.getCount(), getter.get().getMaxStackSize() - getter.get().getCount());
            getter.get().grow(transferAmt);
            stack.shrink(transferAmt);
            return stack;
        }

        public int getMaxCount(){
            return getter.get().getMaxStackSize();
        }
    }

    public static class GrabbableEnt extends GrabbableGeneric{
        public GrabbableEnt(ItemEntity ent){
            super(ent::getItem, ent::setItem);
        }
    }

    public static class AutoGrabbable extends GrabbableSlot{
        int inventoryIndex;
        
        // for the inventory you want to auto fill into, 0 would be player inventory, 1 would be next largest, and so on
        public AutoGrabbable(int inventoryIndex){
            super((Slot)null);
            this.inventoryIndex = inventoryIndex;
        }

        // call this to set the slot to something actually useable
        // return whether or not it actually found a valid slot
        public boolean findSlot(ItemStack stackWith, CastingEnvironment ctx){
            KittyContext kCtx = ((IContextHelper)(Object)ctx).getKittyContext();
            Slot lastSlot = null;
            for(Slot maybeSlot : kCtx.getSlots(inventoryIndex)){
                if(maybeSlot.mayPlace(stackWith) 
                && (maybeSlot.getItem().isEmpty() || (ItemStack.isSameItemSameComponents(maybeSlot.getItem(), stackWith)
                && maybeSlot.getItem().getCount() < Math.min(maybeSlot.getMaxStackSize(), maybeSlot.getItem().getMaxStackSize())))){
                    this.slot = maybeSlot;
                    return true;
                }
                lastSlot = maybeSlot;
            }
            this.slot = lastSlot; // return *something* and it just won't transfer any
            return false;
        }
    }


    // give translation keys for the names of the inventories
    public static final Map<Class<? extends Container>, String> inventoryNames = Map.of(
        PlayerEnderChestContainer.class, "container.enderchest",
        SimpleContainer.class, "hexgloop.container.simple",
        ResultContainer.class, "hexgloop.container.result",
        CraftingContainer.class, "container.crafting",
        MerchantContainer.class, "merchant.trades",
        CompoundContainer.class, "container.chestDouble" // not ideal but i don't feel like writing an accesor into DoubleInventory rn
    );

    // some inventory types are just too generic
    public static final Set<Class<? extends Container>> needsMoreDetail = Set.of(
        SimpleContainer.class,
        ResultContainer.class
    );

    public static String getInventoryName(Container inv, KittyContext kCtx){
        // easy if it has a name
        if(inv instanceof Nameable nameableInv){
            return nameableInv.getName().getString();
        }
        // seems to really only be for like minecarts ?
        if(inv instanceof MenuProvider nameableInv){
            return nameableInv.getDisplayName().getString();
        }

        Class<?> invClass = inv.getClass();
        if(invClass.isAnonymousClass()){
            invClass = invClass.getSuperclass();
        }
        if(inventoryNames.containsKey(invClass)){
            // not sure how well this will work but oh well
            String genericName = Component.translatable(inventoryNames.get(invClass)).getString();
            if(needsMoreDetail.contains(invClass)){
                // try to get more detail
                AbstractContainerMenu handler = kCtx.playerInv.player.containerMenu;
                ResourceLocation handlerID = null;
                try {
                    handlerID = BuiltInRegistries.MENU.getKey(handler.getType());
                } catch(Exception e){} // ignore it, it's fine
                if(handlerID != null){
                    // try to get the name of the screen handler -- who knows if it'll actually translate ! should work fine for vanilla handlers atleast
                    String handlerTransKey = getHandlerTranslationKey(handlerID);
                    if(handlerTransKey == null){
                        return genericName;
                    }
                    return Component.translatable(handlerTransKey).getString() + ": " + genericName;
                }
            }
            return genericName;
        }

        // backup case - use the class name.
        // won't work for obfuscated classes, but we should have those handled already hopefully
        // might not be the best for modded inventories, but decent enough
        return invClass.getSimpleName();
    }

    public static final Map<ResourceLocation, String> hardToMapIDs = Map.of(
        ResourceLocation.parse("enchantment"), "block.minecraft.enchanting_table"
    );

    public static String getHandlerTranslationKey(ResourceLocation handlerID){
        if(hardToMapIDs.containsKey(handlerID)){
            return hardToMapIDs.get(handlerID);
        }
        Block maybeBlock = BuiltInRegistries.BLOCK.get(handlerID);
        if(maybeBlock != null && maybeBlock != Blocks.AIR){
            return maybeBlock.getDescriptionId();
        }
        // for things like crafting_table or smithing_table - really only 2 vanilla cases but who knows, maybe catch some modded ones?
        Block maybeTableBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(handlerID.getNamespace(), handlerID.getPath() + "_table"));
        if(maybeTableBlock != null && maybeTableBlock != Blocks.AIR){
            return maybeTableBlock.getDescriptionId();
        }
        EntityType<?> maybeEnt = EntityType.byString(handlerID.toString()).orElse(null);
        if(maybeEnt != null){
            return maybeEnt.getDescriptionId();
        }
        return null;
    }
}
