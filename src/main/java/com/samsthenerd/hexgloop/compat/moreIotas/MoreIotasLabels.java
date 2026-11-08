package com.samsthenerd.hexgloop.compat.moreIotas;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker.Label;
import com.samsthenerd.hexgloop.misc.wnboi.LabelTypes.TextLabel;

import at.petrak.hexcasting.api.casting.iota.Iota;
import ram.talia.moreiotas.api.casting.iota.StringIota;

public class MoreIotasLabels {
    public static void register(){
        LabelMaker.registerIotaLabelFunction(StringIota.TYPE, MoreIotasLabels::stringToLabel);
    }

    @Nullable
    public static Label stringToLabel(Iota iota){
        if(!(iota instanceof StringIota)) return null;
        StringIota stringIota = (StringIota) iota;
        return new TextLabel(Component.literal(stringIota.getString()));
    }
}
