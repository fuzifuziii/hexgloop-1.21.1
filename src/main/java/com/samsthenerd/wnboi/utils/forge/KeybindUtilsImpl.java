package com.samsthenerd.wnboi.utils.forge;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.util.Tuple;
import com.samsthenerd.wnboi.utils.KeybindUtils;

public class KeybindUtilsImpl {

    public static List<Tuple<KeyMapping, KeybindUtils.KeybindHandler> > keybindingsToRegister = new ArrayList<>();

    public static void registerKeybind(KeyMapping keyBinding, KeybindUtils.KeybindHandler handler){
        keybindingsToRegister.add(new Tuple<>(keyBinding, handler));
    }
}
