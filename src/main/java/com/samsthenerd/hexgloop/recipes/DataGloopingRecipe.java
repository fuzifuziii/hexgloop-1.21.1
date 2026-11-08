package com.samsthenerd.hexgloop.recipes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.samsthenerd.hexgloop.recipes.GloopingRecipes.GloopingRecipe;

import at.petrak.hexcasting.api.misc.MediaConstants;

// probably only gonna be used for item only recipes
public class DataGloopingRecipe implements Recipe<RecipeInput>, GloopingRecipe{
    public static class Type implements RecipeType<DataGloopingRecipe> {
		private Type() {}
		public static final Type INSTANCE = new Type();
		public static final String ID = "hexgloop:data_glooping";
	}

    public final List<Tuple<Ingredient, Integer>> ingredients;
    public final ItemStack result;
    public final int mediaCost;
    public final int priority;
 
	public DataGloopingRecipe(List<Tuple<Ingredient, Integer>> ingredients, ItemStack result, int mediaCost, int priority) {
		this.ingredients = ingredients;
		this.result = result;
        this.mediaCost = mediaCost;
        this.priority = priority;
	}

    @Override
    public String toString(){
        String ingredientString = "";
        for(Tuple<Ingredient, Integer> ingredient : ingredients){
            ingredientString += "\n\t\t -x" + ingredient.getB();
            for(ItemStack stack : ingredient.getA().getItems())
                ingredientString += "\n\t\t\t" + stack;
        }
        return "DataGloopingRecipe(" +
        "\n\tresult: " + result +
        "\n\tmediaCost: " + mediaCost +
        "\n\tpriority: " + priority +
        "\n\tingredients: " + ingredientString + 
        ")";
    }

    @Override
    public boolean matches(List<Entity> inputs) {
        List<Tuple<Ingredient, Integer>> remainingIngredients = new ArrayList<>(ingredients);
        for(Entity ent : inputs){
            if(ent instanceof ItemEntity itemEnt){
                ItemStack stack = itemEnt.getItem();
                if(stack.isEmpty())
                    continue;
                for(int i = 0; i < remainingIngredients.size(); i++){
                    Tuple<Ingredient, Integer> ingredient = remainingIngredients.get(i);
                    if(ingredient.getA().test(stack)){
                        int maxToRemove = Math.min(ingredient.getB(), stack.getCount());
                        if(ingredient.getB() == maxToRemove){
                            remainingIngredients.remove(i);
                            if(remainingIngredients.isEmpty())
                                return true;
                        }else{
                            remainingIngredients.set(i, new Tuple<Ingredient, Integer>(ingredient.getA(), ingredient.getB() - maxToRemove));
                        }
                    }
                }
            }
        }

        return false;
    }

    // consumes the earlier items in the input list first
    @Override
    public ItemStack craft(List<Entity> inputs, boolean forRealzies){
        List<Tuple<Ingredient, Integer>> remainingIngredients = new ArrayList<>(ingredients);
        for(Entity ent : inputs){
            if(ent instanceof ItemEntity itemEnt){
                ItemStack stack = itemEnt.getItem();
                if(stack.isEmpty())
                    continue;
                for(int i = 0; i < remainingIngredients.size(); i++){
                    Tuple<Ingredient, Integer> ingredient = remainingIngredients.get(i);
                    if(ingredient.getA().test(stack)){
                        int maxToRemove = Math.min(ingredient.getB(), stack.getCount());
                        if(ingredient.getB() == maxToRemove){
                            remainingIngredients.remove(i);
                        }else{
                            remainingIngredients.set(i, new Tuple<Ingredient, Integer>(ingredient.getA(), ingredient.getB() - maxToRemove));
                        }
                        if(forRealzies){
                            stack.shrink(maxToRemove);
                            if(stack.isEmpty())
                                itemEnt.discard();
                        }
                        if(remainingIngredients.isEmpty())
                            return result.copy();
                    }
                }
            }
        }

        return result.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries){
        return result.copy();
    }

    @Override
    public ItemStack getOutput(){
        return result.copy();
    }

    @Override
    public int getPriority(){
        return priority;
    }

    @Override
    public int getMediaCost(){
        return mediaCost;
    }

	@Override
	public RecipeType<?> getType() {
		return Type.INSTANCE;
	}

    // ignore crafting recipe stuff
    @Override
    public boolean matches(RecipeInput pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput pContainer, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    public RecipeSerializer<DataGloopingRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<DataGloopingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        // an ingredient is either {"ingredient": <ingredient>, "count": n} or just a bare <ingredient>
        private static final Codec<Tuple<Ingredient, Integer>> ENTRY_CODEC = Codec.withAlternative(
            RecordCodecBuilder.<Tuple<Ingredient, Integer>>create(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(Tuple::getA),
                Codec.INT.optionalFieldOf("count", 1).forGetter(Tuple::getB)
            ).apply(inst, (ing, c) -> new Tuple<>(ing, c))),
            Ingredient.CODEC.xmap(ing -> new Tuple<>(ing, 1), Tuple::getA)
        );

        private static final Codec<ItemStack> RESULT_CODEC = BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem);

        private static final MapCodec<DataGloopingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ExtraCodecs.nonEmptyList(ENTRY_CODEC.listOf()).fieldOf("ingredients").forGetter(r -> r.ingredients),
            RESULT_CODEC.fieldOf("result").forGetter(r -> r.result),
            Codec.INT.optionalFieldOf("mediaCost", (int) MediaConstants.CRYSTAL_UNIT).forGetter(r -> r.mediaCost),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(r -> r.priority)
        ).apply(inst, DataGloopingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DataGloopingRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                buf.writeVarInt(recipe.ingredients.size());
                for(Tuple<Ingredient, Integer> ingredient : recipe.ingredients){
                    buf.writeVarInt(ingredient.getB());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient.getA());
                }
                ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                buf.writeVarInt(recipe.mediaCost);
                buf.writeVarInt(recipe.priority);
            },
            buf -> {
                int size = buf.readVarInt();
                List<Tuple<Ingredient, Integer>> ingredients = new ArrayList<>();
                for(int i = 0; i < size; i++){
                    int count = buf.readVarInt();
                    Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                    ingredients.add(new Tuple<>(ingredient, count));
                }
                ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                int mediaCost = buf.readVarInt();
                int priority = buf.readVarInt();
                return new DataGloopingRecipe(ingredients, result, mediaCost, priority);
            });

        @Override
        public MapCodec<DataGloopingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DataGloopingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
