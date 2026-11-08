package com.samsthenerd.hexgloop.misc.wnboi;



import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

/*
 * An interface that gives Iotas for the IotaWheelScreen. 
 * 
 * extends label provider so that all iotaproviders have label providers but we can still have other label providers
 */
public interface IotaProvider extends LabelProvider{

    public CompoundTag getIotaNBT(int index);

    public RandomSource getRNG();

    public Component getName(int index);

    public default FrozenPigment getColorizer(){
        return at.petrak.hexcasting.api.HexAPI.instance().getColorizer(Minecraft.getInstance().player);
    }
}