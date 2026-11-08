package com.samsthenerd.hexgloop.items;

import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;


public class ItemCosmeticEnergizer extends Item {
    public ItemCosmeticEnergizer(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if(state.getBlock() instanceof BlockCircleComponent){
            if(state.getBlock() instanceof BlockAbstractImpetus){

                if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof BlockEntityAbstractImpetus impetus && impetus instanceof ILociHandler handler && !handler.getTrackedBlocks().isEmpty()){
                    return InteractionResult.PASS;
                }
            }
            context.getLevel().setBlockAndUpdate(context.getClickedPos(), state.setValue(BlockCircleComponent.ENERGIZED, !state.getValue(BlockCircleComponent.ENERGIZED)));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
