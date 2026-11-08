package com.samsthenerd.hexgloop.blockentities;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.blocks.BlockIoticDial;
import com.samsthenerd.hexgloop.blocks.iotic.IIoticProvider;
import com.samsthenerd.hexgloop.items.ItemMultiFocus;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

public class BlockEntityDial extends BlockEntity implements IIoticProvider {

    private ItemStack innerMultiFocus = ItemStack.EMPTY;

    public BlockEntityDial(BlockPos pos, BlockState state) {
        super(HexGloopBEs.DIAL_BE.get(), pos, state);
    }

    public ItemStack getInnerMultiFocus() {
        return innerMultiFocus;
    }

    // so that we can handle changing the blockstate
    public void setInnerMultiFocus(ItemStack innerMultiFocus) {
        this.innerMultiFocus = innerMultiFocus;
        BlockState old = level.getBlockState(worldPosition);
        int sel = 0;
        if(!innerMultiFocus.isEmpty()){
            sel = ItemSpellbook.getPage(innerMultiFocus, 1);
        }
        level.setBlockAndUpdate(worldPosition, old.setValue(BlockIoticDial.SELECTED, sel));
        HexGloop.logPrint("new inner multi focus !");
        setChanged();
    }

    public void setSelection(int selection){
        selection = Mth.clamp(selection, 0, 6);
        BlockState old = level.getBlockState(worldPosition);
        level.setBlockAndUpdate(worldPosition, old.setValue(BlockIoticDial.SELECTED, selection));
        if(selection != 0){
            ItemMultiFocus.setPageIdx(innerMultiFocus, selection);
        }
        setChanged();
    }

    public int getSelection(){
        return level.getBlockState(worldPosition).getValue(BlockIoticDial.SELECTED);
    }

    // TODO: wrap this so we can get a hook when the iota is changed
    public ADIotaHolder getIotaHolder(Level world, BlockPos pos){
        return IXplatAbstractions.INSTANCE.findDataHolder(innerMultiFocus);
    }

    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        innerMultiFocus = ItemStack.parseOptional(registries, nbt.getCompound("innerMultiFocus"));
    }

    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("innerMultiFocus", innerMultiFocus.saveOptional(registries));
    }

    @Override
    public void setChanged() {
        if (level instanceof ServerLevel sWorld) {
            sWorld.getChunkSource().blockChanged(worldPosition);
        }
        super.setChanged();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
