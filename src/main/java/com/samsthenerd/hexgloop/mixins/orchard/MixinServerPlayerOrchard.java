package com.samsthenerd.hexgloop.mixins.orchard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.samsthenerd.hexgloop.casting.orchard.IOrchard;
import com.samsthenerd.hexgloop.casting.orchard.IOrchardKeybind;

import at.petrak.hexcasting.api.casting.math.HexPattern;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerOrchard implements IOrchard, IOrchardKeybind{
    protected List<Double> orchardList = List.of(0.0);
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

    @Override
    @NotNull
    public List<Double> getOrchardList(){
        if(orchardList == null || orchardList.size() < 1) return List.of(0.0);
        return List.copyOf(orchardList);
    }

    @Override
    public void setOrchardList(List<Double> list){
        orchardList = List.copyOf(list);
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
    at=@At(value="TAIL"))
    public void deserOrchard(CompoundTag nbt, CallbackInfo ci){
        if(nbt.contains("mindsorchard", Tag.TAG_LIST)){
            ListTag orchardNbtList = nbt.getList("mindsorchard", Tag.TAG_DOUBLE);
            List<Double> newOrchard = new ArrayList<Double>();
            for(int i = 0; i < orchardNbtList.size(); i++){
                newOrchard.add(orchardNbtList.getDouble(i));
            }
            orchardList = newOrchard;
        }
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
    at=@At(value="TAIL"))
    public void serOrchard(CompoundTag nbt, CallbackInfo ci){
        if(orchardList != null){
            ListTag orchardNbtList = new ListTag();
            for(int i = 0; i < orchardList.size(); i++){
                orchardNbtList.add(orchardNbtList.size(), DoubleTag.valueOf(orchardList.get(i)));
            }
            nbt.put("mindsorchard", orchardNbtList);
        }
    }
}
