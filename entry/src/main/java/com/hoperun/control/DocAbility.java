package com.hoperun.control;

import com.hoperun.control.slice.DocAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.IAbilityContinuation;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;


public class DocAbility extends Ability implements IAbilityContinuation {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(DocAbilitySlice.class.getName());
        //虽然一个 Page 可以包含多个 AbilitySlice，但是 Page 进入前台时界面默认只展示一个 AbilitySlice。
        // 默认展示的 AbilitySlice 是通过setMainRoute()方法来指定的。
    }

    @Override
    public boolean onStartContinuation() {
        return true;
    }

    @Override
    public boolean onSaveData(IntentParams intentParams) {
        return true;
    }

    @Override
    public boolean onRestoreData(IntentParams intentParams) {
        return true;
    }

    @Override
    public void onCompleteContinuation(int i) {

    }

    @Override
    public void onRemoteTerminated() {

    }



}
