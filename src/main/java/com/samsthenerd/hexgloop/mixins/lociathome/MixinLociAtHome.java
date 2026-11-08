package com.samsthenerd.hexgloop.mixins.lociathome;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json — BlockEntityAbstractImpetus lost stepCircle/performSpellCasting/
// stopCasting/getTickSpeed; the circle execution was rewritten around tickExecution/endExecution.

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociAtHome;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociHandler;
import com.samsthenerd.hexgloop.casting.wehavelociathome.LociRegistration;
import com.samsthenerd.hexgloop.casting.wehavelociathome.LociUtils;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.IIotaProviderLocus;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.ILocusModule;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.IRedirectLocus;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.ISpeedLocus;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;

@Mixin(BlockEntityAbstractImpetus.class)
public class MixinLociAtHome implements ILociHandler{
    @Shadow
    private List<BlockPos> trackedBlocks;
    
    @Shadow
    @Nullable
    private transient Set<BlockPos> knownBlocks;

    @Shadow
    private boolean foundAll;
    
    private boolean shouldExit = false;
    private Set<BlockPos> castedBlocks = new HashSet<BlockPos>();
    // don't break the contract, only put modules in lists corresponding to the correct key
    
    @Shadow
    void stepCircle() {
        throw new AssertionError();
    }
    
    // ILociHandler accessors
    
    private Map<Class<? extends ILocusModule>, List<Tuple<BlockPos, ? extends ILocusModule>>> trackedModuleBlocks = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends ILocusModule> List<Tuple<BlockPos, T>> getTrackedModuleBlocks(Class<T> module){
        if(trackedModuleBlocks.containsKey(module)){
            // cast to list to force it to return the correct generic
            return new ArrayList<Tuple<BlockPos, T>>((List)(trackedModuleBlocks.get(module)));
        }
        return new ArrayList<>();
    }

    public List<BlockPos> getTrackedBlocks(){
        return new ArrayList<BlockPos>(trackedBlocks);
    }
    
    // blocks that the circle has energized over
    public Set<BlockPos> getKnownBlocks(){
        if(knownBlocks == null){
            return new HashSet<BlockPos>();
        }
        return new HashSet<BlockPos>(knownBlocks);
    }
    
    // blocks that the circle has actually gone back and casted over - good for not accessing things it hasn't "gotten to" yet
    public Set<BlockPos> getCastedBlocks(){
        return new HashSet<BlockPos>(castedBlocks);
    }

    // primary hooks

    // track blocks by module
    @WrapOperation(method="stepCircle()V", at=@At(value="INVOKE", target="java/util/List.add (Ljava/lang/Object;)Z"), remap=false)
    private boolean trackNewBlock(List probablyTrackedBlocks, Object probablyBlockPos, Operation<Boolean> original){
        if(probablyBlockPos instanceof BlockPos pos){
            BlockEntityAbstractImpetus impetus = ((BlockEntityAbstractImpetus)(Object)this);
            Level world = impetus.getLevel();
            BlockState state = world.getBlockState(pos);
            ILociAtHome locusBlock = LociRegistration.getLocus(state, pos, world);
            if(locusBlock != null){
                // enter new one
                locusBlock.waveEnter(pos, state, world, impetus);
                
                for(Class<? extends ILocusModule> module : ILociHandler.MODULE_TYPES){
                    if(!module.isInstance(locusBlock)) continue; // verify that it has this module
                    // track it !
                    if(!trackedModuleBlocks.containsKey(module)){
                        trackedModuleBlocks.put(module, new ArrayList<>());
                    }
                    trackedModuleBlocks.get(module).add(new Tuple<BlockPos, ILocusModule>(pos, module.cast(locusBlock)));
                }
            }
            // exit old one
            if(trackedBlocks.size() > 0){
                BlockPos lastBlock = trackedBlocks.get(trackedBlocks.size()-1);
                BlockState lastState = world.getBlockState(lastBlock);
                ILociAtHome lastLocus = LociRegistration.getLocus(lastState, lastBlock, world);
                if(lastLocus != null){
                    lastLocus.waveExit(lastBlock, lastState, world, impetus);
                }
            }
        }
        return original.call(probablyTrackedBlocks, probablyBlockPos);
    }
    
    @WrapOperation(method = "performSpellCasting()V",
    at=@At(value="INVOKE", target="at/petrak/hexcasting/api/block/circle/BlockCircleComponent.getPattern (Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;)Lat/petrak/hexcasting/api/casting/math/HexPattern;"))
    private HexPattern bigFakeGloopyLociWrap(BlockCircleComponent block, BlockPos pos, BlockState bs, Level world, Operation<HexPattern> original, 
        @Local(ordinal=0) CastingVM harness, @Local(ordinal = 0) LocalRef<BlockPos> erroredPos){
        if(shouldExit) return null; // unideal but it'll just skip every new slate
        ILociAtHome locusBlock = LociRegistration.getLocus(bs, pos, world);
        if(locusBlock != null){
            castedBlocks.add(pos);
            locusBlock.rawLociCall(pos, bs, world, harness);
            if(locusBlock.shouldStopCircle()){
                shouldExit = true;
                ((BlockEntityAbstractImpetus)(Object)this).postMishap(locusBlock.getStopCircleError());
                erroredPos.set(pos);
                return null;
            }
            if(locusBlock instanceof IIotaProviderLocus iotaLocus){
                Iota providedIota = iotaLocus.provideIota(pos, bs, world, harness);
                if(providedIota instanceof PatternIota pIota){
                    return pIota.getPattern();
                } else if(providedIota != null){
                    // embed it
                    LociUtils.addOrEmbedIota(harness, providedIota);
                }
            }
        }
        return original.call(block, pos, bs, world);
    }

    @Inject(method = "stopCasting()V", at=@At("HEAD"), remap=false)
    private void resetShouldExit(CallbackInfo ci){
        shouldExit = false;
        castedBlocks = new HashSet<BlockPos>();
        trackedModuleBlocks = new HashMap<>();
        // tell them the circle stopped
        if(knownBlocks != null){
            BlockEntityAbstractImpetus impetus = (BlockEntityAbstractImpetus)(Object)this;
            for(BlockPos pos : knownBlocks){
                Level world = impetus.getLevel();
                BlockState state = world.getBlockState(pos);
                ILociAtHome locus = LociRegistration.getLocus(state, pos, world);
                if(locus != null){
                    locus.circleStopped(pos, state, world, impetus);
                }
            }
        }
    }

    // specific module hooks:

    @WrapOperation(method="stepCircle()V", at=@At(value="INVOKE", target="at/petrak/hexcasting/api/block/circle/BlockCircleComponent.exitDirections (Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;)Ljava/util/EnumSet;"))
    public EnumSet<Direction> checkForForcedNextPos(BlockCircleComponent thisComponent, BlockPos currentPos, 
        BlockState currentState, Level world, Operation<EnumSet<Direction>> original, @Share("pos") LocalRef<BlockPos> forcedPos){
        ILociAtHome locus = LociRegistration.getLocus(currentState, currentPos, world);
        if(locus instanceof IRedirectLocus redirectLocus){
            BlockPos possibleNextPos = redirectLocus.forceNextPos(currentPos, currentState, world, trackedBlocks, knownBlocks);
            if(possibleNextPos != null){
                forcedPos.set(possibleNextPos);
                return EnumSet.noneOf(Direction.class);
            }
        }
        // set it back to null if it was set from last time
        forcedPos.set(null);
        return original.call(thisComponent, currentPos, currentState, world);
    }

    @ModifyVariable(method="stepCircle()V", 
    at=@At(value="STORE"),
    ordinal = 1,
    slice=@Slice(
        from=@At(value="INVOKE", target="at/petrak/hexcasting/api/block/circle/BlockCircleComponent.exitDirections (Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;)Ljava/util/EnumSet;")
    ))
    public BlockPos applyForcedNextPos(BlockPos original, @Share("pos") LocalRef<BlockPos> forcedPos){
        BlockPos forced = forcedPos.get();
        if(forced != null){
            forcedPos.set(null);
            boolean closedLoop = (this.trackedBlocks.size() >= 3 && this.trackedBlocks.get(0).equals(forced));
            foundAll |= closedLoop;
            return forced;
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method="getTickSpeed()I", at=@At("RETURN"), remap=false)
    public int modifyTickSpeed(int original){
        double currentModifier = 1;
        Map<Block, Integer> blockCounts = new HashMap<Block, Integer>();
        int blocksIn = 0;
        int blocksAgo = trackedBlocks != null ? trackedBlocks.size()-1 : 0;
        BlockEntityAbstractImpetus impetus = (BlockEntityAbstractImpetus)(Object)this;
        if(trackedBlocks != null){
            // OK i know i just reworked this to not need to iterate over every block,, but it's the easiest way to get the blocks ago number
            for(BlockPos pos : trackedBlocks){
                BlockState state = impetus.getLevel().getBlockState(pos);
                Block block = state.getBlock();
                if(blockCounts.containsKey(block)){
                    blockCounts.put(block, blockCounts.get(block) + 1);
                } else {
                    blockCounts.put(block, 1);
                }
                blocksIn++;
                blocksAgo--;
                ILociAtHome locus = LociRegistration.getLocus(state, pos, impetus.getLevel());
                if(locus instanceof ISpeedLocus speedLocus){
                    currentModifier *= speedLocus.modifyTickDelay(blocksAgo, currentModifier, original, blockCounts.get(block), trackedBlocks, impetus.getLevel(), impetus);
                    if(currentModifier < 0){
                        return -1; // -1 to mean instant
                    }
                }
            }
        }
        if(currentModifier < 0){
            return -1;
        }
        return (int)Math.round(original * currentModifier);
    }

    
    @WrapOperation(method="stepCircle()V", at=@At(value="INVOKE", target="net/minecraft/world/level/Level.scheduleTick (Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
    public void stepImmediatelyIfNeeded(Level world, BlockPos pos, Block block, int delay, Operation<Void> original){
        if(delay < 0){ // have negative imply that we should tick again immediately
            // HexGloop.logPrint("immediately stepping");
            stepCircle();
        } else {
            original.call(world, pos, block, delay);
        }
    }
}
