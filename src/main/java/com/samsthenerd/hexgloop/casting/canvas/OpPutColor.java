package com.samsthenerd.hexgloop.casting.canvas;

import java.util.ArrayList;
import java.util.List;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.items.ItemSlateCanvas;

import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

public class OpPutColor implements Action{

    public OpPutColor(){
    }

    @Override
    public OperationResult operate(CastingEnvironment context, CastingImage image, SpellContinuation continuation){
        // potentially get an entity first -- deal with that later
        var held = context.getHeldItemToOperateOn(itemstack -> itemstack.is(HexGloopItems.SLATE_CANVAS_ITEM.get()));
        ItemStack mapStack = held == null ? ItemStack.EMPTY : held.stack();

        TreeList<Iota> stack = image.getStack();

        int brushsize = 1;
        try {
            brushsize = OperatorUtils.getIntBetween(List.of(stack.get(stack.size()-1)), 0, 1, 128, 1);
            stack = stack.init();
        } catch (Throwable mishap) {
            // do nothing
            HexGloop.logPrint("mishap: " + mishap);
        }

        if(stack.size() < 3){
            MishapThrowerWrapper.throwMishap(new MishapNotEnoughArgs(3, stack.size()));
        }
        List<Iota> args = stack.takeRight(3);

        Integer x = null;
        if(!(args.get(0) instanceof NullIota)){
            x = OperatorUtils.getIntBetween(args, 0, 0, 127, 3);
        }
        Integer y = null;
        if(!(args.get(1) instanceof NullIota)){
            y = OperatorUtils.getIntBetween(args, 1, 0, 127, 3);
        }
        byte closestColor = MapColor.NONE.getPackedId(Brightness.NORMAL);
        if(!(args.get(2) instanceof NullIota)){
            Vec3 colorVec = OperatorUtils.getVec3(args, 2, 3);
            closestColor = SlateCanvasUtils.getClosestMapColor(new Vec3i((int)colorVec.x, (int)colorVec.y, (int)colorVec.z), context.getCastingEntity() instanceof Player p ? p : null);
        }

        stack = stack.dropRight(3);

        // TODO(1.21 port): map ids are DataComponents.MAP_ID now; ItemSlateCanvas.createMapState / setCanvasMapId are group A's
        MapId mapId = mapStack.get(DataComponents.MAP_ID);
        if(mapId == null){
            HexGloop.logPrint("creating a new map");
            Tuple<MapItemSavedData, Integer> newMapData = ItemSlateCanvas.createMapState(context.getWorld());
            ItemSlateCanvas.setCanvasMapId(mapStack, newMapData.getB());
            mapId = new MapId(newMapData.getB());
        }
        HexGloop.logPrint("map id: " + mapId);
        MapItemSavedData mapState = MapItem.getSavedData(mapId, context.getWorld());
        if(mapState == null){
            return MishapThrowerWrapper.result(MishapThrowerWrapper.withStack(image, stack), continuation);
        }
        if(x == null && y == null){ // paint whole thing
            for(int i = 0; i < 128; i++){
                for(int j = 0; j < 128; j++){
                    mapState.setColor(i, j, closestColor);
                }
            }
        } else {
            int startX = 0, startY = 0;
            int endX = 128, endY = 128;

            if(x != null){
                startX = (int)(x - Math.floor((brushsize-1)/2.0));
                endX = (int)(x + Math.ceil((brushsize+1)/2.0));
            }

            if(y != null){
                startY = (int)(y - Math.floor((brushsize-1)/2.0));
                endY = (int)(y + Math.ceil((brushsize+1)/2.0));
            }

            // clamp all the values down to actual sizes
            startX = Math.min(Math.max(0, startX), 128);
            startY = Math.min(Math.max(0, startY), 128);
            endX = Math.min(Math.max(0, endX), 128);
            endY = Math.min(Math.max(0, endY), 128);

            for(int i = startX; i < endX; i++){
                for(int j = startY; j < endY; j++){
                    mapState.setColor(i, j, closestColor);
                }
            }
        }
        return MishapThrowerWrapper.result(MishapThrowerWrapper.withStack(image, stack), continuation);
    }

}
