package com.samsthenerd.hexgloop.utils;

import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 1.20.5+ removed free-form item NBT. HexGloop kept a lot of state in item tags, so this helper stores it in the
 * vanilla {@code minecraft:custom_data} component under the same keys the 1.19.2 version used.
 * <p>
 * Reads return a COPY: mutating it does nothing. Use {@link #edit} (or the put/remove helpers) to write.
 */
public final class StackNBT {
    private StackNBT(){}

    public static boolean has(ItemStack stack){
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /** copy of the custom data, or null if there is none (mirrors the old {@code stack.getTag()}). */
    @Nullable
    public static CompoundTag get(ItemStack stack){
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    /** copy of the custom data, never null (read-only stand-in for {@code getOrCreateTag()}). */
    public static CompoundTag getOrEmpty(ItemStack stack){
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static void set(ItemStack stack, @Nullable CompoundTag tag){
        if(tag == null || tag.isEmpty()){
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static void edit(ItemStack stack, Consumer<CompoundTag> editor){
        CustomData.update(DataComponents.CUSTOM_DATA, stack, editor);
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if(data != null && data.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
    }

    public static boolean contains(ItemStack stack, String key){
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(key);
    }

    public static boolean contains(ItemStack stack, String key, int type){
        CompoundTag tag = get(stack);
        return tag != null && tag.contains(key, type);
    }

    public static void remove(ItemStack stack, String key){
        if(contains(stack, key)) edit(stack, t -> t.remove(key));
    }

    public static void put(ItemStack stack, String key, Tag value){ edit(stack, t -> t.put(key, value)); }
    public static void putCompound(ItemStack stack, String key, CompoundTag value){ put(stack, key, value); }
    public static void putInt(ItemStack stack, String key, int value){ edit(stack, t -> t.putInt(key, value)); }
    public static void putLong(ItemStack stack, String key, long value){ edit(stack, t -> t.putLong(key, value)); }
    public static void putDouble(ItemStack stack, String key, double value){ edit(stack, t -> t.putDouble(key, value)); }
    public static void putFloat(ItemStack stack, String key, float value){ edit(stack, t -> t.putFloat(key, value)); }
    public static void putBoolean(ItemStack stack, String key, boolean value){ edit(stack, t -> t.putBoolean(key, value)); }
    public static void putString(ItemStack stack, String key, String value){ edit(stack, t -> t.putString(key, value)); }
    public static void putUUID(ItemStack stack, String key, UUID value){ edit(stack, t -> t.putUUID(key, value)); }

    @Nullable
    public static CompoundTag getCompound(ItemStack stack, String key){
        CompoundTag tag = get(stack);
        return tag != null && tag.contains(key, Tag.TAG_COMPOUND) ? tag.getCompound(key) : null;
    }
    public static boolean hasCompound(ItemStack stack, String key){ return contains(stack, key, Tag.TAG_COMPOUND); }
    public static boolean hasString(ItemStack stack, String key){ return contains(stack, key, Tag.TAG_STRING); }
    public static boolean hasNumber(ItemStack stack, String key){ return contains(stack, key, Tag.TAG_ANY_NUMERIC); }
    public static boolean hasUUID(ItemStack stack, String key){
        CompoundTag tag = get(stack);
        return tag != null && tag.hasUUID(key);
    }

    public static int getInt(ItemStack stack, String key){ return getInt(stack, key, 0); }
    public static int getInt(ItemStack stack, String key, int def){
        CompoundTag tag = get(stack);
        return tag != null && tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : def;
    }
    public static long getLong(ItemStack stack, String key, long def){
        CompoundTag tag = get(stack);
        return tag != null && tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : def;
    }
    public static double getDouble(ItemStack stack, String key, double def){
        CompoundTag tag = get(stack);
        return tag != null && tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getDouble(key) : def;
    }
    public static boolean getBoolean(ItemStack stack, String key){
        CompoundTag tag = get(stack);
        return tag != null && tag.getBoolean(key);
    }
    @Nullable
    public static String getString(ItemStack stack, String key){
        CompoundTag tag = get(stack);
        return tag != null && tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : null;
    }
    @Nullable
    public static UUID getUUID(ItemStack stack, String key){
        CompoundTag tag = get(stack);
        return tag != null && tag.hasUUID(key) ? tag.getUUID(key) : null;
    }

    // ---- iotas -------------------------------------------------------------------------------------------------
    // Hex 0.12 serializes iotas with codecs instead of hand-written NBT. These encode with plain NbtOps, which
    // works for every builtin iota type (entities are stored by UUID). Pass a registry lookup when you have one.

    @Nullable
    public static Tag encodeIota(Iota iota, @Nullable HolderLookup.Provider registries){
        var ops = registries == null ? NbtOps.INSTANCE : RegistryOps.create(NbtOps.INSTANCE, registries);
        return IotaType.TYPED_CODEC.encodeStart(ops, iota).result().orElse(null);
    }

    @Nullable
    public static Iota decodeIota(@Nullable Tag tag, @Nullable HolderLookup.Provider registries){
        if(tag == null) return null;
        var ops = registries == null ? NbtOps.INSTANCE : RegistryOps.create(NbtOps.INSTANCE, registries);
        return IotaType.TYPED_CODEC.parse(ops, tag).result().orElse(null);
    }

    public static void putIota(ItemStack stack, String key, @Nullable Iota iota){
        if(iota == null){
            remove(stack, key);
            return;
        }
        Tag encoded = encodeIota(iota, null);
        if(encoded != null) put(stack, key, encoded);
    }

    @Nullable
    public static Iota getIota(ItemStack stack, String key){
        CompoundTag tag = get(stack);
        if(tag == null || !tag.contains(key)) return null;
        return decodeIota(tag.get(key), null);
    }
}
