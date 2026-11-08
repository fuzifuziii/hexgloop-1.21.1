package com.samsthenerd.hexgloop.recipes;

import com.samsthenerd.hexgloop.HexGloop;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class HexGloopRecipes {
    public static Registrar<RecipeSerializer<?> > recipeSerializers = HexGloop.REGISTRIES.get().get(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER);
    public static Registrar<RecipeType<?> > recipeTypes = HexGloop.REGISTRIES.get().get(net.minecraft.core.registries.Registries.RECIPE_TYPE);
    
    public static RegistrySupplier<SimpleCraftingRecipeSerializer<SealMultiFocusRecipe>> SEAL_MULTI_FOCUS_RECIPE = 
        register("crafting_seal_multi_focus", SealMultiFocusRecipe.SERIALIZER);
    public static RegistrySupplier <SimpleCraftingRecipeSerializer<GloopDyeRecipe>> GLOOP_DYE_RECIPE = 
        register("crafting_gloop_dye", GloopDyeRecipe.SERIALIZER);
    public static RegistrySupplier <SimpleCraftingRecipeSerializer<CoveredSpellbookRecipe>> MAKE_DYEBOOK_RECIPE = 
        register("crafting_make_dyebook", CoveredSpellbookRecipe.SERIALIZER);
    public static RegistrySupplier<DataGloopingRecipe.Serializer> DATA_GLOOPING_RECIPE = 
        register("data_glooping", DataGloopingRecipe.Serializer.INSTANCE);
    public static RegistrySupplier<DataGloopingRecipe.Type> DATA_GLOOPING_TYPE = 
        register("data_glooping", DataGloopingRecipe.Type.INSTANCE);

    public static RegistrySupplier<ItemFlayingRecipe.Serializer> ITEM_FLAYING_RECIPE = 
        register("item_flaying", ItemFlayingRecipe.Serializer.INSTANCE);
    public static RegistrySupplier<ItemFlayingRecipe.Type> ITEM_FLAYING_TYPE = 
        register("item_flaying", ItemFlayingRecipe.Type.INSTANCE);
    

    public static void register(){
        GloopingRecipes.init();
    }

    public static <S extends RecipeSerializer<T>, T extends Recipe<?>> RegistrySupplier<S> register(String id, S serializer) {
        return recipeSerializers.register(ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, id), () -> serializer);
    }

    public static <S extends RecipeType<T>, T extends Recipe<?>> RegistrySupplier<S> register(String id, S type) {
        return recipeTypes.register(ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, id), () -> type);
    }
}
