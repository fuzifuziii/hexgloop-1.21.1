package com.samsthenerd.hexgloop.recipes;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.items.ItemMultiFocus;
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
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexItems;

public class SealMultiFocusRecipe extends CustomRecipe{
    public static final SimpleCraftingRecipeSerializer<SealMultiFocusRecipe> SERIALIZER =
        new SimpleCraftingRecipeSerializer<SealMultiFocusRecipe>(SealMultiFocusRecipe::new);

    public SealMultiFocusRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingInventory, Level world) {
        if(countItem(craftingInventory, Items.HONEYCOMB) == 0){
            return false;
        }

        boolean foundOne = false;
        Item foundItem = null;
        // check for dyeable spellbook first, it's not great but we're tacking it on here !
        if(countItem(craftingInventory, HexGloopItems.DYEABLE_SPELLBOOK_ITEM.get()) == 1){
            if(foundOne) return false;
            foundOne = true;
            foundItem = HexGloopItems.DYEABLE_SPELLBOOK_ITEM.get();
        }
        for(int i = 0; i < HexGloop.FOCUS_ITEMS.size(); i++){
            if(countItem(craftingInventory, HexGloop.FOCUS_ITEMS.get(i).get()) == 1){
                if(foundOne) return false;
                foundOne = true;
                foundItem = HexGloop.FOCUS_ITEMS.get(i).get();
            }
        }
        if(!foundOne) return false;

        for(int s = 0; s < craftingInventory.size(); s++){
            ItemStack stack = craftingInventory.getItem(s);
            if(stack.getItem() == foundItem){
                if(foundItem instanceof ItemMultiFocus){
                    if(!ItemSpellbook.isSealed(stack) && HexGloopItems.MULTI_FOCUS_ITEM.get().readIota(stack) != null){
                        return true;
                    }
                } else if(stack.getItem() == HexGloopItems.DYEABLE_SPELLBOOK_ITEM.get()){
                    if(!ItemSpellbook.isSealed(stack) && HexGloopItems.DYEABLE_SPELLBOOK_ITEM.get().readIota(stack) != null){
                        return true;
                    }
                } else {
                    if(!ItemFocus.isSealed(stack) && ((ItemFocus) HexItems.FOCUS).readIota(stack) != null){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public ItemStack assemble(CraftingInput craftingInventory, HolderLookup.Provider registries) {
        for(int s = 0; s < craftingInventory.size(); s++){
            ItemStack stack = craftingInventory.getItem(s);
            if(stack.getItem() instanceof ItemMultiFocus || stack.getItem() instanceof ItemSpellbook){
                ItemStack returnStack = stack.copy();
                ItemSpellbook.setSealed(returnStack, true);
                return returnStack;
            } else if(stack.getItem() instanceof ItemFocus){
                ItemStack returnStack = stack.copy();
                ItemFocus.seal(returnStack);
                return returnStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        NonNullList<ItemStack> defaultedList = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack remStack = inventory.getItem(i);
            Item item = remStack.getItem();
            if (!remStack.hasCraftingRemainingItem()) continue;
            defaultedList.set(i, remStack.getCraftingRemainingItem());
        }
        return defaultedList;
    }

    // @Override
    // public ItemStack getOutput() {
    //     return new ItemStack(DuckyPeriph.KEYBOARD_ITEM);
    // }

    // can't reach much from 2x2 but eh
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }


    private static int countItem(CraftingInput input, Item item){
        int c = 0;
        for(int i = 0; i < input.size(); i++){
            ItemStack st = input.getItem(i);
            if(st.is(item)) c += st.getCount();
        }
        return c;
    }
}
