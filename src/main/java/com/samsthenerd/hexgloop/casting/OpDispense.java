package com.samsthenerd.hexgloop.casting;

import java.util.List;

import org.jetbrains.annotations.NotNull;


import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.RenderedSpell;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import kotlin.Triple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class OpDispense implements SpellAction {

    public OpDispense(){
    }

    @Override
    public int getArgc(){ return 3;}

    public boolean hasCastingSound(@NotNull CastingEnvironment ctx){
        return false;
    }

    public boolean awardsCastingStat(@NotNull CastingEnvironment ctx){
        return true;
    }





    @Override
    public SpellAction.Result execute(List<? extends Iota> args, CastingEnvironment context){
        // position to dispense from
        BlockPos pos = OperatorUtils.getBlockPos(args, 0, getArgc());
        context.assertPosInRange(pos);
        Vec3 dir = OperatorUtils.getVec3(args, 1, getArgc());
        ItemEntity itemEnt = OperatorUtils.getItemEntity(args, context.getWorld(), 2, getArgc());
        context.assertEntityInRange(itemEnt);

        ItemStack stackToDispense = itemEnt.getItem();

        DispenseItemBehavior behavior = getBehavior(stackToDispense);

        long cost = 0;
        if(behavior != DispenseItemBehavior.NOOP && !stackToDispense.isEmpty()){
            cost = MediaConstants.DUST_UNIT / 2;
            if(behavior instanceof ProjectileDispenseBehavior){
                cost = MediaConstants.DUST_UNIT * 3; // 3 to launch a non arrow projectile
            }
            if(stackToDispense.getItem() instanceof ArrowItem){
                cost = MediaConstants.CRYSTAL_UNIT; // make it cost 1 charged for an arrow
            }
        }

        return new SpellAction.Result(
            new Spell(itemEnt, pos, dir), 
            cost, 
            List.of(), 1L);
    }

    private static final DispenseItemBehavior FALLBACK = new DefaultDispenseItemBehavior();

    // replaces DispenserBlock#getDispenseMethod (protected instance method; DISPENSER_REGISTRY is public)
    private static DispenseItemBehavior getBehavior(ItemStack stack){
        if(stack.isEmpty()) return DispenseItemBehavior.NOOP;
        return DispenserBlock.DISPENSER_REGISTRY.getOrDefault(stack.getItem(), FALLBACK);
    }

    public class Spell implements RenderedSpell{
        private ItemEntity dispenseEnt;
        private BlockPos pos;
        private Vec3 dir;

        public Spell(ItemEntity dispenseEnt, BlockPos pos, Vec3 dir){
            this.dispenseEnt = dispenseEnt;
            this.pos = pos;
            this.dir = dir;
        }

        public void cast(CastingEnvironment ctx){
            ItemStack stackToDispense = dispenseEnt.getItem();

            DispenseItemBehavior behavior = getBehavior(stackToDispense);

            if(behavior != DispenseItemBehavior.NOOP && !stackToDispense.isEmpty()){
                BlockState fakeDispenserState = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.getNearest(dir.x(), dir.y(), dir.z()));
                DispenserBlockEntity be = new DispenserBlockEntity(pos, fakeDispenserState);
                BlockSource bp = new BlockSource(ctx.getWorld(), pos, fakeDispenserState, be);
                dispenseEnt.setItem(behavior.dispense(bp, stackToDispense));
                for(int i = 0; i < be.getContainerSize(); i++){
                    ItemStack leftoverStack = be.getItem(i);
                    if(leftoverStack.isEmpty()){
                        continue;
                    }
                    ItemEntity leftoverItem = new ItemEntity(ctx.getWorld(), pos.getX(), pos.getY(), pos.getZ(), leftoverStack);
                    ctx.getWorld().addFreshEntity(leftoverItem);
                }
                
            }
        }
    }

}

