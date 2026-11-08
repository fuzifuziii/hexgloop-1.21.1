package com.samsthenerd.hexgloop.items;

import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.samsthenerd.hexgloop.casting.mirror.IPlayerPTUContext;

// idk probably won't use it for anything else but if i do it'll be nice that it's separate
// makes all the use functions into wrappers that call the use function of some stored item
/**
 * NOTE: a lot of this stuff functioning properly relies on some mixins, so make sure you yoink those too if you want to use this
 * (you here likely being future me, but if you aren't future me you should do that too !)
 * 
 * The mixins generally work by wrapping some original function call and swapping the hand out for the stored item before calling the original 
 * and then storing the result and swapping the original hand stack back in. 
 * There might be *slight* duplication issues here if something gets both the original stored item and the hand copy in the span of the wrapped method
 * 
 * anyways it should all be in mixins/mirroritems, it's mostly for entities and using tools
 */
public abstract class ItemAbstractPassThrough extends Item{
    public ItemAbstractPassThrough(Properties settings){
        super(settings);
    }

    public abstract ItemStack getStoredItem(ItemStack stack, LivingEntity ent, Level world, InteractionHand hand);

    // so we can 'put it back' in whatever way makes sense for the item we're using
    // returns the itemstack that should be in the player's hand after the use, just if you need to modify it or something
    public abstract ItemStack setStoredItem(ItemStack stack, LivingEntity ent, Level world, InteractionHand hand, ItemStack storedItem);

    // stack is really provided here as a curtesy, it probably shouldn't depend on the stack ?
    public abstract boolean shouldPassTools(ItemStack stack);

    // some methods just don't give us much to work with, support this if it makes sense for the item or just return null if it doesn't
    public abstract ItemStack getStoredItemCopy(ItemStack stack);

    // may be wise to merge some of this code, there's a lot of copy-pasting but with little annoying core changes

    // not super sure what happens with the itemstack in the result here ?
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        // HexGloop.logPrint("~entered use~");
        SimplePTUContext<InteractionResultHolder<ItemStack>> useContext = new SimplePTUContext<>(world, user, hand, this, (ctx)->{
            if(user.getCooldowns().isOnCooldown(ctx.storedItemRef.getItem())){
                // hopefully it no-ops fine ?
                return new Tuple<>(InteractionResultHolder.pass(ctx.originalHandStackRef), ctx.storedItemRef);
            }
            InteractionResultHolder<ItemStack> result = ctx.storedItemRef.use(world, user, hand);
            ItemStack newStackToStore = result.getObject();
            if(newStackToStore != ctx.storedItemRef) newStackToStore = newStackToStore.copy(); // copy incase it's somehow getting cleared elsewhere or something ?
            ctx.storedItemRef = newStackToStore;
            return new Tuple<>(result, newStackToStore);
        });
        InteractionResultHolder<ItemStack> result = useContext.call();
        return useContext.didSucceed ? new InteractionResultHolder<ItemStack>(result.getResult(), user.getItemInHand(hand)) : super.use(world, user, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // HexGloop.logPrint("~entered use on block~");
        // lol, a bit confusing with context vs ctx, oops
        SimplePTUContext<InteractionResult> useContext = new SimplePTUContext<>(context.getLevel(), context.getPlayer(), context.getHand(), this, (ctx)->{
            // have to recreate it since the getter is protected on ItemUsageContext
            BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
            // need a new one so it uses the storedItem stack
            UseOnContext newContext = new UseOnContext(context.getLevel(), context.getPlayer(), context.getHand(), ctx.storedItemRef, hitResult);
            InteractionResult result = ctx.storedItemRef.useOn(newContext);
            return new Tuple<>(result, PTU_YIELD_CHECK_MARKER);
        });
        InteractionResult result = useContext.call();
        return useContext.didSucceed ? result : super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand){
        // HexGloop.logPrint("~entered use on entity~");
        SimplePTUContext<InteractionResult> useContext = new SimplePTUContext<>(user.level(), user, hand, this, (ctx)->{
            InteractionResult result = ctx.storedItemRef.interactLivingEntity(user, entity, hand);
            return new Tuple<>(result, PTU_YIELD_CHECK_MARKER);
        });
        InteractionResult result = useContext.call();
        return useContext.didSucceed ? result : super.interactLivingEntity(stack, user, entity, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        // HexGloop.logPrint("~entered finish using~");
        InteractionHand hand = user.getUsedItemHand();
        SimplePTUContext<ItemStack> useContext = new SimplePTUContext<>(world, user, hand, this, (ctx)->{
            ItemStack modifiedStoredStack = ctx.storedItemRef.getItem().finishUsingItem(ctx.storedItemRef, world, user);
            return new Tuple<>(modifiedStoredStack, modifiedStoredStack);
        });
        useContext.call();
        return useContext.didSucceed ? user.getItemInHand(hand) : super.finishUsingItem(stack, world, user);
    }

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        // HexGloop.logPrint("~entered stopped using");
        InteractionHand hand = user.getUsedItemHand();
        SimplePTUContext<Void> useContext = new SimplePTUContext<>(world, user, hand, this, (ctx)->{
            ctx.storedItemRef.getItem().releaseUsing(ctx.storedItemRef, world, user, remainingUseTicks);
            return new Tuple<>(null, PTU_YIELD_CHECK_MARKER);
        });
        useContext.call();
	}

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        // HexGloop.logPrint("~entered usage tick");
        InteractionHand hand = user.getUsedItemHand();
        SimplePTUContext<Void> useContext = new SimplePTUContext<>(world, user, hand, this, (ctx)->{
            ctx.storedItemRef.getItem().onUseTick(world, user, ctx.storedItemRef, remainingUseTicks);
            return new Tuple<>(null, PTU_YIELD_CHECK_MARKER);
        });
        useContext.call();
	}

    // stuff below here doesn't require swapping hand stacks

    // stuff for tools
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        ItemStack storedItem = getStoredItemCopy(stack);
        if(storedItem != null && !(storedItem.isEmpty() && !(storedItem.getItem() instanceof ItemAbstractPassThrough))){
            return storedItem.getItem().getDestroySpeed(storedItem, state);
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ItemStack storedItem = getStoredItem(stack, attacker, attacker.level(), attacker.getUsedItemHand());
        if(storedItem != null){
            storedItem.getItem().hurtEnemy(storedItem, target, attacker);
            // storedItem.damage(2, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClientSide && state.getDestroySpeed(world, pos) != 0.0f) {
            ItemStack storedItem = getStoredItem(stack, miner, world, miner.getUsedItemHand());
            if(storedItem != null){
                storedItem.getItem().mineBlock(storedItem, world, state, pos, miner);
                // storedItem.damage(1, miner, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            }
        }
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        ItemStack storedItem = getStoredItemCopy(miner.getItemInHand(InteractionHand.MAIN_HAND));
        if(storedItem != null && !storedItem.isEmpty() && !(storedItem.getItem() instanceof ItemAbstractPassThrough)){
            return storedItem.getItem().canAttackBlock(state, world, pos, miner);
        }
        return super.canAttackBlock(state, world, pos, miner);
	}

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ItemStack storedItem = getStoredItemCopy(stack);
        if(storedItem != null && !storedItem.isEmpty() && !(storedItem.getItem() instanceof ItemAbstractPassThrough)){
            return storedItem.getItem().getUseAnimation(storedItem);
        }
        return super.getUseAnimation(stack);
	}

    @Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStack storedItem = getStoredItemCopy(stack);
        if(storedItem != null && !storedItem.isEmpty() && !(storedItem.getItem() instanceof ItemAbstractPassThrough)){
            return storedItem.getItem().getUseDuration(storedItem, entity);
        }
        return super.getUseDuration(stack, entity);
	}

    @Override
    public boolean useOnRelease(ItemStack stack) {
        ItemStack storedItem = getStoredItemCopy(stack);
        if(storedItem != null && !storedItem.isEmpty() && !(storedItem.getItem() instanceof ItemAbstractPassThrough)){
            return storedItem.getItem().useOnRelease(storedItem);
        }
        return super.useOnRelease(stack);
    }

    // hopefully works fine with deferred registry,,
    // anyways, pass this as a result of the use function to indicate that the stored item should be retrieved from wherever the 
    // pt item got off to. *usually* this will just be the hand,, but that's not guaranteed
    public static final ItemStack PTU_YIELD_CHECK_MARKER = new ItemStack(Items.BRICK);
    static {
        StackNBT.putBoolean(PTU_YIELD_CHECK_MARKER, "ptu_yield_check_marker", true);
    }

    public static boolean isMarker(ItemStack stack){
        return stack.is(PTU_YIELD_CHECK_MARKER.getItem()) && StackNBT.getBoolean(stack, "ptu_yield_check_marker");
    }

    // an abstracting class for the pass through use functions.
    // perhaps unnecessarily complicated but should be worth it if I ever want to add/change any behaviors - which I probably will be doing soon,,
    // ok,, i forgot how lambdas work and so the args stuff probably shouldn't be used in most cases, but i'm leaving it incase it is
    public static class PassThroughUseContext<A, R> {
        // just kinda general stuff for function calls - should maybe have these be getters but nah 
        public Level world;
        public LivingEntity user;
        public InteractionHand hand;
        public ItemAbstractPassThrough passItem;
        public A args;
        public ItemStack originalHandStackRef;
        public ItemStack storedItemRef; // mostly for our functions to use
        public boolean didSucceed = false;
        public int ptSlot = -1; // since we have stuff that can swap the hotbar slot, this will make sure we put it back in the right spot

        // let A be vague since different use calls take very different things
        // returns a pair with the 
        public BiFunction<PassThroughUseContext<A,R>, A, Tuple<R, ItemStack>> useWrapper;

        @Nullable // if it's null then probably just call the super
        public R call(){
            ItemStack handStack = user.getItemInHand(hand); // important since we'll need to restore it
            originalHandStackRef = handStack; // just so we have it
            ItemStack storedItem = passItem.getStoredItem(handStack, user, world, hand);
            String sideString = world.isClientSide ? "[client] " : "[server] ";
            if(storedItem != null) storedItem = storedItem.copy();
            if(storedItem != null && !(storedItem.getItem() instanceof ItemAbstractPassThrough)){ // no loops!
                if(user instanceof ServerPlayer sPlayer){
                    ptSlot = hand == InteractionHand.MAIN_HAND ? sPlayer.getInventory().selected : Inventory.SLOT_OFFHAND;
                    ((IPlayerPTUContext)(Object)sPlayer).setPTUContext(this);
                }
                // HexGloop.logPrint(sideString + "swapping stored item into hand: " + storedItem.toString());
                user.setItemInHand(hand, storedItem); // hopefully works fine sided ?
                storedItemRef = storedItem; // so that it can be accessed neatly from the wrapper
                Tuple<R, ItemStack> result = useWrapper.apply(this, args);
                storedItem = result.getB();
                // HexGloop.logPrint(sideString + "result is: " + storedItem.toString());
                if(isMarker(storedItem)){
                    storedItem = user.getItemInHand(hand); // for non-players
                    // HexGloop.logPrint(sideString + "determined it is a marker, found item in hand: " + storedItem.toString());
                    if(user instanceof ServerPlayer sPlayer){
                        Inventory inv = sPlayer.getInventory();
                        storedItem = inv.getItem(ptSlot);
                        // HexGloop.logPrint(sideString + "used by player, found item in ptSlot[" + ptSlot + "]: " + storedItem.toString());
                    }
                }
                // HexGloop.logPrint(sideString + "using stored item: " + storedItem.toString());
                handStack = passItem.setStoredItem(handStack, user, world, hand, storedItem);
                // put the hand stack into wherever it got off to, or if it's not a player just assume it's still in the hand
                // HexGloop.logPrint(sideString + "got handstack back: " + handStack.toString());
                if(user instanceof ServerPlayer sPlayer){
                    Inventory inv = sPlayer.getInventory();
                    // HexGloop.logPrint(sideString + "putting handstack back into inventory at slot " + ptSlot + ": " + handStack.toString());
                    inv.setItem(ptSlot, handStack);
                    ((IPlayerPTUContext)(Object)sPlayer).clearPTUContext();
                } else {
                    // HexGloop.logPrint(sideString + "putting handstack back into hand: " + handStack.toString());
                    user.setItemInHand(hand, handStack);
                }
                didSucceed = true;
                return result.getA();
            }
            return null;
        }

        public PassThroughUseContext(Level world, LivingEntity user, InteractionHand hand, ItemAbstractPassThrough passItem, BiFunction<PassThroughUseContext<A,R>, A, Tuple<R, ItemStack>> useWrapper, A args){
            this.world = world;
            this.user = user;
            this.hand = hand;
            this.passItem = passItem;
            this.args = args;
            this.useWrapper = useWrapper;
        }
    }

    public static class SimplePTUContext<R> extends PassThroughUseContext<Void, R> {
        public SimplePTUContext(Level world, LivingEntity user, InteractionHand hand, ItemAbstractPassThrough passItem, Function<PassThroughUseContext<Void, R>, Tuple<R, ItemStack>> useWrapper){
            super(world, user, hand, passItem, (ctx, args) -> useWrapper.apply(ctx), null);
        }
    }
}
