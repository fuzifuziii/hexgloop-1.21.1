package com.samsthenerd.hexgloop.blockentities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import com.samsthenerd.hexgloop.blocks.BlockPedestal;
import com.samsthenerd.hexgloop.blocks.IDynamicFlayTarget;
import com.samsthenerd.hexgloop.blocks.iotic.IIoticProvider;
import com.samsthenerd.hexgloop.items.IMindTargetItem;
import com.samsthenerd.hexgloop.misc.HexGloopTags;
import com.samsthenerd.hexgloop.misc.INoMoving;
import com.samsthenerd.hexgloop.recipes.ItemFlayingRecipe;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

// switching it to sided so that it can be used by modded chutes and whatnot hopefully ?
public class BlockEntityPedestal extends BlockEntity implements Container, IReallyHateForgeWhyWouldAnInventoryInterfaceNotBeAnInterfaceThatsWhatAnInterfaceIsFor, IDynamicFlayTarget, IIoticProvider {
    public static final String ITEM_DATA_TAG = "inv_storage";
    public static final String PERSISTENT_UUID_TAG = "persistent_uuid";

    public static final double HEIGHT = 0.75;

    private ItemStack storedItem = ItemStack.EMPTY;
    // item entity on top of pedestal
    private ItemEntity itemEnt = null; 
    private UUID persistentUUID = null;
    
    private boolean isMirror;
    private boolean hasMindStorage;
    private Villager storedMind = null;

    public BlockEntityPedestal(BlockPos pos, BlockState state) {
        super(HexGloopBEs.PEDESTAL_BE.get(), pos, state);
        isMirror = false;
        hasMindStorage = false;
        if(state.getBlock() instanceof BlockPedestal){
            isMirror = ((BlockPedestal)state.getBlock()).isMirror;
            hasMindStorage = ((BlockPedestal)state.getBlock()).hasMindStorage;
        }
        if(getLevel() != null && !getLevel().isClientSide()){
            persistentUUID = getNewUUID();
            setChanged();
            makeNewItemEntity();
        }
    }

    private UUID getNewUUID(){
        UUID newUUID;
        boolean isUnique = false;
        do{
            newUUID = UUID.randomUUID();
            if(getLevel() instanceof ServerLevel sWorld){
                isUnique = sWorld.getEntity(newUUID) == null;
            }
        } while(!isUnique);
        return newUUID;
    }

    public UUID getPersistentUUID(){
        return persistentUUID;
    }

    public Direction getNormal(){
        return getBlockState().getValue(BlockPedestal.FACING);
    }

    private void makeNewItemEntity(){
        if(getLevel() instanceof ServerLevel sWorld){
            if(storedItem == null || storedItem.isEmpty()){
                return;
            }
            if(persistentUUID == null){
                persistentUUID = getNewUUID();
            }
            // first need to deal with old one -- maybe
            if(itemEnt != null){
                itemEnt.discard();
                itemEnt = null;
            }
            Entity maybeItemEnt = sWorld.getEntity(persistentUUID);
            Vec3i norm = getNormal().getNormal();
            double heightOffset = HEIGHT - 0.5 + 0.01;
            double xPos = worldPosition.getX() + 0.5 + (heightOffset+0.2f) * norm.getX(); // + 0.125 to put it far enough out
            double yPos = worldPosition.getY() + 0.2 + (heightOffset * norm.getY()) + Math.abs(0.3 * norm.getY()) + (norm.getY() < 0 ? -0.7 : 0); // put it on the 'floor' when it's sideways so it's not floating too high
            double zPos = worldPosition.getZ() + 0.5 + (heightOffset+0.2f) * norm.getZ();
            boolean needsToSpawn = false;
            if(maybeItemEnt instanceof ItemEntity someItemEnt){
                // if(someItemEnt.isRemoved()){
                //     // see if we can restore it -- no clue if this will actually work
                //     ((INoMoving)someItemEnt).callUnsetRemoval();
                // }
                // we have some item entity ? that seems to exist ?
                itemEnt = someItemEnt;
                itemEnt.setItem(storedItem);
            } else {
                needsToSpawn = true;
                itemEnt = new ItemEntity(sWorld, 
                    xPos, yPos, zPos, 
                    storedItem, 0, 0, 0);
            }
            itemEnt.setPosRaw(xPos, yPos, zPos);
            itemEnt.setUUID(persistentUUID);
            itemEnt.setNoGravity(true);
            itemEnt.noPhysics = true;
            itemEnt.setNeverPickUp();
            itemEnt.setUnlimitedLifetime();
            itemEnt.setInvulnerable(true);
            ((INoMoving)itemEnt).setNoMoving(true);
            if(needsToSpawn){
                sWorld.addFreshEntity(itemEnt);
            }
            setChanged();
        }
    }

    public void onRemoved(){
        if(itemEnt != null){
            itemEnt.discard();
        }
        if(level instanceof ServerLevel sWorld){
            Vec3i norm = getNormal().getNormal();
            double heightOffset = HEIGHT - 0.5 + 0.01;
            // spawn a normal item entity, just as a drop
            itemEnt = new ItemEntity(sWorld, 
                worldPosition.getX() + 0.5 + heightOffset * norm.getX(), 
                worldPosition.getY() + 0.5 + heightOffset * norm.getY(), 
                worldPosition.getZ() + 0.5 + heightOffset * norm.getZ(), 
                storedItem, 0, 0, 0);
            sWorld.addFreshEntity(itemEnt);
            setChanged();
        }
    }

    private Set<Player> interactedCheck = new HashSet<>();

    public void interacted(Player player){
        interactedCheck.add(player);
    }

    public InteractionResult use(Player player, InteractionHand hand, BlockHitResult hit){
        // make sure it doesn't get the second hand when it shouldn't
        if(interactedCheck.contains(player)) {
            interactedCheck.remove(player);
            return InteractionResult.CONSUME;
        }
        boolean isClient = level.isClientSide();
        String logLore = "[ " + (isClient ? "client" : "server") + " ] -  " + (hand == InteractionHand.MAIN_HAND ? "main" : "off") + ": ";
        ItemStack heldStack = player.getItemInHand(hand);
        ItemStack otherStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        // HexGloop.logPrint("entered use on [ " + (isClient ? "client" : "server") + " ] with heldStack [ " + heldStack + " ] and otherStack [ " + otherStack + " ]");
        if(storedItem==null || storedItem.isEmpty()){ // it's empty, so maybe put something in there
            // HexGloop.logPrint(logLore + "stored item empty");
            if(level.isClientSide){
                // HexGloop.logPrint(logLore + "returning on client based on heldStack.isEmpty()");
                return heldStack.isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS;
            }
            if(heldStack.isEmpty()){
                // HexGloop.logPrint(logLore + "heldstack empty");
                return InteractionResult.PASS;
            } else {
                setItem(0, heldStack.copy());
                heldStack.shrink(heldStack.getCount());
                // HexGloop.logPrint(logLore + "heldstack not empty");
                return InteractionResult.SUCCESS;
            }
        }
        // not empty, maybe take something out - or add it
        if(ItemStack.isSameItemSameComponents(storedItem, heldStack)){
            // HexGloop.logPrint(logLore + "can combine with heldStack");
            if(!level.isClientSide){
                int amtToMove = Math.min(storedItem.getMaxStackSize() - storedItem.getCount(), heldStack.getCount());
                storedItem.grow(amtToMove);
                heldStack.shrink(amtToMove);
                syncItemWithEntity(true);
            }
            return InteractionResult.SUCCESS;
        }
        if(ItemStack.isSameItemSameComponents(storedItem, otherStack)){ // let it be handled by other hand ? 
            // HexGloop.logPrint(logLore + "can combine with otherStack");
            return InteractionResult.PASS;
        }
        // can't combine, so pop it and then replace only if main hand ?
        if(!level.isClientSide){
            ItemStack returnedStack = removeItemNoUpdate(0);
            if(hand == InteractionHand.MAIN_HAND){
                // HexGloop.logPrint(logLore + "putting main hand stack in pedestal");
                setItem(0, heldStack.copy());
                heldStack.shrink(heldStack.getCount());
            }
            if(!returnedStack.isEmpty()){
                // HexGloop.logPrint(logLore + "returning stack to player");
                if(player.getMainHandItem().isEmpty()){
                    player.setItemInHand(InteractionHand.MAIN_HAND, returnedStack);
                } else {
                    player.getInventory().placeItemBackInInventory(returnedStack);
                }
            }
        }
        // HexGloop.logPrint(logLore + "returning success");
        return InteractionResult.SUCCESS;
    }

    @Override
    public ADIotaHolder getIotaHolder(Level world, BlockPos pos){
        return IXplatAbstractions.INSTANCE.findDataHolder(storedItem);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.storedItem = ItemStack.parseOptional(registries, nbt.getCompound(ITEM_DATA_TAG));
        if(nbt.hasUUID(PERSISTENT_UUID_TAG))
            this.persistentUUID = nbt.getUUID(PERSISTENT_UUID_TAG);
        if(hasMindStorage && nbt.contains("stored_mind")){
            if(EntityType.create(nbt.getCompound("stored_mind"), level).orElse(null) instanceof Villager villager){
                storedMind = villager;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if(storedItem != null){
            nbt.put(ITEM_DATA_TAG, storedItem.saveOptional(registries));
        }
        if(persistentUUID != null){
            nbt.putUUID(PERSISTENT_UUID_TAG, persistentUUID);
        }
        if(storedMind != null && hasMindStorage){
            CompoundTag mindNbt = new CompoundTag();
            storedMind.saveAsPassenger(mindNbt);
            nbt.put("stored_mind", storedMind.saveWithoutId(mindNbt));
        }
    }

    public void syncItemWithEntity(boolean forceBlock){
        if(level.isClientSide()) return;
        if(storedItem == null || storedItem.isEmpty()){
            // no stored item so kill our entity
            if(itemEnt != null){
                // actually maybe don't discard it ourselves, it should take care of itself ?
                // itemEnt.discard();
                itemEnt.setItem(storedItem); // empty it, it'll die on tick ?
                itemEnt = null;
                setChanged();
            }
            return;
        } else { // have an item here
            if(itemEnt == null || itemEnt.isRemoved()){ // no entity
                // it's a mess but it just checks if the item entity is either discarded or killed and empty since some spells kill it when it's empty
                if(itemEnt != null && 
                    (itemEnt.getRemovalReason() == RemovalReason.DISCARDED || 
                        (itemEnt.getRemovalReason() == RemovalReason.KILLED && 
                        (itemEnt.getItem() == null || itemEnt.getItem().isEmpty()) 
                    ))
                && !forceBlock){
                    storedItem = ItemStack.EMPTY;
                    setChanged();
                } else {
                    makeNewItemEntity();
                }
            } else { // there is an entity and an item
                if(storedItem != itemEnt.getItem()){
                    if(forceBlock){
                        itemEnt.setItem(storedItem);
                    } else {
                        storedItem = itemEnt.getItem();
                        setChanged();
                    }
                }
            }
        }
    }

    public void tick(Level world, BlockPos pos, BlockState state){
        if(world.isClientSide()) return;
        interactedCheck.clear();
        List<ItemEntity> hopperableItemEnts = getInputItemEntities();
        hopperableItemEnts.sort((a, b) -> {
            return (int) (pos.distToCenterSqr(a.position().x, a.position().y, a.position().z) - pos.distToCenterSqr(b.position().x, b.position().y, b.position().z));
        });
        // sync entity to here first
        syncItemWithEntity(false);
        boolean wasItemUpdated = false;
        for(ItemEntity iEnt : hopperableItemEnts){
            wasItemUpdated = true;
            ItemStack entStack = iEnt.getItem();
            if(storedItem == null || storedItem.isEmpty()){
                storedItem = entStack.copy();
                entStack.shrink(entStack.getCount());
                break;
            }
            if(ItemStack.isSameItemSameComponents(storedItem, entStack)){
                int amtToMove = Math.min(storedItem.getMaxStackSize() - storedItem.getCount(), entStack.getCount());
                storedItem.grow(amtToMove);
                entStack.shrink(amtToMove);
                break;
            }
        }
        // force this update if needed
        if(wasItemUpdated) syncItemWithEntity(true);
        // move mind into item if possible
        if(hasMindStorage && !storedItem.isEmpty() && storedMind != null && canHeldItemAcceptMind(storedMind, null)){
            // HexGloop.logPrint("moving mind into item");
            flayHeldItem(storedMind, null);
            storedMind = null;
            world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(BlockPedestal.MINDFUL, false));
            setChanged();
        }
    }

    public List<ItemEntity> getInputItemEntities() {
        // so we had this grab a bit above the pedestal before, but that sounds like a pain rotated, so this should be fine probably ?
        AABB box = new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1);
        return level.getEntitiesOfClass(ItemEntity.class, box, (ent) -> !(ent.getUUID().equals(persistentUUID)) && EntitySelector.ENTITY_STILL_ALIVE.test(ent) && !((INoMoving)ent).getNoMoving());
    }
        
    // tries to get a pattern based on the scroll or slate in the pedestal if there is one
    @Nullable
    public HexPattern getPattern(){
        // make sure we're on the server and have a pattern holder
        ADIotaHolder iotaHolder = IXplatAbstractions.INSTANCE.findDataHolder(storedItem); // so that we can get stuff like ducks or Pi
        if(!(level instanceof ServerLevel sWorld)
        || storedItem.is(HexGloopTags.NOT_PATTERN_PEDESTAL_PROVIDER)
        || iotaHolder == null) return null; 
        Iota iota = iotaHolder.readIota();
        if(iota instanceof PatternIota pIota){
            return pIota.getPattern();
        }
        return null;
    }

    public Iota getIota(){
        if(!isMirror) return null; // only do embedding stuff on the mirror
        if(!(level instanceof ServerLevel sWorld)) return null;
        ADIotaHolder iotaHolder = IXplatAbstractions.INSTANCE.findDataHolder(storedItem);
        if(iotaHolder != null){
            return iotaHolder.readIota();
        }
        return null;
    }
    
    public int getContainerSize(){
        return 1;
    }

    public boolean isEmpty(){
        return storedItem.isEmpty();
    }

    public ItemStack getItem(int slot){
        if(slot == 0){
            return storedItem;
        }
        return ItemStack.EMPTY;
    }

    public void setItem(int slot, ItemStack stack){
        if(slot == 0){
            storedItem = stack;
            syncItemWithEntity(true);
            setChanged();
        }
    }

    public ItemStack removeItemNoUpdate(int slot){
        if(slot == 0){
            ItemStack temp = storedItem;
            storedItem = ItemStack.EMPTY;
            syncItemWithEntity(true);
            setChanged();
            return temp;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack removeItem(int slot, int amount){
        if(slot == 0){
            ItemStack newSplit = storedItem.split(amount);
            syncItemWithEntity(true);
            setChanged();
            return newSplit;
        }
        return ItemStack.EMPTY;
    }

    public boolean stillValid(Player player){
        return false; // no gui 
    }

    public void clearContent(){
        storedItem = ItemStack.EMPTY;
        syncItemWithEntity(true);
        setChanged();
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

    // flaying stuff:

    // abstracted here so we can call it without casting context and not deal with storing the mind in the pedestal
    public void flayHeldItem(Villager sacrifice, @Nullable CastingEnvironment ctx){
        // this deals with dynamic stuff and recipes
        Level world = getLevel();
        IMindTargetItem mindTarget = ItemFlayingRecipe.getItemTarget(storedItem, sacrifice, world.getRecipeManager());
        if(mindTarget != null){
            ItemStack result = mindTarget.absorbVillagerMind(sacrifice, storedItem, ctx);
            if(result != storedItem){
                if(storedItem.isEmpty()){
                    storedItem = result; // just replace it
                    syncItemWithEntity(true);
                } else {
                    // spawn a new item entity - might be nice to add a bit of velocity to it ?
                    ItemEntity newEnt = new ItemEntity(world, 
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 
                    result, 0, 0, 0);
                    world.addFreshEntity(newEnt);
                }
            }
            return;
        }
    }

    public void absorbVillagerMind(Villager sacrifice, BlockPos flayPos, CastingEnvironment ctx){
    
        if(canHeldItemAcceptMind(sacrifice, ctx)){
            flayHeldItem(sacrifice, ctx);
            return;
        }

        // handle mindstorage
        if(hasMindStorage && storedMind == null){
            Villager clone = new Villager(EntityType.VILLAGER, level);
            clone.setVillagerData(sacrifice.getVillagerData());
            clone.setPosRaw(flayPos.getX(), flayPos.getY(), flayPos.getZ());
            storedMind = clone;
            if(storedMind == null){
                level.setBlockAndUpdate(flayPos, level.getBlockState(flayPos).setValue(BlockPedestal.MINDFUL, false));
            } else {
                level.setBlockAndUpdate(flayPos, level.getBlockState(flayPos).setValue(BlockPedestal.MINDFUL, true));
            }
            setChanged();
        }
    }
    
    public boolean canHeldItemAcceptMind(Villager sacrifice, CastingEnvironment ctx){
        IMindTargetItem mindTarget = ItemFlayingRecipe.getItemTarget(storedItem, sacrifice, getLevel().getRecipeManager());
        if(mindTarget != null){
            return mindTarget.canAcceptMind(sacrifice, storedItem, ctx);
        }
        return false;
    }

    // return true if it can be accepted
    public boolean canAcceptMind(Villager sacrifice, BlockPos flayPos, CastingEnvironment ctx){
        
        if(canHeldItemAcceptMind(sacrifice, ctx)){
            return true;
        }

        if(hasMindStorage){
            return storedMind == null;
        }
        
        return false;
    }
}
