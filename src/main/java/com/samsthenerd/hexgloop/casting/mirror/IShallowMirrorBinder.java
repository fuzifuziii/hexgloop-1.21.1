package com.samsthenerd.hexgloop.casting.mirror;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IShallowMirrorBinder {

    // put it out here so we can use it elsewhere :)
    public static final EntityDataAccessor<ItemStack> HELD_STACK = SynchedEntityData.defineId(Player.class, EntityDataSerializers.ITEM_STACK);

    // Force HELD_STACK allocation during mod init, before any Player exists.
    // 1.21: SynchedEntityData.Builder is sized from ClassTreeIdRegistry.getCount() at construction;
    // if defineId() first runs inside Player.defineSynchedData (via the TAIL inject in
    // MixinPlayerMirrorStuff), the builder is already 21 slots and builder.define(HELD_STACK, ...)
    // throws ArrayIndexOutOfBoundsException: Index 21 out of bounds for length 21 -> "Неверные данные игрока".
    static void init() {
        int ignored = HELD_STACK.id();
    }

     // should only really be used for rendering
    public ItemStack getTrackedStack();
}
