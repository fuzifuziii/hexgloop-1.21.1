package com.samsthenerd.hexgloop.casting.tags;

import net.minecraft.core.registries.BuiltInRegistries;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.mojang.datafixers.util.Either;
import com.samsthenerd.hexgloop.compat.hexal.HexalMaybeIotas;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import dev.architectury.platform.Platform;

public class TagUtils {
    
    public static TagChecker getTagChecker(Iota iota, @Nullable CastingEnvironment ctx){
        if(Platform.isModLoaded("hexal")){
            Either<Item, Block> blockOrItem = HexalMaybeIotas.getBlockOrItemFromIota(iota);
            if(blockOrItem != null){
                return new ItemOrBlockTagChecker(blockOrItem);
            }
        }
        if(iota instanceof EntityIota entityIota){
            return new EntityTagChecker(entityIota.getEntity(ctx == null ? null : ctx.getWorld()));
        }
        if(iota instanceof Vec3Iota vecIota && ctx != null){
            return new ItemOrBlockTagChecker(Either.right(ctx.getWorld().getBlockState(BlockPos.containing(vecIota.getVec3())).getBlock()));
        }

        return new EmptyTagChecker();
    }

    public static interface TagChecker {
        public boolean hasTag(String tag);

        public Set<String> getTags();
    }

    public static class EmptyTagChecker implements TagChecker{
        public boolean hasTag(String tag){
            return false;
        }

        public Set<String> getTags(){
            return new HashSet<>();
        }
    }

    public static class ItemOrBlockTagChecker implements TagChecker{

        Either<Item, Block> blockOrItem;

        public ItemOrBlockTagChecker(Either<Item, Block> blockOrItem){
            this.blockOrItem = blockOrItem;
        }

        private Block asBlock(){
            if(blockOrItem.right().isPresent()){
                return blockOrItem.right().get();
            }
            if(blockOrItem.left().isPresent() && blockOrItem.left().get() instanceof BlockItem blockItem){
                return blockItem.getBlock();
            }
            return Blocks.AIR;
        }

        private Item asItem(){
            if(blockOrItem.left().isPresent()){
                return blockOrItem.left().get();
            }
            if(blockOrItem.right().isPresent()){
                return blockOrItem.right().get().asItem();
            }
            return Items.AIR;
        }

        public boolean hasTag(String tag){
            // Optional<RegistryEntry<Block>> blockEntry = BuiltInRegistries.BLOCK.getKey(asBlock()).map(key -> BuiltInRegistries.BLOCK.getEntry(key))
            Block block = asBlock();
            if(block != Blocks.AIR && block.builtInRegistryHolder().is(TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse(tag)))){
                return true;
            }
            Item item = asItem();
            if(item != Items.AIR && item.builtInRegistryHolder().is(TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse(tag)))){
                return true;
            }
            ResourceLocation id = ResourceLocation.parse(tag);
            // want to check if the tag is for what mod it's from
            if(id.getNamespace().equals("modid")){
                Optional<ResourceKey<Block>> blockKey = BuiltInRegistries.BLOCK.getResourceKey(block);
                if(block != Blocks.AIR && blockKey.isPresent() && blockKey.get().location().getNamespace().equals(id.getPath())) return true;
                Optional<ResourceKey<Item>> itemKey = BuiltInRegistries.ITEM.getResourceKey(item);
                if(item != Items.AIR && itemKey.isPresent() && itemKey.get().location().getNamespace().equals(id.getPath())) return true;
            }
            return false;
        }

        public Set<String> getTags(){
            Set<String> tags = new HashSet<>();
            Block block = asBlock();
            if(block != Blocks.AIR){
                tags.add(ResourceLocation.fromNamespaceAndPath("modid", BuiltInRegistries.BLOCK.getResourceKey(block).get().location().getNamespace()).toString());
                block.builtInRegistryHolder().tags().forEach(tag -> {
                    tags.add(tag.location().toLanguageKey());
                });
            }
            Item item = asItem();
            if(item != Items.AIR){
                tags.add(ResourceLocation.fromNamespaceAndPath("modid", BuiltInRegistries.ITEM.getResourceKey(item).get().location().getNamespace()).toString());
                item.builtInRegistryHolder().tags().forEach(tag -> {
                    tags.add(tag.location().toLanguageKey());
                });
            }
            return tags;
        }
    }

    public static class EntityTagChecker implements TagChecker{

        Entity entity;

        public EntityTagChecker(Entity entity){
            this.entity = entity;
        }

        public ItemStack getItemStack(){
            if(entity instanceof ItemEntity itemEntity){
                return itemEntity.getItem();
            }
            if(entity instanceof ItemFrame itemFrameEntity){
                return itemFrameEntity.getItem();
            }
            return ItemStack.EMPTY;
        }

        public boolean hasTag(String tag){
            if(entity.getType().builtInRegistryHolder().is(TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.parse(tag)))){
                return true;
            }
            ItemStack stack = getItemStack();
            if(!stack.isEmpty()){
                return new ItemOrBlockTagChecker(Either.left(stack.getItem())).hasTag(tag);
            }
            return false;
        }

        public Set<String> getTags(){
            Set<String> tags = new HashSet<>();
            entity.getType().builtInRegistryHolder().tags().forEach(tag -> {
                tags.add(tag.location().toLanguageKey());
            });
            ItemStack stack = getItemStack();
            if(!stack.isEmpty()){
                tags.addAll(new ItemOrBlockTagChecker(Either.left(stack.getItem())).getTags());
            }
            return tags;
        }
    }

}
