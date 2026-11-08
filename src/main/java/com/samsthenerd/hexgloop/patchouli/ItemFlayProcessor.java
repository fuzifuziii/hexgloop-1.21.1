package com.samsthenerd.hexgloop.patchouli;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import com.samsthenerd.hexgloop.recipes.ItemFlayingRecipe;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

// yoinked almost entirely from BrainsweepProcessor
public class ItemFlayProcessor implements IComponentProcessor {
    private ItemFlayingRecipe recipe;
    private HolderLookup.Provider registries;

    @Override
    public void setup(Level level, IVariableProvider vars) {
        this.registries = level.registryAccess();
        var id = ResourceLocation.parse(vars.get("recipe", registries).asString());

        var recman = level.getRecipeManager();
        var brainsweepings = recman.getAllRecipesFor(ItemFlayingRecipe.Type.INSTANCE);
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

        switch (key) {
            case "header" -> {
                return IVariable.from(this.recipe.result.getDescription(), registries);
            }
            case "input" -> {
                var inputStacks = this.recipe.ingredient.getA().getItems();
                return IVariable.from(inputStacks, registries);
            }
            case "result" -> {
                return IVariable.from(new ItemStack(this.recipe.result), registries);
            }

            case "entity" -> {
                var professionObj = this.recipe.villagerIn.getProfession();
                var biomeObj = this.recipe.villagerIn.getBiome();
                var profession = professionObj == null ? ResourceLocation.parse("toolsmith")
                    : net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION.getKey(professionObj);
                var biome = biomeObj == null ? ResourceLocation.parse("plains")
                    : net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(biomeObj);
                var villagerLevel = this.recipe.villagerIn.getMinLevel();
                var iHatePatchouli = String.format(
                    "minecraft:villager{VillagerData:{profession:'%s',type:'%s',level:%d}}",
                    profession, biome, villagerLevel);
                return IVariable.wrap(iHatePatchouli, registries);
            }
            case "entityTooltip" -> {
                Minecraft mc = Minecraft.getInstance();
                return IVariable.wrapList(this.recipe.villagerIn
                    .getTooltip(mc.options.advancedItemTooltips)
                    .stream()
                    .map(c -> IVariable.from(c, registries))
                    .toList(), registries);
            }
            default -> {
                return null;
            }
        }
    }
}

