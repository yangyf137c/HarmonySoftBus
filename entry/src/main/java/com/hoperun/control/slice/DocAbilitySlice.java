package com.hoperun.control.slice;

import com.hoperun.control.ResourceTable;
import com.hoperun.control.constants.EventConstants;
import com.hoperun.control.proxy.ConnectManager;
import com.hoperun.control.proxy.ConnectManagerIml;
import com.hoperun.control.utils.LogUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityContinuation;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Switch;
import ohos.agp.components.TextField;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.event.commonevent.*;
import ohos.miscservices.pasteboard.IPasteDataChangedListener;
import ohos.miscservices.pasteboard.PasteData;
import ohos.miscservices.pasteboard.SystemPasteboard;
import ohos.rpc.RemoteException;

import java.util.HashMap;
import java.util.Map;

public class DocAbilitySlice extends AbilitySlice implements IAbilityContinuation {

    private static final String TAG = DocAbilitySlice.class.getName();
    private TextField textField;
    private ConnectManager connectManager;
    private String deviceIdConn;
    private static final int INIT_SIZE = 8;
    private Button migrateBtn,reverseBtn;
    private Switch docSwitch,pasteSwitch;
    private SystemPasteboard pasteboard;
    private DocAbilitySlice.MyCommonEventSubscriber subscriber;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_typingboard);

        initViews();
        initListener();
        subscribe();

        // 获取想被控制的设备ID
        deviceIdConn = intent.getStringParam("localDeviceId");
        // 连接被控制的设备PA
        initConnManager(deviceIdConn,ConnectManagerIml.DocType);
    }

    private void initConnManager(String deviceId,int type) {
        connectManager = new ConnectManagerIml(type);
        connectManager.connectPa(this, deviceId);
    }

    private void initViews() {
        pasteboard= SystemPasteboard.getSystemPasteboard(getContext());

        textField = (TextField)findComponentById(ResourceTable.Id_remote_input);
        migrateBtn =(Button)findComponentById(ResourceTable.Id_migrate);
        reverseBtn=(Button)findComponentById(ResourceTable.Id_reverse);
        pasteSwitch=(Switch)findComponentById(ResourceTable.Id_pasteSwitch);
        docSwitch=(Switch)findComponentById(ResourceTable.Id_docSwitch);
    }

    private void initListener() {
        // 监听文本变化，远程显示
        textField.addTextObserver((ss, ii, i1, i2) -> {
            Map<String, String> map = new HashMap<>(INIT_SIZE);
            map.put("inputString", ss);
            if (connectManager != null && docSwitch.isSelected()) {
                connectManager.sendRequest(ConnectManagerIml.REQUEST_SEND_DATA, map);
            }
        });

        IPasteDataChangedListener listener = new IPasteDataChangedListener() {
            //监听剪切板内容变化，发送给另一端
            @Override
            public void onChanged() {
                PasteData pasteData = pasteboard.getPasteData();
                if (pasteData == null) {
                    return;
                }
                // Operations to handle data change on the system pasteboard
                if (connectManager != null && pasteSwitch.isSelected()) {
                    Map<String, String> map = new HashMap<>(INIT_SIZE);
                    map.put("pasteContent", getPasteContent());
                    connectManager.sendRequest(ConnectManagerIml.REQUEST_PASTE_CONTENT, map);
                }
            }
        };

        migrateBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                if (deviceIdConn != null) {
                    continueAbilityReversibly(deviceIdConn);
                    //迁移
                }
//                String result = (String)connectManager.sendRequest(ConnectManagerIml.REQUEST_SEND_DATA, (Integer)1);
//                if(result==null)
//                    new ToastDialog(DocAbilitySlice.this).setAlignment(LayoutAlignment.CENTER).setText("其他设备剪贴板为空").show();
//                else
//                    pasteboard.setPasteData(PasteData.creatPlainTextData(result));
            }
        });

        reverseBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                try {
                    reverseContinueAbility();
                    //回迁
                } catch (Exception e) {
                }
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

    private String getPasteContent()
    {
        //获取剪切板的内容
        String paste="";
        PasteData pasteData = pasteboard.getPasteData();
        PasteData.DataProperty dataProperty = pasteData.getProperty();
        boolean hasHtml = dataProperty.hasMimeType(PasteData.MIMETYPE_TEXT_HTML);
        boolean hasText = dataProperty.hasMimeType(PasteData.MIMETYPE_TEXT_PLAIN);
        if (hasHtml || hasText) {
            for (int i = 0; i < pasteData.getRecordCount(); i++) {
                PasteData.Record record = pasteData.getRecordAt(i);
                String mimeType = record.getMimeType();
                if (mimeType.equals(PasteData.MIMETYPE_TEXT_HTML)) {
                    paste=record.getHtmlText();
                    break;
                } else if (mimeType.equals(PasteData.MIMETYPE_TEXT_PLAIN)) {
                    paste=record.getPlainText().toString();
                    break;
                } else {
                    // skip records of other Mime type
                }
            }
        }
        return paste;
    }

    @Override
    public boolean onSaveData(IntentParams intentParams) {
        //如果 onStartContinuation() 返回 true ，则系统回调此方法，开发者在此回调中保存必须传递到另外设备上以便恢复 Page 状态的数据。
        intentParams.setParam("pasteContent", getPasteContent());
        intentParams.setParam("input", textField.getText());
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
                String pasteData=(String)intentParams.getParam("pasteContent");
                pasteboard.setPasteData(PasteData.creatPlainTextData(pasteData));
                textField.setText(intentParams.getParam("input").toString());
                new ToastDialog(DocAbilitySlice.this).setAlignment(LayoutAlignment.CENTER).setText(pasteData).show();
            }
        });
        return true;
    }

    private void unSubscribe() {
        try {
            CommonEventManager.unsubscribeCommonEvent(subscriber);
            //退订所有事件
        } catch (RemoteException e) {
            LogUtils.error(TAG, "unSubscribe Exception");
        }
    }

    @Override
    public void onCompleteContinuation(int i) {
        //目标侧设备上恢复数据一旦完成，系统就会在源侧设备上回调 Page 的此方法，以便通知应用迁移流程已结束。
        // 开发者可以在此检查迁移结果是否成功，并在此处理迁移结束的动作，例如，应用可以在迁移完成后终止自身生命周期。
        new ToastDialog(DocAbilitySlice.this).setText("粘贴板迁移成功").show();
    }

    private void subscribe() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(EventConstants.PASTEBOARD_MIGRATE_EVENT);
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_SCREEN_ON);
        CommonEventSubscribeInfo subscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        subscriber = new DocAbilitySlice.MyCommonEventSubscriber(subscribeInfo);
        try {
            CommonEventManager.subscribeCommonEvent(subscriber);
            //订阅事件
        } catch (RemoteException e) {
            LogUtils.error("", "subscribeCommonEvent occur exception.");
        }
    }

    /**
     * 公共事件订阅处理
     *
     * @since 2020-12-03
     */
    class MyCommonEventSubscriber extends CommonEventSubscriber {
        MyCommonEventSubscriber(CommonEventSubscribeInfo info) {
            super(info);
        }

        @Override
        public void onReceiveEvent(CommonEventData commonEventData) {
            //由开发者实现, 在接收到公共事件时被调用。
            Intent intent = commonEventData.getIntent();
            //CommonEventData 封装公共事件相关信息。用于在发布、 分发和接收时处理数据。
            int requestType = intent.getIntParam("requestType", 0);
            if (requestType == ConnectManagerIml.REQUEST_PASTE_CONTENT) {
                String pasteData=intent.getStringParam("pasteContent");
                pasteboard.setPasteData(PasteData.creatPlainTextData(pasteData));
                new ToastDialog(DocAbilitySlice.this).setAlignment(LayoutAlignment.CENTER).setText("收到剪贴板数据"+pasteData).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribe();
    }
}
