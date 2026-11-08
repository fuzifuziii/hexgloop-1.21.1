package com.samsthenerd.hexgloop.mixins.orchard;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

import com.samsthenerd.hexgloop.casting.orchard.IOrchardKeybind;

import at.petrak.hexcasting.api.casting.math.HexPattern;

@Mixin(LocalPlayer.class)
public class MixinClientPlayerOrchardKeybind implements IOrchardKeybind{
    protected Set<String> activatedPatterns = new HashSet<String>();

    public void setAssociation(HexPattern pattern, boolean isActivated){
        if(pattern == null) return;
        if(isActivated){
            activatedPatterns.add(pattern.getSignature().toAnglesString());
        }else{
            activatedPatterns.remove(pattern.getSignature().toAnglesString());
        }

    }

    public boolean getAssociation(HexPattern pattern){
        return activatedPatterns.contains(pattern.getSignature().toAnglesString());
    }
}
