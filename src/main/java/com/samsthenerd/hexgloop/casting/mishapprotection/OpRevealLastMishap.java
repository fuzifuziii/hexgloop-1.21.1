package com.samsthenerd.hexgloop.casting.mishapprotection;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import com.samsthenerd.hexgloop.compat.moreIotas.MoreIotasMaybeIotas;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import dev.architectury.platform.Platform;

public class OpRevealLastMishap implements ConstMediaAction {
   private boolean revealOrString;

   public OpRevealLastMishap(boolean revealOrString) {
      this.revealOrString = revealOrString;
   }

   @Override
   public List<Iota> execute(List<? extends Iota> args, CastingEnvironment ctx){
      Component mishapText = ((IMishapStorage)(Object)ctx).getLastMishap();
      Component wrapperText = Component.translatable("hexgloop.mishap.catch_wrapper" + (mishapText == null ? ".none" : ""), mishapText);
      List<Iota> result = new ArrayList<>();
      if(revealOrString){
         ctx.getCastingEntity().sendSystemMessage(wrapperText);
      } else if(Platform.isModLoaded("moreiotas")){
         result.add(MoreIotasMaybeIotas.makeStringIota(wrapperText.getString()));
      }
      return result;
   }


   public long getMediaCost(){
      return 0;
   }

   public int getArgc(){
      return 0;
   }
}
