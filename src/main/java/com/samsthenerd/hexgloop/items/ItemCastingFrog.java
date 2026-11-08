package com.samsthenerd.hexgloop.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.utils.StackNBT;

import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;

public class ItemCastingFrog extends ItemPackagedHex {

    public static final String FROG_VARIANT_KEY = "frog_variant";

    public static final ResourceLocation FROG_VARIANT_PREDICATE = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "frog_variant");

    public ItemCastingFrog(Properties settings){
        super(settings);
    }

    @Override
    public boolean canDrawMediaFromInventory(ItemStack stack){
        return true;
    }

    @Override
    public boolean breakAfterDepletion(){
        return false;
    }

    // for hexxycraft backport
    @Override
    public int cooldown(){
        return 0;
    }

    // don't hand cast
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand) {
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    public List<ItemStack> getEquippedFrogs(LivingEntity entity){
        List<ItemStack> frogs = new ArrayList<>();
        for(List<ItemStack> stacksInSlot : HexGloop.TRINKETY_INSTANCE.getTrinkets(entity).values()){
            for(ItemStack stack : stacksInSlot){
                if(stack != null && stack.getItem().equals(this)){
                    frogs.add(stack);
                }
            }
        }
        return frogs;
    }

    @Override
    public void setMedia(ItemStack stack, long media) {
        HexGloop.logPrint("setting media on frog: " + stack + " to " + media);
        super.setMedia(stack, media);
    }

    @Nullable
    public FrogVariant getFrogVariant(ItemStack stack){
        String frogIdStr = StackNBT.getString(stack, FROG_VARIANT_KEY);
        if(frogIdStr == null){
            return null;
        }
        ResourceLocation frogId = ResourceLocation.parse(frogIdStr);
        return BuiltInRegistries.FROG_VARIANT.get(frogId);
    }

    public void setFrogVariant(ItemStack stack, FrogVariant frogVariant){
        ResourceLocation frogId = BuiltInRegistries.FROG_VARIANT.getKey(frogVariant);
        if(frogId != null){
            StackNBT.putString(stack, FROG_VARIANT_KEY, frogId.toString());
        }
    }

    public void attachVariantIfNeeded(ItemStack stack, Level world, Entity entity){
        if(StackNBT.hasString(stack, FROG_VARIANT_KEY)){
            return;
        }
        FrogVariant frogVariant = getFrogVariantFromWorld(world, entity.blockPosition());
        setFrogVariant(stack, frogVariant);
    }

    public static FrogVariant getFrogVariantFromWorld(Level world, BlockPos pos){
        Holder<Biome> registryEntry = world.getBiome(pos);
        if (registryEntry.is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
            return BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(FrogVariant.COLD).value();
        } else if (registryEntry.is(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)) {
            return BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(FrogVariant.WARM).value();
        } else {
            return BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(FrogVariant.TEMPERATE).value();
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        attachVariantIfNeeded(stack, world, entity);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        attachVariantIfNeeded(stack, world, player);
    }

    public void cast(ItemStack stack, LivingEntity entity){
        HexGloop.logPrint("in frog cast");
        if(!(entity instanceof ServerPlayer sPlayer)) return;
        HexGloop.logPrint("is server player");
        ServerLevel sWorld = sPlayer.serverLevel();

        List<Iota> instrs = getHex(stack, sWorld);
        if (instrs == null) {
            return;
        }
        HexGloop.logPrint("has instrs");
        // Hex 0.12: CastingEnvironment is abstract; packaged hexes cast through PackagedItemCastEnv + CastingVM.empty
        var env = new PackagedItemCastEnv(sPlayer, InteractionHand.MAIN_HAND);
        if((Object)env instanceof IContextHelper ctxHelper){
            ctxHelper.setFrog(stack);
        }
        var harness = CastingVM.empty(env);
        harness.queueExecuteAndWrapIotas(instrs, sWorld);

        // Hex 0.12: PackagedItemCastEnv only accumulates the cast sound via postExecution;
        // ItemPackagedHex.use plays env.getSound() itself afterwards. Do the same here,
        // otherwise frog casts are completely silent.
        var castSound = env.getSound().sound();
        if (castSound != null) {
            var pos = sPlayer.position();
            sWorld.playSound(null, pos.x, pos.y, pos.z, castSound, net.minecraft.sounds.SoundSource.PLAYERS, 1f, 1f);
        }

        sPlayer.awardStat(Stats.ITEM_USED.get(this));

        sPlayer.getCooldowns().addCooldown(this, 5);
    }
}
