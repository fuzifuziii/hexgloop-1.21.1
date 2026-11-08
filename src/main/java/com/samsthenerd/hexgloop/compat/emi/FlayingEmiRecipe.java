package com.samsthenerd.hexgloop.compat.emi;

import java.util.List;

import com.samsthenerd.hexgloop.recipes.ItemFlayingRecipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FlayingEmiRecipe implements EmiRecipe {
    // Book itemflay page background (128x128)
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("hexgloop", "textures/gui/itemflaygui.png");

    private final ResourceLocation id;
    private final EmiIngredient input;
    private final EmiStack output;
    private final ItemFlayingRecipe recipe;
    private final Component villagerLine;

    public FlayingEmiRecipe(ResourceLocation id, ItemFlayingRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
        this.input = EmiIngredient.of(recipe.ingredient.getA(), recipe.ingredient.getB());
        this.output = EmiStack.of(new ItemStack(recipe.result, recipe.resultCount));
        this.villagerLine = buildVillagerLine(recipe);
    }

    private static Component buildVillagerLine(ItemFlayingRecipe recipe) {
        var profession = recipe.villagerIn.getProfession();
        Component name = profession == null
            ? Component.translatable("hexgloop.emi.flaying.any_villager")
            : Component.translatable("entity.minecraft.villager."
                + net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).getPath());
        return Component.translatable("hexgloop.emi.flaying.villager", name, recipe.villagerIn.getMinLevel());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return HexGloopEmiPlugin.FLAYING;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 128;
    }

    @Override
    public int getDisplayHeight() {
        return 92;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Same layout as the Patchouli itemflay template: input item at
        // x 12, y 35; entity centred at x 50, y 44; result at x 87, y 35;
        // text at y 77. EMI cannot render the entity itself, so the
        // villager spawn egg stands in for it in the middle. No filling
        // arrow and no default slot backgrounds: the texture is the frame.
        widgets.addTexture(BG, 0, 0, 128, 88, 0, 0, 128, 88, 128, 128);
        widgets.addSlot(input, 11, 34).drawBack(false);
        widgets.addSlot(EmiStack.of(new ItemStack(Items.VILLAGER_SPAWN_EGG)), 41, 34).drawBack(false);
        widgets.addSlot(output, 86, 34).drawBack(false).recipeContext(this);
        widgets.addText(villagerLine, 6, 79, 0x808080, false);
    }
}
