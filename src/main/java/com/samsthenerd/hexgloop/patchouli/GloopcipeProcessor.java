package com.samsthenerd.hexgloop.patchouli;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import com.samsthenerd.hexgloop.recipes.DataGloopingRecipe;

import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import at.petrak.hexcasting.common.lib.HexItems;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class GloopcipeProcessor implements IComponentProcessor {
    private DataGloopingRecipe recipe;
    private HolderLookup.Provider registries;

    @Override
    public void setup(Level level, IVariableProvider vars) {
        this.registries = level.registryAccess();
        var id = ResourceLocation.parse(vars.get("recipe", registries).asString());

        var recman = level.getRecipeManager();
        var brainsweepings = recman.getAllRecipesFor(DataGloopingRecipe.Type.INSTANCE);
        for (var poisonApples : brainsweepings) {
            if (poisonApples.id().equals(id)) {
                this.recipe = poisonApples.value();
                break;
            }
        }
    }

    @Override
    public IVariable process(Level level, String key) {
        if (this.recipe == null) {
            return null;
        }

        // inputX
        // 012345
        if(key.length() == 6 && key.substring(0, 5).equals("input")){
            var inputStacks = this.recipe.ingredients;
            int index = Integer.parseInt(key.substring(5, 6)) - 1;
            if(index < inputStacks.size()){
                List<ItemStack> stacks = new ArrayList<ItemStack>();
                for(ItemStack stack : inputStacks.get(index).getA().getItems()){
                    ItemStack pageStack = stack.copy();
                    pageStack.setCount(inputStacks.get(index).getB());
                    stacks.add(pageStack);
                }
                return IVariable.from(stacks.toArray(new ItemStack[0]), registries);
            } else {
                return IVariable.from(new ItemStack(Items.AIR), registries);
            }
        }

        switch (key) {
            case "header" -> {
                return IVariable.from(this.recipe.getResultItem(registries).getHoverName(), registries);
            }
            case "result" -> {
                return IVariable.from(this.recipe.getResultItem(registries), registries);
            }
            case "dustIcon" -> {
                return IVariable.from(new ItemStack(HexItems.AMETHYST_DUST.get()), registries);
            }
            case "mediaCost" -> {
                return IVariable.from(Component.translatable("hexgloop.hexdoc.gloopcipe_media_cost", this.recipe.getMediaCost()/10000)
                    .setStyle(Style.EMPTY.withColor(ItemMediaHolder.HEX_COLOR)), registries);
            }
            default -> {
                return null;
            }
        }
    }
}

