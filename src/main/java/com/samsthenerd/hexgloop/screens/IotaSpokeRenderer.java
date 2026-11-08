package com.samsthenerd.hexgloop.screens;

import net.minecraft.client.gui.GuiGraphics;
import com.samsthenerd.hexgloop.misc.wnboi.IotaProvider;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker.Label;
import com.samsthenerd.wnboi.screen.SpokeRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.Vec3;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;

public class IotaSpokeRenderer extends SpokeRenderer{
    public int iotaIndex = 0;
    public IotaProvider iotaProvider;

    IotaSpokeRenderer(double orX, double orY, double rad, int numSecs, int secIndex, int _iotaIndex, IotaProvider _iotaProvider){
        super(orX, orY, rad, numSecs, secIndex);
        iotaIndex = _iotaIndex;
        iotaProvider = _iotaProvider;
    }

    @Override
    public void select(){
        super.select();
        innerOutlineWeight = 1;
    }

    @Override
    public int getColorFill(int vI, int numOuter, int numInner, boolean isInner){
        if(!isInner){
            CompoundTag iotaNbt = iotaProvider.getIotaNBT(iotaIndex);
            if(iotaNbt == null) return ARGB32.color(150, 200, 200, 200);
            int color = com.samsthenerd.hexgloop.utils.StackNBT.decodeIota(iotaNbt, net.minecraft.client.Minecraft.getInstance().level.registryAccess()).getType().color();
            return ARGB32.color(150, ARGB32.red(color), ARGB32.green(color), ARGB32.blue(color));
        } else if(iotaProvider.currentSlot() == iotaIndex+1){
            CompoundTag iotaNbt = iotaProvider.getIotaNBT(iotaIndex);
            if(iotaNbt == null) return ARGB32.color(150, 200, 200, 200);
            int color = com.samsthenerd.hexgloop.utils.StackNBT.decodeIota(iotaNbt, net.minecraft.client.Minecraft.getInstance().level.registryAccess()).getType().color();
            return ARGB32.color(200, ARGB32.red(color), ARGB32.green(color), ARGB32.blue(color));
        }
        return ARGB32.color(96, 200, 200, 200);
    }

    @Override
    public int getColorOutline(int vI){
        Vec3 colorPos = new Vec3(vI + iotaProvider.getRNG().nextFloat()*0.5, 
            iotaIndex + iotaProvider.getRNG().nextFloat()*0.5, vI + iotaProvider.getRNG().nextFloat()*0.5).scale(0.8);
        int color = iotaProvider.getColorizer().getColorProvider().getColor((float) currentTime, colorPos);
        return color;
    }

    @Override
    public void renderLabel(GuiGraphics matrices, int mouseX, int mouseY, float delta){
        Label label = iotaProvider.getLabelMaker().getLabel(iotaIndex+1);
        if(label == null) {
            return;
        }
        // HexGloop.logPrint("rendering label " + (iotaIndex+1) + ": " + label.toNbt().toString());
        int labelDistToUse = (labelDist == null) ? (int)((outerRadius-innerRadius) / 2 + innerRadius) : labelDist;
        int x = (int)(originX+offsetX+Math.cos(midAngle)*labelDistToUse);
        int y = (int)(originY+offsetY+Math.sin(midAngle)*labelDistToUse);
        label.render(matrices.pose(), x, y, 24, 24);
    }

}
