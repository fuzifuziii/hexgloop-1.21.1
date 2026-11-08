package com.samsthenerd.hexgloop.misc.worldData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.samsthenerd.hexgloop.HexGloop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

// i guess could also just store on player nbt but nah
public class AgreeTruenameEULAState extends SavedData{
    // maps player uuids to their truename lock uuids
    private Set<UUID> playersWhoAgreed;

    @Nullable
    public boolean checkAgreement(UUID playerUUID){
        return playersWhoAgreed.contains(playerUUID);
    }

    public void setAgreement(UUID playerUUID, boolean agreed){
        if(agreed)
            playersWhoAgreed.add(playerUUID);
        else
            playersWhoAgreed.remove(playerUUID);
        setDirty();
    }

    public AgreeTruenameEULAState(){
        super();
        playersWhoAgreed = new HashSet<>();
    }

    @Override
    public CompoundTag save(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag agreedPlayers = new ListTag();
        for(UUID playerUuid : playersWhoAgreed){
            agreedPlayers.add(StringTag.valueOf(playerUuid.toString()));
        }
        nbt.put("agreedPlayers", agreedPlayers);
        return nbt;
    }
 
    public static AgreeTruenameEULAState createFromNbt(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        AgreeTruenameEULAState serverState = new AgreeTruenameEULAState();
        ListTag agreedPlayers = tag.getList("agreedPlayers", Tag.TAG_STRING);
        for(Tag agreedPlayer : agreedPlayers){
            serverState.setAgreement(UUID.fromString(agreedPlayer.getAsString()), true);
        }
        return serverState;
    }

    public static AgreeTruenameEULAState getServerState(MinecraftServer server) {
        // First we get the persistentStateManager for the OVERWORLD
        DimensionDataStorage persistentStateManager = server
                .getLevel(Level.OVERWORLD).getDataStorage();
 
        // Calling this reads the file from the disk if it exists, or creates a new one and saves it to the disk
        AgreeTruenameEULAState serverState = persistentStateManager.computeIfAbsent(
                new SavedData.Factory<>(AgreeTruenameEULAState::new, AgreeTruenameEULAState::createFromNbt),
                HexGloop.MOD_ID + "_truename_eula_state"); 

        serverState.setDirty(); // YOU MUST DO THIS!!!! Or data wont be saved correctly.
 
        return serverState;
    }
}
