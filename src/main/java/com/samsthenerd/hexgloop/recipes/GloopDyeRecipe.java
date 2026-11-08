package com.samsthenerd.hexgloop.recipes;

import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.items.ItemGloopDye;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import at.petrak.hexcasting.api.misc.MediaConstants;

// maybe not the neatest for mod compat but whatever, it's probably good enough for most things
public class GloopDyeRecipe extends CustomRecipe{
    public static final SimpleCraftingRecipeSerializer<GloopDyeRecipe> SERIALIZER =
        new SimpleCraftingRecipeSerializer<GloopDyeRecipe>(GloopDyeRecipe::new);

    public GloopDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingInventory, Level world) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack gloopDyeStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInventory.size(); ++i) {
            ItemStack itemStack2 = craftingInventory.getItem(i);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.is(ItemTags.DYEABLE)) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.getItem() instanceof ItemGloopDye) {
                if (!gloopDyeStack.isEmpty()) {
                    return false;
                }
                gloopDyeStack = itemStack2;
                if(HexGloopItems.GLOOP_DYE_ITEM.get().getMedia(gloopDyeStack) < MediaConstants.DUST_UNIT){
                    // HexGloop.logPrint("cringe no media");
                    return false;
                }
                continue;
            }
            return false;
        }
        return !itemStack.isEmpty() && !gloopDyeStack.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInventory, HolderLookup.Provider registries) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack gloopDyeStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInventory.size(); ++i) {
            ItemStack itemStack2 = craftingInventory.getItem(i);
            if (itemStack2.isEmpty()) continue;
            Item item = itemStack2.getItem();
            if (itemStack2.is(ItemTags.DYEABLE)) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2.copy();
                continue;
            }
            if (item instanceof ItemGloopDye) {
                if (!gloopDyeStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                gloopDyeStack = itemStack2.copy();
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (itemStack.isEmpty() || gloopDyeStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(ItemGloopDye.getDyeColor(gloopDyeStack) & 0xFFFFFF, true));
        itemStack.setCount(1);
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        NonNullList<ItemStack> defaultedList = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack remStack = inventory.getItem(i);
            Item item = remStack.getItem();
            if(item instanceof ItemGloopDye){
                ItemStack smallerGloop = inventory.getItem(i).copy();
                HexGloopItems.GLOOP_DYE_ITEM.get().decrementMedia(smallerGloop, (int) MediaConstants.DUST_UNIT);
                defaultedList.set(i, smallerGloop);
                continue;
            }
            if (!remStack.hasCraftingRemainingItem()) continue;
            defaultedList.set(i, remStack.getCraftingRemainingItem());
        }
        return defaultedList;
    }
}
