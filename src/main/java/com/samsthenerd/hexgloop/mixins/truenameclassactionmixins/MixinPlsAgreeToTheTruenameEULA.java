package com.samsthenerd.hexgloop.mixins.truenameclassactionmixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.misc.worldData.AgreeTruenameEULAState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName;
import at.petrak.hexcasting.common.casting.actions.rw.OpWrite;

@Mixin(OpWrite.class)
public class MixinPlsAgreeToTheTruenameEULA {
    @WrapOperation(method="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Lat/petrak/hexcasting/api/casting/castables/SpellAction$Result;",
    at=@At(value="INVOKE", target="at/petrak/hexcasting/api/casting/mishaps/MishapOthersName$Companion.getTrueNameMishapFromDatum (Lnet/minecraft/server/level/ServerLevel;Lat/petrak/hexcasting/api/casting/iota/Iota;Lnet/minecraft/world/entity/player/Player;)Lat/petrak/hexcasting/api/casting/mishaps/MishapOthersName;"))
    public MishapOthersName wrapTruenameCheckerForEULA(MishapOthersName.Companion staticCompanion, net.minecraft.server.level.ServerLevel world, Iota iota, Player thisPlayer, Operation<MishapOthersName> original){
        // probably want to add a check for if the player has agreed already or not
        if(iota instanceof EntityIota entIota){
            if(thisPlayer instanceof ServerPlayer sPlayer && entIota.getEntity(sPlayer.serverLevel()) == thisPlayer){
                // do our mishap or whatever
                boolean hasAgreed = AgreeTruenameEULAState.getServerState(sPlayer.getServer()).checkAgreement(sPlayer.getUUID());
                if(!hasAgreed){
                    // should probably add name of the spell to cast and its symbol as arguments here
                    sPlayer.sendSystemMessage(Component.translatable("hexgloop.truename_eula"), false);
                }
            }
        }
        return original.call(staticCompanion, world, iota, thisPlayer);
    }
}
