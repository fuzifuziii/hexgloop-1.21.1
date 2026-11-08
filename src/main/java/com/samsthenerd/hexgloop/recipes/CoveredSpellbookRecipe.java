package com.samsthenerd.hexgloop.recipes;

import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.misc.HexGloopTags;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexItems;

public class CoveredSpellbookRecipe extends CustomRecipe{
    public static final SimpleCraftingRecipeSerializer<CoveredSpellbookRecipe> SERIALIZER =
        new SimpleCraftingRecipeSerializer<CoveredSpellbookRecipe>(CoveredSpellbookRecipe::new);

    public CoveredSpellbookRecipe(CraftingBookCategory category) {
        super(category);
    }

    protected static boolean isLeatherIsh(ItemStack stack){
        return stack.is(HexGloopTags.FABRIC_LEATHERY) || stack.is(HexGloopTags.FORGE_LEATHERY) || stack.getItem() == Items.LEATHER;
    }

    @Override
    public boolean matches(CraftingInput craftingInventory, Level world) {
        int bookslot = -1;
        for(int i = 0; i < craftingInventory.size(); i++){
            if(craftingInventory.getItem(i).getItem() == HexItems.SPELLBOOK){
                if(bookslot != -1){
                    return false;
                }
                bookslot = i;
            }
        }
        if(bookslot == -1){
            return false;
        }
        int bookx = bookslot % craftingInventory.width();
        int booky = bookslot / craftingInventory.width();
        // get the leather-y pieces
        boolean gotNetherite = false;
        int leatherCount = 0;
        for(int i = 0; i < craftingInventory.size(); i++){
            if(i == bookslot) continue; // don't worry about that
            int slotx = i % craftingInventory.width();
            int sloty = i / craftingInventory.width();
            boolean closeToBook = Math.abs(slotx - bookx) <= 1 && Math.abs(sloty - booky) <= 1; // if it's adjacent/diagonal
            if(slotx == bookx && sloty == booky+1){
                if(craftingInventory.getItem(i).getItem() != Items.NETHERITE_INGOT){
                    return false;
                }
                gotNetherite = true;
                continue;
            }
            if(closeToBook){
                if(!isLeatherIsh(craftingInventory.getItem(i)))
                    return false;
                leatherCount++;
                continue;
            }
            if(!closeToBook && !craftingInventory.getItem(i).isEmpty()){
                return false;
            }
        }
        return gotNetherite && leatherCount == 7;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInventory, HolderLookup.Provider registries) {
        ItemStack oldSpellbook = ItemStack.EMPTY;
        for(int i = 0; i < craftingInventory.size(); i++){
            if(craftingInventory.getItem(i).getItem() == HexItems.SPELLBOOK){
                oldSpellbook = craftingInventory.getItem(i);
            }
        }
        // it shouldn't be but just in case
        if(oldSpellbook.isEmpty()){
            return ItemStack.EMPTY;
        }
        ItemStack newSpellbook = new ItemStack(HexGloopItems.DYEABLE_SPELLBOOK_ITEM.get());
        newSpellbook.applyComponents(oldSpellbook.getComponents());
        return newSpellbook;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
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
            if(item instanceof ItemSpellbook) continue;
            if (!remStack.hasCraftingRemainingItem()) continue;
            defaultedList.set(i, remStack.getCraftingRemainingItem());
        }
        return defaultedList;
    }
}

