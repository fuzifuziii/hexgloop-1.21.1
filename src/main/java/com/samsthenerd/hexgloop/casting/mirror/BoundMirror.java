package com.samsthenerd.hexgloop.casting.mirror;

import java.util.UUID;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoundMirror {
    // ok,, not sure we actually needed world + pos but oh well, neat to have if we want them later i guess
    private ResourceKey<Level> dim;
    private BlockPos pos;
    private UUID uuid;

    public static final String NBT_KEY = "bound_mirror";

    public BoundMirror(ResourceKey<Level> dim, BlockPos pos, UUID uuid) {
        this.dim = dim;
        this.pos = pos;
        this.uuid = uuid;
    }

    public BoundMirror(Level world, BlockPos pos, UUID uuid) {
        this(world.dimension(), pos, uuid);
    }

    public ResourceKey<Level> getDim() {
        return dim;
    }

    public BlockPos getPos() {
        return pos;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public ItemEntity getItemEntity(ServerLevel world){
        return getItemEntity(world.getServer());
    }

    @Nullable
    public ItemEntity getItemEntity(MinecraftServer server){
        ServerLevel targetWorld = server.getLevel(dim);
        Entity maybeItemEnt = targetWorld.getEntity(uuid);
        if(maybeItemEnt instanceof ItemEntity itemEnt){
            return itemEnt;
        }
        return null;
    }

    public ItemStack getItemStack(ServerLevel world){
        return getItemStack(world.getServer());
    }

    public ItemStack getItemStack(MinecraftServer server){
        ItemEntity itemEnt = getItemEntity(server);
        if(itemEnt != null){
            return itemEnt.getItem();
        }
        return ItemStack.EMPTY;
    }

    public CompoundTag toNbt(){
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("uuid", uuid);
        nbt.putLong("pos", pos.asLong());
        nbt.putString("dim", dim.location().toString());
        return nbt;
    }
    

    public static BoundMirror fromNbt(CompoundTag nbt){
        UUID uuid = nbt.hasUUID("uuid") ? nbt.getUUID("uuid") : null;
        BlockPos pos = nbt.contains("pos", Tag.TAG_LONG) ? BlockPos.of(nbt.getLong("pos")) : null;
        ResourceLocation dimID = nbt.contains("dim", Tag.TAG_STRING) ? ResourceLocation.parse(nbt.getString("dim")) : null;
        ResourceKey<Level> dim = dimID != null ? ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimID) : null;
        if(uuid == null || pos == null || dim == null){
            return null;
        }
        return new BoundMirror(dim, pos, uuid);
    }
}
