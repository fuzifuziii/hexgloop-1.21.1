package com.samsthenerd.hexgloop.misc.worldData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.samsthenerd.hexgloop.HexGloop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class TruenameLockState extends SavedData{
    // maps player uuids to their truename lock uuids
    private Map<UUID, UUID> uuidMap;

    @Nullable
    public UUID getLockUUID(UUID playerUUID){
        return uuidMap.get(playerUUID);
    }

    public void setLockUUID(UUID playerUUID, UUID lockUUID){
        uuidMap.put(playerUUID, lockUUID);
        setDirty();
    }

    public TruenameLockState(){
        super();
        uuidMap = new HashMap<UUID, UUID>();
    }

    @Override
    public CompoundTag save(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag lockMapUUID = new CompoundTag();
        for(UUID playerUuid : uuidMap.keySet()){
            UUID lockUuid = uuidMap.get(playerUuid);
            lockMapUUID.putString(playerUuid.toString(), lockUuid.toString());
        }
        nbt.put("lockMap", lockMapUUID);
        return nbt;
    }
 
    public static TruenameLockState createFromNbt(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        TruenameLockState serverState = new TruenameLockState();
        CompoundTag lockMapNbt = tag.getCompound("lockMap");
        for(String playerUuidString : lockMapNbt.getAllKeys()){
            String lockUuidString = lockMapNbt.getString(playerUuidString);
            serverState.setLockUUID(UUID.fromString(playerUuidString), UUID.fromString(lockUuidString));
        }
        return serverState;
    }

    public static TruenameLockState getServerState(MinecraftServer server) {
        // First we get the persistentStateManager for the OVERWORLD
        DimensionDataStorage persistentStateManager = server
                .getLevel(Level.OVERWORLD).getDataStorage();
 
        // Calling this reads the file from the disk if it exists, or creates a new one and saves it to the disk
        TruenameLockState serverState = persistentStateManager.computeIfAbsent(
                new SavedData.Factory<>(TruenameLockState::new, TruenameLockState::createFromNbt),
                HexGloop.MOD_ID + "_truename_lock_state"); 

        serverState.setDirty(); // YOU MUST DO THIS!!!! Or data wont be saved correctly.
 
        return serverState;
    }
}
