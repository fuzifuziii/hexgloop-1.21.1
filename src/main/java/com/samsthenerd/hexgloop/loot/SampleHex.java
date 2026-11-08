package com.samsthenerd.hexgloop.loot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class SampleHex{
    public Rarity rarity = Rarity.COMMON;

    public List<Iota> patterns;
    public Component name;
    public @Nullable Component desc;

    // null if anywhere normal
    public Set<ResourceLocation> locations = null;
    // air if it can be spawned on any item
    public Set<? extends Item> forItems = Set.of(Items.AIR);
    public Set<FrozenPigment> pigments = null;
    public long mediaAmt = MediaConstants.SHARD_UNIT * 8;

    public Set<String> mods = new HashSet<>();

    public SampleHex(List<Iota> patterns, Component name, @Nullable Component desc){
        this.patterns = patterns;
        this.name = name;
        this.desc = desc;
    }

    public SampleHex withRarity(Rarity rarity){
        this.rarity = rarity;
        return this;
    }

    // if it should only spawn in specific places
    public SampleHex withLocations(Set<ResourceLocation> locations){
        this.locations = locations;
        return this;
    }

    public SampleHex withLocations(ResourceLocation... locations){
        this.locations = Set.of(locations);
        return this;
    }

    public SampleHex forItems(Set<? extends Item> itemTypes){
        forItems = itemTypes;
        return this;
    }

    public SampleHex forItems(Item... itemTypes){
        forItems = Set.of(itemTypes);
        return this;
    }

    public SampleHex withPigments(Set<FrozenPigment> pigments){
        this.pigments = pigments;
        return this;
    }

    public SampleHex withPigments(FrozenPigment... pigments){
        this.pigments = Set.of(pigments);
        return this;
    }
    
    public SampleHex withMediaAmt(long amt){
        this.mediaAmt = amt;
        return this;
    }

    public SampleHex requiredMods(String... modIDs){
        mods = Set.of(modIDs);
        return this;
    }
}

