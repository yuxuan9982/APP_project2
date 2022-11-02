package com.example.puzzlegame;

import com.example.puzzlegame.slice.Init_pageSlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(Init_pageSlice.class.getName());
    }
}
