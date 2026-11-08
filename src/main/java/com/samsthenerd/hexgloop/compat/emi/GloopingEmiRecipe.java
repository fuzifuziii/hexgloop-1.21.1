package com.samsthenerd.hexgloop.compat.emi;

import java.util.ArrayList;
import java.util.List;

import com.samsthenerd.hexgloop.recipes.DataGloopingRecipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.Ingredient;

public class GloopingEmiRecipe implements EmiRecipe {
    private static final int SLOT = 18;
    private static final int COLS = 3;
    // Book gloopcipe page background: water, lightning and the result frame (128x128)
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("hexgloop", "textures/gui/gloopcipegui.png");

    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final EmiStack output;
    private final int mediaCost;

    public GloopingEmiRecipe(ResourceLocation id, DataGloopingRecipe recipe) {
        this.id = id;
        List<EmiIngredient> ins = new ArrayList<>();
        for (Tuple<Ingredient, Integer> entry : recipe.ingredients) {
            ins.add(EmiIngredient.of(entry.getA(), entry.getB()));
        }
        this.inputs = ins;
        this.output = EmiStack.of(recipe.result.copy());
        this.mediaCost = recipe.mediaCost;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return HexGloopEmiPlugin.GLOOPING;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
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
        // Same layout as the Patchouli gloopcipe template: the texture is
        // drawn at y=4 on the book page, so slot positions are the template
        // item positions shifted by -4 vertically and -1 for the slot frame.
        // Ingredients 3x3 in the water at x 9/28/47, y 17/36/54; result at
        // x 96, y 36; media cost text at x 20, y 83. The texture already
        // contains the water, the lightning and the result frame, so there
        // is no filling arrow and the slots get no default background.
        widgets.addTexture(BG, 0, 0, 128, 88, 0, 0, 128, 88, 128, 128);
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), 8 + (i % COLS) * 19, 12 + (i / COLS) * 19).drawBack(false);
        }
        widgets.addSlot(output, 95, 31).drawBack(false).recipeContext(this);
        widgets.addText(Component.translatable("hexgloop.emi.media_cost", mediaCost / 10000),
            20, 79, 0xa06ee0, false);
    }
}
