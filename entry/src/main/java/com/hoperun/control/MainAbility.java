package com.hoperun.control;

import com.hoperun.control.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.window.service.WindowManager;
import ohos.bundle.IBundleManager;

import static ohos.security.SystemPermission.DISTRIBUTED_DATASYNC;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        // 禁止软件盘弹出
        getWindow().setLayoutFlags(WindowManager.LayoutConfig.MARK_ALT_FOCUSABLE_IM,
                WindowManager.LayoutConfig.MARK_ALT_FOCUSABLE_IM);

        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
        //虽然一个 Page 可以包含多个 AbilitySlice，但是 Page 进入前台时界面默认只展示一个 AbilitySlice。
        // 默认展示的 AbilitySlice 是通过setMainRoute()方法来指定的。

        if (verifySelfPermission(DISTRIBUTED_DATASYNC) != IBundleManager.PERMISSION_GRANTED) {
            // 没有权限
            if (canRequestPermission(DISTRIBUTED_DATASYNC)) {
                // 弹框
                requestPermissionsFromUser(
                        new String[]{DISTRIBUTED_DATASYNC}, 0);
            }
        }
    }
}
