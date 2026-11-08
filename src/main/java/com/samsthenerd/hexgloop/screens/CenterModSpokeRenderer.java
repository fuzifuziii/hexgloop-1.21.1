package com.samsthenerd.hexgloop.screens;

import com.samsthenerd.hexgloop.misc.wnboi.IotaProvider;
import com.samsthenerd.wnboi.screen.SpokeRenderer;
import net.minecraft.world.phys.Vec3;

// for the center of it
public class CenterModSpokeRenderer extends SpokeRenderer{
    public boolean currentPage = false;
    IotaProvider iotaProvider;


    CenterModSpokeRenderer(double orX, double orY, double rad, int numSecs, int secIndex, IotaProvider _iotaProvider){
        super(orX, orY, rad, numSecs, secIndex);
        iotaProvider = _iotaProvider;
        innerOutlineWeight = 1;
        numDivisions = 10;
    }

    @Override
    public int getColorFill(int vI, int numOuter, int numInner, boolean isInner){
        if(currentPage){
            Vec3 colorPos;
            if(isInner){
                colorPos = new Vec3(iotaProvider.getRNG().nextFloat()*0.5, 
                    sectionIndex + iotaProvider.getRNG().nextFloat()*0.5, iotaProvider.getRNG().nextFloat()*0.5).scale(0.3);
            } else {
                colorPos = new Vec3(sectionIndex + iotaProvider.getRNG().nextFloat()*0.5, 
                    iotaProvider.getRNG().nextFloat()*0.5, sectionIndex + iotaProvider.getRNG().nextFloat()*0.5).scale(0.3);
            }
            int color = iotaProvider.getColorizer().getColorProvider().getColor((float) currentTime, colorPos);
            return color;
        }
        return 0x00000000; // clear
    }

    @Override
    public int getColorOutline(int vI){
        if(currentPage){
            return 0x00000000; // clear
        }
        Vec3 colorPos = new Vec3(vI + iotaProvider.getRNG().nextFloat()*0.5, 
            sectionIndex + iotaProvider.getRNG().nextFloat()*0.5, sectionIndex + iotaProvider.getRNG().nextFloat()*0.5).scale(0.25);
        int color = iotaProvider.getColorizer().getColorProvider().getColor((float) currentTime, colorPos);
        return color;
    }
}
