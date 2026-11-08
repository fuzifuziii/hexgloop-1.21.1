package com.samsthenerd.wnboi.utils;


import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import com.samsthenerd.wnboi.interfaces.KeyboundItem;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class KeybindUtils {
    public static final KeyMapping DEFAULT_KEYBINDING = new KeyMapping("key.wnboi.default_context",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.wnboi");

    public static final void handleKeyboundItems(KeyMapping keyBinding, Minecraft client){
        if(!keyBinding.isDown()) return; // don't run if key isn't pressed
        Player player = client.player;
        if(player == null){
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        KeyboundItem mainItem = null;
        KeyboundItem offItem = null;

        if(mainHand.getItem() instanceof KeyboundItem){
            mainItem = (KeyboundItem)mainHand.getItem();
        }
        if(offHand.getItem() instanceof KeyboundItem){
            offItem = (KeyboundItem)offHand.getItem();
        }

        if(client.screen != null){
            // there's some other screen open, probably shouldn't open ours
            return;
        } else {
            // no screen open - check if we should open one
            if(mainItem != null && mainItem.getKeyBinding() == keyBinding){
                mainItem.openScreen();
                return;
            }
            if(offItem != null && offItem.getKeyBinding() == keyBinding){
                offItem.openScreen();
                return;
            }
        }
    }

    public static void registerKeybinds(){
        registerKeybind(DEFAULT_KEYBINDING, (keyBinding, client) -> handleKeyboundItems(keyBinding, client));
    }

    // vendored wnboi (1.21 port): replaces the old @ExpectPlatform fabric/forge impls with architectury
    public static void registerKeybind(KeyMapping keyBinding, KeybindHandler handler){
        KeyMappingRegistry.register(keyBinding);
        ClientTickEvent.CLIENT_POST.register(client -> handler.run(keyBinding, client));
    }

    @FunctionalInterface
    public interface KeybindHandler{
        void run(KeyMapping keyBinding, Minecraft client);
    }
}
