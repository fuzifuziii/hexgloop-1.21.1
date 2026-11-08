package com.samsthenerd.hexgloop.compat.emi;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.blocks.HexGloopBlocks;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.recipes.DataGloopingRecipe;
import com.samsthenerd.hexgloop.recipes.ItemFlayingRecipe;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

// EMI categories for the two custom recipe types (glooping and item flaying).
// Without them EMI only shows vanilla crafting/smelting, because both types are
// Recipe<RecipeInput> implementations that never appear in a crafting grid.
@EmiEntrypoint
public class HexGloopEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory GLOOPING = new EmiRecipeCategory(
        ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "glooping"),
        EmiStack.of(HexGloopItems.GLOOP_ITEM.get()));

    public static final EmiRecipeCategory FLAYING = new EmiRecipeCategory(
        ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "flaying"),
        EmiStack.of(HexGloopItems.MIND_JAR_ITEM.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(GLOOPING);
        registry.addCategory(FLAYING);

        // the gloop energizer boils the items dropped into it
        registry.addWorkstation(GLOOPING, EmiStack.of(HexGloopBlocks.GLOOP_ENERGIZER_BLOCK.get()));
        // item flaying happens at the mind pedestal
        registry.addWorkstation(FLAYING, EmiStack.of(HexGloopBlocks.MIND_PEDESTAL_BLOCK.get()));

        for (RecipeHolder<DataGloopingRecipe> holder : registry.getRecipeManager()
            .getAllRecipesFor(DataGloopingRecipe.Type.INSTANCE)) {
            registry.addRecipe(new GloopingEmiRecipe(holder.id(), holder.value()));
        }

        for (RecipeHolder<ItemFlayingRecipe> holder : registry.getRecipeManager()
            .getAllRecipesFor(ItemFlayingRecipe.Type.INSTANCE)) {
            registry.addRecipe(new FlayingEmiRecipe(holder.id(), holder.value()));
        }
    }
}
