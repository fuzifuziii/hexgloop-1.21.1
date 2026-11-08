package com.samsthenerd.hexgloop.misc.clientgreatbook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.utils.patternmatching.PatternMatching;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;

public class GreatBook {

    public static GreatBook INSTANCE = new GreatBook();

    private Map<ResourceLocation, HexPattern> storedPatterns = new HashMap<>();

    @Nullable
    public HexPattern getPattern(ResourceLocation id){
        return storedPatterns.get(id);
    }

    public void savePattern(HexPattern pattern){
        ResourceLocation patternId = PatternMatching.matchGreatSpell(pattern);
        if(patternId != null){
            HexPattern prev = storedPatterns.put(patternId, pattern);
            if(!pattern.equals(prev)){
                Component patternName = PatternMatching.getName(pattern);
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("hexgloop.greatbook.saved", patternName, pattern.toString()));
                save();
            }
        }
    }

    public void clearPattern(ResourceLocation id){
        if(storedPatterns.remove(id) != null){
            Component patternName = HexAPI.instance().getActionI18n(ResourceKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), id), true);
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("hexgloop.greatbook.removed", patternName));
            save();
        }
    }

    public void clearAll(){
        if(!storedPatterns.isEmpty()){
            storedPatterns.clear();
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("hexgloop.greatbook.cleared"));
            save();
        }
    }

    public CompoundTag toNbt(){
        CompoundTag nbt = new CompoundTag();
        for(ResourceLocation id : storedPatterns.keySet()){
            nbt.put(id.toString(), HexPattern.CODEC.encodeStart(NbtOps.INSTANCE, storedPatterns.get(id)).getOrThrow());
        }
        return nbt;
    }

    public static GreatBook fromNbt(CompoundTag nbt){
        GreatBook book = new GreatBook();
        for(String id : nbt.getAllKeys()){
            book.storedPatterns.put(ResourceLocation.parse(id), HexPattern.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(id)).getOrThrow());
        }
        return book;
    }

    private static String asFileName(String original){
        return original.replaceAll("[^a-zA-Z0-9\\-_\\.]", "_");
    }

    @Nullable // null if it's not in a world
    public static File getFile(){
        Minecraft client = Minecraft.getInstance();
        if(client.hasSingleplayerServer()){ // saving it to the world       
            IntegratedServer server = client.getSingleplayerServer();
            Path worldPath = server.getWorldPath(LevelResource.ROOT);
            Path greatbookPath = worldPath.resolve("data/greatbook.dat");
            return greatbookPath.toFile();
        }
        ServerData sInfo = client.getCurrentServer();
        if(sInfo != null){
            String cleanAddress = asFileName(sInfo.ip);
            Path greatbookPath = client.gameDirectory.toPath().resolve("greatbook/greatbook-" + cleanAddress + ".dat");
            return greatbookPath.toFile();
        }
        return null;
    }

    public static GreatBook load(){
        HexGloop.logPrint("Loading greatbook");
        File file = getFile();
        if(file == null || !file.canRead()){
            return new GreatBook();
        }
        CompoundTag greatBookNbt = null;
        try{
            greatBookNbt = NbtIo.read(file.toPath());
        } catch(IOException e){
            HexGloop.LOGGER.error("Failed to load greatbook");
        }
        if(greatBookNbt == null){
            return new GreatBook();
        }
        return GreatBook.fromNbt(greatBookNbt);
    }

    public void save(){
        HexGloop.logPrint("Saving greatbook");
        File file = getFile();
        if(file == null){
            return;
        }
        file.getAbsoluteFile().getParentFile().mkdirs();
        try{
            file.createNewFile();
            CompoundTag greatBookNbt = this.toNbt();
            NbtIo.write(greatBookNbt, file.toPath());
        } catch(IOException e){
            HexGloop.LOGGER.error("Failed to save greatbook");
        }
    }
    public static void registerLoadEvent(){
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(world -> {
            INSTANCE = load();
        });
    }
}
