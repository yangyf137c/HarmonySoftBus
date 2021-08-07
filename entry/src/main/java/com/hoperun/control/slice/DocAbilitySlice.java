package com.hoperun.control.slice;

import com.hoperun.control.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityContinuation;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.agp.window.dialog.ToastDialog;
import ohos.miscservices.pasteboard.PasteData;
import ohos.miscservices.pasteboard.SystemPasteboard;

public class DocAbilitySlice  extends AbilitySlice implements IAbilityContinuation {

    private static final String TAG = DocAbilitySlice.class.getName();
    private TextField textField;
    private Button pasteBtn;
    private SystemPasteboard pasteboard;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_typingboard);

        initViews();
        initListener();
    }

    private void initViews() {
        pasteboard= SystemPasteboard.getSystemPasteboard(getContext());

        textField = (TextField) findComponentById(ResourceTable.Id_remote_input);
        pasteBtn=(Button) findComponentById(ResourceTable.Id_get_paste);
    }

    private void initListener() {
        pasteBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {

            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }


    @Override
    public boolean onStartContinuation() {
        //Page 请求迁移后，系统首先回调此方法，开发者可以在此回调中决策当前是否可以执行迁移，比如，弹框让用户确认是否开始迁移。
        return true;
    }

    @Override
    public boolean onSaveData(IntentParams intentParams) {
        //如果 onStartContinuation() 返回 true ，则系统回调此方法，开发者在此回调中保存必须传递到另外设备上以便恢复 Page 状态的数据。
        intentParams.setParam("pasteData", pasteboard.getPasteData());
        return true;
    }

    @Override
    public boolean onRestoreData(IntentParams intentParams) {
        //源侧设备上 Page 完成保存数据后，系统在目标侧设备上回调此方法，开发者在此回调中接受用于恢复 Page 状态的数据。
        // 注意，在目标侧设备上的 Page 会重新启动其生命周期，无论其启动模式如何配置。且系统回调此方法的时机在 onStart() 之前。

        //这里内部嵌入了一个线程，要更新UI主线程中的组件上的值，要先拿到UI线程，然后投递任务Runnable过去
        //在任务Runnable中，向UI组件写值
        getUITaskDispatcher().asyncDispatch(new Runnable() {
            @Override
            public void run() {
                pasteboard.setPasteData(PasteData.creatPlainTextData(intentParams.getParam("pasteData").toString()));
            }
        });
        return true;
    }

    @Override
    public void onCompleteContinuation(int i) {
        //目标侧设备上恢复数据一旦完成，系统就会在源侧设备上回调 Page 的此方法，以便通知应用迁移流程已结束。
        // 开发者可以在此检查迁移结果是否成功，并在此处理迁移结束的动作，例如，应用可以在迁移完成后终止自身生命周期。
        new ToastDialog(DocAbilitySlice.this).setText("迁移成功").show();
    }
}
