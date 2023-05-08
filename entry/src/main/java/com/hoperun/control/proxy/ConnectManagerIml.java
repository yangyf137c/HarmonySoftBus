/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License,Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hoperun.control.proxy;

import com.hoperun.control.RemoteService;
import com.hoperun.control.utils.LogUtils;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.rpc.IRemoteObject;

/**
 * 远程管理类实现
 */
public class ConnectManagerIml<returnType,paramsType> implements ConnectManager<returnType,paramsType> {
    /**
     * 发送数据
     */
    public static final int REQUEST_SEND_DATA = 1;

    /**
     * 请求查询
     */
    public static final int REQUEST_SEND_SEARCH = 2;

    /**
     * 方向移动通知
     */
    public static final int REQUEST_SEND_MOVE = 3;

    /**
     * 方向移动通知
     */
    public static final int REQUEST_PLUS = 4;

    /**
     * 方向移动通知
     */
    public static final int REQUEST_START_PLAY = 5;

    /**
     * 方向移动通知
     */
    public static final int REQUEST_PAUSE_PLAY = 6;
    private static final String TAG = ConnectManagerIml.class.getName();

    public static final int RemoteType = 1;
    public static final int DocType=2;

    private static ConnectManager instance;
    private int type=1;
    private IAbilityConnection conn;
    private Proxy proxy;

    public ConnectManagerIml() {
    }

    public ConnectManagerIml(int type) {
        this.type=type;
    }

    /**
     * 获取管理类实例
     *
     * @return 管理类实例
     */
    public static synchronized ConnectManager getInstance(int type) {
        if (instance == null) {
            instance = new ConnectManagerIml(type);
        }
        return instance;
    }

    /**
     * 连接远程PA
     *
     * @param context  上下文
     * @param deviceId 设备id
     */
    @Override
    public void connectPa(Context context, String deviceId) {
        if (deviceId != null && !deviceId.trim().isEmpty()) {
            Intent connectPaIntent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withDeviceId(deviceId)
                    .withBundleName(context.getBundleName())
                    .withAbilityName(RemoteService.class.getName())
                    .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                    .build();
            connectPaIntent.setOperation(operation);
            conn = new IAbilityConnection() {
                @Override
                public void onAbilityConnectDone(ElementName elementName, IRemoteObject remote, int resultCode) {
                    LogUtils.info(TAG, "===connectAbility done");
                    if(type==1)
                        proxy = new MyRemoteProxy(remote);
                    else if(type==2)
                        proxy=new MyDocProxy(remote);
                    //多态没实现好，需要手动输入类型来指定相应的代理
                }

                @Override
                public void onAbilityDisconnectDone(ElementName elementName, int resultCode) {
                    LogUtils.info(TAG, "onAbilityDisconnectDone......");
                    proxy = null;
                }
            };
            context.connectAbility(connectPaIntent, conn);
        }
    }

    @Override
    public returnType sendRequest(int requestType, paramsType params) {
        returnType result = null;
        if (proxy != null) {
            Object t=proxy.senDataToRemote(requestType, params);
            result = (returnType) proxy.senDataToRemote(requestType, params);
        }
        return result;
    }
}
