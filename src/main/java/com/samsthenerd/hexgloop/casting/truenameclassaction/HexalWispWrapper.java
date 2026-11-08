package com.samsthenerd.hexgloop.casting.truenameclassaction;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

public class HexalWispWrapper {
    public static boolean isWisp(CastingEnvironment ctx){
        // hexal is not a compile dependency (optional), so check by class name
        return ctx.getClass().getName().equals("ram.talia.hexal.api.casting.eval.env.WispCastEnv");
    }
}
