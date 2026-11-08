package com.samsthenerd.hexgloop.mixins.misc;

import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientAdvancements.class)
public interface MixinAccessClientAdvancementProgress {
    @Accessor("progress")
    public Map<net.minecraft.advancements.AdvancementHolder, AdvancementProgress> getAdvancementProgresses();
}
