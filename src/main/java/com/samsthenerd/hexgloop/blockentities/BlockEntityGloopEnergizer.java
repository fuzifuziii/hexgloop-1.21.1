package com.samsthenerd.hexgloop.blockentities;

import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.samsthenerd.hexgloop.blocks.BlockGloopEnergizer;
import com.samsthenerd.hexgloop.recipes.GloopingRecipes;
import com.samsthenerd.hexgloop.recipes.GloopingRecipes.GloopingRecipe;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.common.lib.HexItems;

public class BlockEntityGloopEnergizer extends BlockEntity implements Container{
    private static final int MAX_CAPACITY = 2_000_000_000;
    private static final double DUST_COST = 0.25; // per second per block
    private static final double PARTICLE_SPAWN_CHANCE = 0.02;
    private static final ParticleOptions[] POSSIBLE_PARTICLES = {ParticleTypes.SNEEZE, ParticleTypes.EFFECT};
    Random rand = new Random();
    protected ItemStack latestResult = ItemStack.EMPTY;
    private int numConnectedBlocks = 0;
    private int media = 0;
    
    public BlockEntityGloopEnergizer(BlockPos pos, BlockState state) {
        super(HexGloopBEs.GLOOP_ENERGIZER_BE.get(), pos, state);
    }

    // tries to find a recipe it can make and makes it
    public void makeGloopSoup(BlockPos centerPos){
        if(!(level instanceof ServerLevel)) return;
        if(this.media < MediaConstants.DUST_UNIT * 10) return;
        GloopingRecipe bestRec = detectRecipe();
        if(bestRec == null) return;
        this.media -= bestRec.getMediaCost();
        AABB box = new AABB(worldPosition).inflate((double) BlockGloopEnergizer.BOILER_RADIUS);
        List<Entity> potentialIngredients = level.getEntities(null, box);
        doGloopCraft(bestRec, potentialIngredients, centerPos);
    }

    public Entity doGloopCraft(GloopingRecipe rec, List<Entity> potentialIngredients, BlockPos spawnPos){
        if(!(level instanceof ServerLevel)) return null;
        if(rec == null) return null;
        ItemStack result = rec.craft(potentialIngredients, true).copy();
        if(result.isEmpty()) return null;
        result.setCount(1);
        ItemEntity yummySoup = new ItemEntity(level, spawnPos.getX() + 0.5, spawnPos.getY() + 1.5, spawnPos.getZ() + 0.5, result);
        yummySoup.setInvulnerable(true);
        boolean didSpawn = level.addFreshEntity(yummySoup);
        // HexGloop.logPrint("spawned gloop soup product: " + didSpawn + "; " + yummySoup.getUuidAsString() + " at " + yummySoup.getPos().toString());
        return yummySoup;
        // return EntityType.ITEM.spawnFromItemStack((ServerWorld)world, result, null, spawnPos, SpawnReason.EVENT, true, false);
    }

    public GloopingRecipe detectRecipe(){
        AABB box = new AABB(worldPosition).inflate((double) BlockGloopEnergizer.BOILER_RADIUS);
        List<Entity> potentialIngredients = level.getEntities(null, box);
        GloopingRecipe rec = GloopingRecipes.findRecipe(potentialIngredients, level.getRecipeManager());
        latestResult = rec == null ? ItemStack.EMPTY : rec.getOutput();
        sync();
        return rec;
    }

    // want to use up media and also spawn some particles
    public void tick(Level world, BlockPos pos, BlockState state){
        if(!(world instanceof ServerLevel)) return;
        if(this.media > 0 || this.media == -1){
            Set<BlockPos> adjWater = BlockGloopEnergizer.getAdjacentWater(world, pos);
            numConnectedBlocks = adjWater.size();
            if(!adjWater.isEmpty()){
                // just getting rid of media drain for now
                // double decAmt = MediaConstants.DUST_UNIT / 20.0;
                // this.media = (int) Math.max(((double)this.media) - decAmt, 0);
                for(BlockPos waterPos : adjWater){
                    if(rand.nextDouble() > PARTICLE_SPAWN_CHANCE) continue;
                    ((ServerLevel) world).sendParticles(ParticleTypes.SNEEZE, 
                        waterPos.getX() + 0.5 + rand.nextDouble()*0.5, waterPos.getY() + 0.3 + rand.nextDouble()*0.5, waterPos.getZ()+0.5 + rand.nextDouble()*0.5, 1, 0, 0.3, 0, 0.01);
                }
            }
            sync();
        }
    }

    public ItemStack getLatestResult(){
        return latestResult;
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

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.media = nbt.getInt("media");
        this.numConnectedBlocks = nbt.getInt("numConnectedBlocks");
        this.latestResult = ItemStack.parseOptional(registries, nbt.getCompound("latestResult"));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putInt("media", this.media);
        nbt.putInt("numConnectedBlocks", this.numConnectedBlocks);
        nbt.put("latestResult", this.latestResult.saveOptional(registries));
    }

    public int getNumConnected(){
        return this.numConnectedBlocks;
    }

    
    // yoink inventory/media stuff from impetus
    private static final int[] SLOTS = {0};

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        insertMedia(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        // NO-OP
    }

    public int getMedia() {
        return this.media;
    }

    public void setMedia(int media) {
        this.media = media;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (remainingMediaCapacity() == 0) {
            return false;
        }

        if (stack.is(HexItems.CREATIVE_UNLOCKER.get())) {
            return true;
        }

        var mediamount = extractMediaFromItem(stack, true);
        return mediamount > 0;
    }

    public int extractMediaFromItem(ItemStack stack, boolean simulate) {
        if (this.media < 0) {
            return 0;
        }
        return (int) MediaHelper.extractMedia(stack, remainingMediaCapacity(), true, simulate);
    }

    public void sync() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void insertMedia(ItemStack stack) {
        if (getMedia() >= 0 && !stack.isEmpty() && stack.getItem() == HexItems.CREATIVE_UNLOCKER) {
            setInfiniteMedia();
            stack.shrink(1);
        } else {
            var mediamount = extractMediaFromItem(stack, false);
            if (mediamount > 0) {
                this.media = Math.min(mediamount + media, MAX_CAPACITY);
                this.sync();
            }
        }
    }

    public void setInfiniteMedia() {
        this.media = -1;
        this.sync();
    }

    public int remainingMediaCapacity() {
        if (this.media < 0) {
            return 0;
        }
        return Math.max(0, MAX_CAPACITY - this.media);
    }
}
