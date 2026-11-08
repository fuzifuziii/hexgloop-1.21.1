package com.samsthenerd.hexgloop.screens;

import net.minecraft.client.gui.GuiGraphics;
import com.samsthenerd.hexgloop.items.ItemFidget.FidgetSettings;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker.Label;
import com.samsthenerd.hexgloop.misc.wnboi.LabelProvider;
import com.samsthenerd.wnboi.screen.SpokeRenderer;
import net.minecraft.util.Tuple;

public class FidgetSpokeRenderer extends SpokeRenderer{
    LabelProvider labelProvider;
    FidgetSettings settings;
    FidgetSpokeRenderer(double orX, double orY, double rad, int secIndex, LabelProvider labelProvider, FidgetSettings settings){
        super(orX, orY, rad, labelProvider.getCount(), secIndex);
        this.labelProvider = labelProvider;
        this.settings = settings;
        Tuple<Integer, Integer> curveLore = settings.getCurveOptions(secIndex, labelProvider.currentSlot() -1 == secIndex, isSelected);
        this.curveOptions = curveLore.getA();
        this.numDivisions = curveLore.getB();
        innerOutlineWeight = 1;
    }

    @Override
    public void select(){
        super.select();
        innerOutlineWeight = 1.5;
        Tuple<Integer, Integer> curveLore = settings.getCurveOptions(sectionIndex, labelProvider.currentSlot() -1 == sectionIndex, isSelected);
        this.curveOptions = curveLore.getA();
        this.numDivisions = curveLore.getB();
    } 

    @Override
    public void unselect(){
        super.unselect();
        innerOutlineWeight = 1;
        Tuple<Integer, Integer> curveLore = settings.getCurveOptions(sectionIndex, labelProvider.currentSlot() -1 == sectionIndex, isSelected);
        this.curveOptions = curveLore.getA();
        this.numDivisions = curveLore.getB();
    }

    @Override
    public int getColorFill(int vI, int numOuter, int numInner, boolean isInner){
        return settings.getColorFill(sectionIndex, vI, numOuter, numInner, isInner, labelProvider.currentSlot() -1 == sectionIndex, isSelected);
    }

    @Override
    public int getColorOutline(int vI){
        return settings.getColorOutline(sectionIndex, vI, labelProvider.currentSlot() -1 == sectionIndex, isSelected);
    }

    @Override
    public void renderLabel(GuiGraphics matrices, int mouseX, int mouseY, float delta){
        Label label = labelProvider.getLabelMaker().getLabel(sectionIndex+1);
        if(label == null) {
            label = settings.getDefaultLabel(sectionIndex, labelProvider.currentSlot() -1 == sectionIndex, isSelected);
            if(label == null) return;
        }
        double workingOuterRadius = outerRadius;
        double workingInnerRadius = innerRadius;
        if(numDivisions == 2){
            workingInnerRadius /= 2;
        }
        // if(numDivisions == 2 && RenderUtils.getOuterCurve(curveOptions) == 1){
        //     workingOuterRadius *= Math.sqrt(2);
        // }
        // HexGloop.logPrint("rendering label " + (iotaIndex+1) + ": " + label.toNbt().toString());
        int labelDistToUse = (labelDist == null) ? (int)((workingOuterRadius-workingInnerRadius) / 2f + workingInnerRadius) : labelDist;
        int x = (int)(originX+offsetX+Math.cos(midAngle)*labelDistToUse);
        int y = (int)(originY+offsetY+Math.sin(midAngle)*labelDistToUse);
        label.render(matrices.pose(), x, y, 24, 24);
    }
}
