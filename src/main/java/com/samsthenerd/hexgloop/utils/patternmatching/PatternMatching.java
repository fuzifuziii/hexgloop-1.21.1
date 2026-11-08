package com.samsthenerd.hexgloop.utils.patternmatching;



import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

public class PatternMatching {
    public static boolean match(HexPattern patA, HexPattern patB){
        return HexLine.parse(patA.getSignature().toAnglesString()).equalModStrokes(HexLine.parse(patB.getSignature().toAnglesString())).isPresent();
    }

    private static Registry<ActionRegistryEntry> actionRegistry(){
        return IXplatAbstractions.INSTANCE.getActionRegistry();
    }

    // returns ID of matching great spell or null
    public static ResourceLocation matchGreatSpell(HexPattern patA){
        Registry<ActionRegistryEntry> reg = actionRegistry();
        Optional<HolderSet.Named<ActionRegistryEntry>> greats = reg.getTag(HexTags.Actions.PER_WORLD_PATTERN);
        if(greats.isEmpty()) return null;
        for(Holder<ActionRegistryEntry> holder : greats.get()){
            try{
                // the prototype is the registry's canonical shape, the real shape is scrungled per world
                if(match(patA, holder.value().prototype())){
                    return holder.unwrapKey().map(ResourceKey::location).orElse(null);
                }
            } catch (IllegalArgumentException e){
                continue;
            }
        }
        return null;
    }

    @Nullable
    public static ResourceLocation getIdentifier(HexPattern pat){
        // normal patterns (this also covers escape/parens, which are plain actions in 0.12)
        Registry<ActionRegistryEntry> reg = actionRegistry();
        for(var entry : reg.entrySet()){
            if(entry.getValue().prototype().getSignature().equals(pat.getSignature())){
                return entry.getKey().location();
            }
        }
        // TODO(1.21 port): special handlers (numbers, bookkeeper's) need a CastingEnvironment to match, not looked up here
        // otherwise must be a great spell - or nothing
        return matchGreatSpell(pat);
    }

    public static Component getName(HexPattern pat){
        ResourceLocation id = getIdentifier(pat);
        if(id != null){
            ResourceKey<ActionRegistryEntry> key = ResourceKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), id);
            boolean great = actionRegistry().getHolder(key).map(h -> h.is(HexTags.Actions.PER_WORLD_PATTERN)).orElse(false);
            return HexAPI.instance().getActionI18n(key, great);
        }
        // TODO(1.21 port): PatternNameHelper.representationForPattern is gone, plain angle string as fallback
        return Component.literal(pat.getSignature().toAnglesString());
    }
}
