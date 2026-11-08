package com.samsthenerd.hexgloop.mixins.mishapprotection;

import org.spongepowered.asm.mixin.Mixin;

import com.samsthenerd.hexgloop.casting.mishapprotection.IMishapStorage;
import net.minecraft.network.chat.Component;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

@Mixin(CastingEnvironment.class)
public class MixinStoreLastMishapContext implements IMishapStorage{
    private Component lastMishap = null;

    public Component getLastMishap(){
        return lastMishap;
    }

    public void setLastMishap(Component mishap){
        lastMishap = mishap;
    }
}
