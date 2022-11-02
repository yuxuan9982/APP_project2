package com.example.puzzlegame;

import com.example.puzzlegame.slice.DynamicPageSlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class DynamicPage extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(DynamicPageSlice.class.getName());
    }
}
