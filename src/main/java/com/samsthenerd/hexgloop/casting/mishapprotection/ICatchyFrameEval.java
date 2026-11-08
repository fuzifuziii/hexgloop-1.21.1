package com.samsthenerd.hexgloop.casting.mishapprotection;

import at.petrak.hexcasting.api.casting.eval.vm.FrameFinishEval;

public interface ICatchyFrameEval {

    public boolean isCatchy();

    public FrameFinishEval setCatchy(boolean catchy);

    public FrameFinishEval initFromInstance();
}
