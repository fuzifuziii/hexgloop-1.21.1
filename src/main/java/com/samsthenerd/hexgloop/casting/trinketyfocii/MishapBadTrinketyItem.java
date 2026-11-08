package com.samsthenerd.hexgloop.casting.trinketyfocii;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.utils.HexUtils;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

// mostly copied from MishapBadOffhandItem
public final class MishapBadTrinketyItem extends Mishap {
    private final ItemStack item;
    private final String slot;
    private final Component wanted;

    public MishapBadTrinketyItem(@NotNull ItemStack item, @NotNull String slot, @NotNull Component wanted) {
      super();
      this.item = item;
      this.slot = slot;
      this.wanted = wanted;
   }

   @NotNull
   public final ItemStack getItem() {
      return this.item;
   }

   @NotNull
   public final String getSlot() {
      return this.slot;
   }

   @NotNull
   public final Component getWanted() {
      return this.wanted;
   }

   @NotNull
   public FrozenPigment accentColor(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx) {
      Intrinsics.checkNotNullParameter(ctx, "ctx");
      Intrinsics.checkNotNullParameter(errorCtx, "errorCtx");
      return this.dyeColor(DyeColor.BROWN);
   }

   public TreeList<Iota> execute(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx, @NotNull TreeList<Iota> stack) {
        return stack;
    }

   @NotNull
   public Component errorMessage(@NotNull CastingEnvironment ctx, @NotNull Mishap.Context errorCtx) {
      Component errorText;
      Object[] errorArgs;
      if (this.item.isEmpty()) {
         errorArgs = new Object[]{this.actionName(errorCtx.getName()), this.wanted, Component.translatable("hexgloop.slotname."+this.slot)};
         errorText = this.error("no_item.slot", errorArgs);
      } else {
         errorArgs = new Object[]{this.actionName(errorCtx.getName()), this.wanted, Component.translatable("hexgloop.slotname."+this.slot), this.item.getCount(), this.item.getDisplayName()};
         errorText = this.error("bad_item.slot", errorArgs);
      }

      return errorText;
   }

   public final static MishapBadTrinketyItem of(@NotNull ItemStack item, @NotNull String slot, @NotNull String stub, @NotNull Object... args) {
      return new MishapBadTrinketyItem(item, slot, (Component)HexUtils.asTranslatedComponent("hexcasting.mishap.bad_item." + stub, Arrays.copyOf(args, args.length)));
   }
}