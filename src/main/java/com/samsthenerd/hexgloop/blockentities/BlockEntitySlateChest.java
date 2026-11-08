package com.samsthenerd.hexgloop.blockentities;

import com.samsthenerd.hexgloop.blocks.BlockSlateChest;
import com.samsthenerd.hexgloop.mixins.lociathome.MixinExposeChestInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntitySlateChest extends ChestBlockEntity {
    private boolean gloopy = false;

    public BlockEntitySlateChest(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        gloopy = false;
        if(blockState.getBlock() instanceof BlockSlateChest slateBlock){
            gloopy = slateBlock.isGloopy();
        }
        ((MixinExposeChestInventory)(Object)this).setInventory(NonNullList.withSize(9 * (gloopy ? 6 : 4), ItemStack.EMPTY));
    }

    public BlockEntitySlateChest(BlockPos pos, BlockState state) {
        this(HexGloopBEs.SLATE_CHEST_BE.get(), pos, state);
    }

    public boolean isGloopy(){
        return gloopy;
    }

    public NonNullList<ItemStack> getInvStackListPublic(){
        return getItems();
    }

    protected Component getDefaultName() {
        return Component.translatable(isGloopy() ? "block.hexgloop.gloopy_slate_chest" : "block.hexgloop.slate_chest");
     }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        if(gloopy){
            return ChestMenu.sixRows(syncId, playerInventory, this);
        } else {
            // hopefully this works ? i figure having it be slightly larger than a normal chest is nice
            return new ChestMenu(MenuType.GENERIC_9x4, syncId, playerInventory, this, 4);
        }
    }

    public int getContainerSize() {
        return 9 * (gloopy ? 6 : 4);
    }
}
