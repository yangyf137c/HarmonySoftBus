package com.hoperun.control;

import com.hoperun.control.constants.EventConstants;
import com.hoperun.control.utils.LogUtils;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.rpc.*;

import static com.hoperun.control.proxy.ConnectManagerIml.*;

public class RemoteService extends Ability {
    public static final int ERR_OK = 0;
    private static final String TAG = RemoteService.class.getSimpleName();
    private MyRemote remote = new MyRemote();

    @Override
    public void onStart(Intent intent) {
        LogUtils.info(TAG, "RemoteService::onStart");
        super.onStart(intent);
    }

    @Override
    public void onBackground() {
        super.onBackground();
        LogUtils.info(TAG, "RemoteService::onBackground");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.info(TAG, "RemoteService::onStop");
    }

    @Override
    public void onCommand(Intent intent, boolean isRestart, int startId) {
    }

    @Override
    protected IRemoteObject onConnect(Intent intent) {
        super.onConnect(intent);
        return remote.asObject();
    }

    @Override
    public void onDisconnect(Intent intent) {
        LogUtils.info(TAG, "RemoteService::onDisconnect");
    }

    /**
     * 远端请求处理
     *
     * @since 2021-02-25
     */
    public class MyRemote extends RemoteObject implements IRemoteBroker {
        private MyRemote() {
            super("===MyService_Remote");
        }

        @Override
        public IRemoteObject asObject() {
            return this;
        }

        @Override
        public boolean onRemoteRequest(int code, MessageParcel data, MessageParcel reply, MessageOption option) {
            //设置接收请求的条目。
//            code	表示对端发送的服务请求码。
//            data	表示对端发送的MessageParcel 对象。
//            reply	表示远程服务发送的响应消息对象。 本地服务将响应数据写入 MessageParcel 对象。
//            option	指示操作是同步的还是异步的。
            LogUtils.info(TAG, "===onRemoteRequest......");
            int requestType = data.readInt();
            String inputString = "";
            if (code == REQUEST_SEND_DATA) {
                inputString = data.readString();
                publishInput(requestType, inputString);
            } else if (code == REQUEST_PLUS) {
                int a = data.readInt();
                int b = data.readInt();
                reply.writeInt(ERR_OK);
                reply.writeInt(a + b);
                publishPlusResult(requestType, String.valueOf(a + b));
                //在这里发布了一个公共订阅事件
            } else if(code == REQUEST_PASTE_CONTENT){
                String s=data.readString();
                reply.writeInt(ERR_OK);
                publishPaste(requestType,s);
            }
            return true;
        }
    }

    private void publishInput(int requestType, String string) {
        LogUtils.info(TAG, "publishInput......");
        try {
            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withAction(EventConstants.SCREEN_REMOTE_CONTROLL_EVENT)
                    //设置此 OperationBuilder 的操作属性。
                    .build();
            intent.setOperation(operation);
            intent.setParam("inputString", string);
            intent.setParam("requestType", requestType);
            CommonEventData eventData = new CommonEventData(intent);
            CommonEventManager.publishCommonEvent(eventData);
            //这里创建公共事件数据
        } catch (RemoteException e) {
            LogUtils.error(TAG, "publishInput occur exception.");
        }
    }

    private void publishPlusResult(int requestType, String result) {
        LogUtils.info(TAG, "publishPlusResult......");
        try {
            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withAction(EventConstants.SCREEN_REMOTE_CONTROLL_EVENT)
                    .build();
            intent.setOperation(operation);
            intent.setParam("plusResult", result);
            intent.setParam("requestType", requestType);
            CommonEventData eventData = new CommonEventData(intent);
            CommonEventManager.publishCommonEvent(eventData);
        } catch (RemoteException e) {
            LogUtils.error(TAG, "publishPlusResult occur exception.");
        }
    }

    private void publishPaste(int requestType, String result) {
        LogUtils.info(TAG, "publishPaste......");
        try {
            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withAction(EventConstants.PASTEBOARD_MIGRATE_EVENT)
                    .build();
            intent.setOperation(operation);
            intent.setParam("pasteContent", result);
            intent.setParam("requestType", requestType);
            CommonEventData eventData = new CommonEventData(intent);
            CommonEventManager.publishCommonEvent(eventData);
        } catch (RemoteException e) {
            LogUtils.error(TAG, "publishPlusResult occur exception.");
        }
    }
}