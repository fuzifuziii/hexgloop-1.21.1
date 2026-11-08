package com.samsthenerd.hexgloop.mixins.wnboi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.keybinds.HexGloopKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.client.book.gui.GuiBook;

@Mixin(Screen.class)
public class MixinWheelInCasting {

    private static GuiSpellcasting storedHexScreen;
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    public void checkForIotaWheelKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir){
        if(((Object) this) instanceof GuiSpellcasting castingScreen){
            // HexGloop.logPrint("MixinWheelInCasting: Casting screen detected");
            if(HexGloopKeybinds.IOTA_WHEEL_KEYBIND.matches(keyCode, scanCode)){
                // HexGloop.logPrint("MixinWheelInCasting: Iota wheel key pressed");
                HexGloopKeybinds.handleIotaWheelItems(HexGloopKeybinds.IOTA_WHEEL_KEYBIND, Minecraft.getInstance(), true);
            }

            // gonna also deal with the scroll keybinds in here too
            if(HexGloopKeybinds.HEX_SCROLL_UP.matches(keyCode, scanCode)){
                HexGloopKeybinds.handleScrollKey(HexGloopKeybinds.HEX_SCROLL_UP, Minecraft.getInstance(), true, true);
            }
            if(HexGloopKeybinds.HEX_SCROLL_DOWN.matches(keyCode, scanCode)){
                HexGloopKeybinds.handleScrollKey(HexGloopKeybinds.HEX_SCROLL_UP, Minecraft.getInstance(), false, true);
            }

            // yeah why not throw in f keybind too
            Minecraft client = Minecraft.getInstance();
            if(client.options.keySwapOffhand.matches(keyCode, scanCode)) {
                if (!client.player.isSpectator())
                    client.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            }

            // maybe should separate this out to a new mixin but whatever, we're gonna do the book in gui stuff here too
            if(HexGloopKeybinds.OPEN_HEX_BOOK.matches(keyCode, scanCode)){
                storedHexScreen = castingScreen;
                PatchouliAPI.get().openBookGUI(ResourceLocation.parse("hexcasting:thehexbook"));
            }
        }
    }

    // 1.21: Screen#close() no longer exists — ESC goes through onClose().
    @Inject(method="onClose()V", at=@At("HEAD"), cancellable = true)
    public void goBackToHexBook(CallbackInfo ci){
        if(((Object)this) instanceof GuiBook bookScreen && storedHexScreen != null){
            Minecraft.getInstance().setScreen(storedHexScreen);
            storedHexScreen = null;
            ci.cancel();
        }
    }
}
