package com.samsthenerd.hexgloop.compat.moreIotas;

import com.samsthenerd.hexgloop.HexGloop;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import ram.talia.moreiotas.api.casting.iota.StringIota;

public class MoreIotasMaybeIotas {
    public static Iota makeStringIota(String sIn){
        try{
            return StringIota.make(sIn);
        } catch (Mishap e){
            // idk why it would do this but whatever
            HexGloop.LOGGER.error("MoreIotas threw a mishap when making a StringIota: ", e);
            return new NullIota();
        }
    }
}
