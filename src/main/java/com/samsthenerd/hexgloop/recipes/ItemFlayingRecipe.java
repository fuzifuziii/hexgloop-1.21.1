package com.samsthenerd.hexgloop.recipes;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.samsthenerd.hexgloop.items.IMindTargetItem;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.VillagerIngredient;

public class ItemFlayingRecipe implements Recipe<RecipeInput> {

    public static class Type implements RecipeType<ItemFlayingRecipe> {
		private Type() {}
		public static final Type INSTANCE = new Type();
		public static final String ID = "hexgloop:item_flaying";
	}

    public final Tuple<Ingredient, Integer> ingredient; // ingredient and count required
    public final VillagerIngredient villagerIn;
    public final Item result;
    public final boolean preserveNbt;
    public final boolean addVillagerNbt;
    public final CompoundTag addedNbt;
    public final int resultCount;

    public ItemFlayingRecipe(Tuple<Ingredient, Integer> ingredient, VillagerIngredient villagerIn, Item result, boolean preserveNbt, boolean addVillagerNbt, CompoundTag addedNbt, int resultCount) {
        this.ingredient = ingredient;
        this.villagerIn = villagerIn;
        this.result = result;
        this.preserveNbt = preserveNbt;
        this.addVillagerNbt = addVillagerNbt;
        this.addedNbt = addedNbt;
        this.resultCount = resultCount;
    }
    

    public boolean matches(ItemStack inStack, Villager villagerIn) {
        return ingredient.getA().test(inStack) && inStack.getCount() >= ingredient.getB() && villagerIn.level() instanceof ServerLevel sl && this.villagerIn.test(villagerIn, sl);
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    // in order to get this to be a "Recipe" we need to do a lot of bending-over-backwards
    // to get the implementation to be satisfied even though we never use it
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

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY.copy();
    }

    private static final Map<Item, IMindTargetItem> ITEM_FLAYING_HANDLERS = new HashMap<>();
    private static final Map<Item, IMindTargetItem> itemFlayingCache = new HashMap<>();

    // TODO: modify this to get base hex block recipes too
    @Nullable
    public static IMindTargetItem getItemTarget(ItemStack stack, Villager villager, RecipeManager recman){
        // get it from an interface
        if(stack.getItem() instanceof IMindTargetItem target){
            return target;
        }
        // get it from an external handler
        if(ITEM_FLAYING_HANDLERS.containsKey(stack.getItem())){
            return ITEM_FLAYING_HANDLERS.get(stack.getItem());
        }
        // check the cache before rechecking the recipes
        if(itemFlayingCache.containsKey(stack.getItem()))
            return itemFlayingCache.get(stack.getItem());
        // get it from a recipe
        for(RecipeHolder<ItemFlayingRecipe> holder : recman.getAllRecipesFor(Type.INSTANCE)){
            ItemFlayingRecipe recipe = holder.value();
            if(recipe.matches(stack, villager)){
                IMindTargetItem target =  new MindRecipeItem(recipe);
                itemFlayingCache.put(stack.getItem(), target);
                return target;
            }
        }
        // try to find block
        if(stack.getItem() instanceof BlockItem blockItem){
            BlockState inState = blockItem.getBlock().defaultBlockState();
            for(RecipeHolder<BrainsweepRecipe> holder : recman.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE.get())){
                BrainsweepRecipe recipe = holder.value();
                if(villager.level() instanceof ServerLevel sl && recipe.matches(inState, villager, sl) && recipe.result().getBlock() != blockItem.getBlock()){
                    IMindTargetItem target =  new MindRecipeBlock(recipe);
                    itemFlayingCache.put(stack.getItem(), target);
                    return target;
                }
            }
        }
        itemFlayingCache.put(stack.getItem(), null);
        return null;
    }

    public static boolean addItemFlayingHandler(Item item, IMindTargetItem handler){
        if(ITEM_FLAYING_HANDLERS.containsKey(item))
            return false;
        ITEM_FLAYING_HANDLERS.put(item, handler);
        return true;
    }

    // so that we can reload it with data reload
    public static void reloadRecipes(){
        itemFlayingCache.clear();
    }

    public static class Serializer implements RecipeSerializer<ItemFlayingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        // addedNbt is given as an snbt string in the json
        private static final Codec<CompoundTag> SNBT_CODEC = Codec.STRING.comapFlatMap(str -> {
            try {
                return DataResult.success(TagParser.parseTag(str));
            } catch(Exception e){
                return DataResult.error(() -> "Error parsing addedNbt: " + e.getMessage());
            }
        }, CompoundTag::toString);

        private static final MapCodec<ItemFlayingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            VillagerIngredient.Type.CODEC.codec().fieldOf("villagerIn").forGetter(r -> r.villagerIn),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient.getA()),
            Codec.INT.optionalFieldOf("inCount", 1).forGetter(r -> r.ingredient.getB()),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(r -> r.result),
            Codec.INT.optionalFieldOf("resultCount", 1).forGetter(r -> r.resultCount),
            Codec.BOOL.optionalFieldOf("preserveNbt", true).forGetter(r -> r.preserveNbt),
            Codec.BOOL.optionalFieldOf("addVillagerNbt", false).forGetter(r -> r.addVillagerNbt),
            SNBT_CODEC.optionalFieldOf("addedNbt", new CompoundTag()).forGetter(r -> r.addedNbt)
        ).apply(inst, (villagerIn, ingredient, inCount, result, resultCount, preserveNbt, addVillagerNbt, addedNbt) ->
            new ItemFlayingRecipe(new Tuple<Ingredient, Integer>(ingredient, inCount), villagerIn, result, preserveNbt, addVillagerNbt, addedNbt, resultCount)));

        private static final StreamCodec<RegistryFriendlyByteBuf, ItemFlayingRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                VillagerIngredient.Type.STREAM_CODEC.encode(buf, recipe.villagerIn);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient.getA());
                buf.writeVarInt(recipe.ingredient.getB());
                ByteBufCodecs.registry(Registries.ITEM).encode(buf, recipe.result);
                buf.writeVarInt(recipe.resultCount);
                buf.writeBoolean(recipe.preserveNbt);
                buf.writeBoolean(recipe.addVillagerNbt);
                buf.writeNbt(recipe.addedNbt);
            },
            buf -> {
                VillagerIngredient villagerIn = VillagerIngredient.Type.STREAM_CODEC.decode(buf);
                Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                int inCount = buf.readVarInt();
                Item result = ByteBufCodecs.registry(Registries.ITEM).decode(buf);
                int resultCount = buf.readVarInt();
                boolean preserveNbt = buf.readBoolean();
                boolean addVillagerNbt = buf.readBoolean();
                CompoundTag addedNbt = buf.readNbt();
                return new ItemFlayingRecipe(new Tuple<Ingredient, Integer>(ingredient, inCount), villagerIn, result, preserveNbt, addVillagerNbt, addedNbt, resultCount);
            });

        @Override
        public MapCodec<ItemFlayingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ItemFlayingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    // wrap all the recipe logic in this class so we can just have a single interface for dealing with flaying into items
    public static class MindRecipeItem implements IMindTargetItem{
        private final ItemFlayingRecipe recipe;

        public MindRecipeItem(ItemFlayingRecipe recipe){
            this.recipe = recipe;
        }

        public ItemStack absorbVillagerMind(Villager sacrifice, ItemStack stack, CastingEnvironment ctx){
            ItemStack original = stack.copy();
            stack.shrink(recipe.ingredient.getB());

            ItemStack result = new ItemStack(recipe.result, recipe.resultCount);
            CompoundTag nbt = new CompoundTag();
            CompoundTag originalNbt = StackNBT.get(original);
            if(recipe.preserveNbt && originalNbt != null){
                nbt = originalNbt.copy();
            }
            // add in any new nbt needed
            nbt.merge(recipe.addedNbt);
            if(recipe.addVillagerNbt){
                CompoundTag villagerNbt = new CompoundTag();
                sacrifice.saveAsPassenger(villagerNbt);
                nbt.put(IMindTargetItem.STORED_MIND_TAG, villagerNbt);
            }
            if(!nbt.isEmpty()) StackNBT.set(result, nbt);
            return result;
        }
    
        public boolean canAcceptMind(Villager sacrifice, ItemStack stack, CastingEnvironment ctx){
            return recipe.matches(stack, sacrifice);
        }

    }

    public static class MindRecipeBlock implements IMindTargetItem{
        private final BrainsweepRecipe recipe;

        public MindRecipeBlock(BrainsweepRecipe recipe){
            this.recipe = recipe;
        }

        public ItemStack absorbVillagerMind(Villager sacrifice, ItemStack stack, CastingEnvironment ctx){
            ItemStack original = stack.copy();
            stack.shrink(1);

            ItemStack result = new ItemStack(recipe.result().getBlock().asItem(), 1);
            return result;
        }
    
        public boolean canAcceptMind(Villager sacrifice, ItemStack stack, CastingEnvironment ctx){
            if(!(stack.getItem() instanceof BlockItem blockItem)) return false;
            BlockState inState = blockItem.getBlock().defaultBlockState();
            return sacrifice.level() instanceof ServerLevel sl && recipe.matches(inState, sacrifice, sl) && recipe.result().getBlock() != blockItem.getBlock();
        }

    }
}