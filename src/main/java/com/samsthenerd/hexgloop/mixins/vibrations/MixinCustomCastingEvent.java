package com.samsthenerd.hexgloop.mixins.vibrations;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json — CastingVM#executeIotas and ControllerInfo are gone in
// Hex 0.12, so the custom casting GameEvent needs a new hook (nothing references GameEvent.ITEM_INTERACT_FINISH anymore).

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.samsthenerd.hexgloop.misc.HexGloopGameEvents;
import net.minecraft.world.level.gameevent.GameEvent;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;

@Mixin(CastingVM.class)
public class MixinCustomCastingEvent {
    @Redirect(method = "executeIotas(Ljava/util/List;Lnet/minecraft/server/level/ServerLevel;)Lat/petrak/hexcasting/api/spell/casting/ControllerInfo;", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/gameevent/GameEvent;ITEM_INTERACT_FINISH:Lnet/minecraft/world/level/gameevent/GameEvent;", opcode = Opcodes.GETSTATIC))
    private GameEvent injectCustomCastingEvent() {
        return HexGloopGameEvents.CASTING_EVENT.get();
    }
}
