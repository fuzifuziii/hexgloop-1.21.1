package com.samsthenerd.hexgloop.casting.mishapprotection;

import net.minecraft.network.chat.Component;

public interface IMishapStorage {
    public Component getLastMishap();

    public void setLastMishap(Component mishap);
}
