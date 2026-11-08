package com.samsthenerd.hexgloop.utils;

import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.samsthenerd.hexgloop.mixins.misc.MixinAccessClientAdvancementProgress;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientUtils {
    public static Player makeOtherClientPlayer(UUID uuid){
        return new RemotePlayer(Minecraft.getInstance().level,
                        new GameProfile(uuid, ""));
    }

    public static Level getClientWorld(){
        return Minecraft.getInstance().level;
    }
    public static boolean shouldShowReflected(){
        return hasLens(Minecraft.getInstance().player);
    }

    // TODO(1.21 port): DiscoveryHandlers.hasLens is gone; check main hand/offhand for the scrying lens
    private static boolean hasLens(Player player){
        if(player == null) return false;
        return player.getMainHandItem().is(HexItems.SCRYING_LENS.get()) || player.getOffhandItem().is(HexItems.SCRYING_LENS.get());
    }

    // doesn't account for enlightenment modifiers, rip i guess
    public static boolean isEnlightened(){
        ClientAdvancements manager = Minecraft.getInstance().getConnection().getAdvancements();
        if(manager instanceof MixinAccessClientAdvancementProgress progressHolder){
            Map<AdvancementHolder, AdvancementProgress> progresses = progressHolder.getAdvancementProgresses();
            AdvancementHolder enlightenment = manager.get(HexAPI.modLoc("enlightenment"));
            if(progresses.containsKey(enlightenment)){
                return progresses.get(enlightenment).isDone();
            }
        }
        return false;
    }

    public static float getClientTime(){
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false) + Minecraft.getInstance().player.level().getGameTime();
    }
}
