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

import com.hoperun.control.utils.LogUtils;
import ohos.rpc.*;

import java.util.Map;

/**
 * 远程连接代理类
 */
public class MyRemoteProxy implements IRemoteBroker {
    /**
     * 远端响应成功的标识
     */
    public static final int ERR_OK = 0;
    private static final String TAG = MyRemoteProxy.class.getSimpleName();
    private final IRemoteObject remote;
    public MyRemoteProxy(IRemoteObject remote) {
        this.remote = remote;
    }

    @Override
    public IRemoteObject asObject() {
        return remote;
    }
    //如果调用者是 RemoteObject，则返回 RemoteObject； 如果调用者是 RemoteProxy，则返回 IRemoteObject。

    public int senDataToRemote(int requestType, Map paramMap) {
        MessageParcel data = MessageParcel.obtain();
        //此类提供读写对象、接口令牌、文件描述符和大数据的方法。
        //obtain()创建一个 MessageParcel 对象。
        MessageParcel reply = MessageParcel.obtain();
        MessageOption option = new MessageOption(MessageOption.TF_SYNC);
        int ec = 1;
        int result = -1;
        try {
            if (paramMap.get("inputString") instanceof String) {
                String inputString = (String) paramMap.get("inputString");
                data.writeInt(requestType);
                data.writeString(inputString);
                remote.sendRequest(requestType, data, reply, option);
                //以同步或异步模式向对等进程发送 MessageParcel 消息。
                //code	表示消息代码，由通信双方确定。 如果接口是由IDL工具生成的，则消息代码由IDL自动生成。
                //data	指示发送到对等进程的 MessageParcel 对象。
                //reply	表示对等进程返回的MessageParcel 对象。
                //option 指示发送消息的同步或异步模式。
            }

            if (requestType == RemoteConnectManagerIml.REQUEST_PLUS) {
                data.writeInt(requestType);
                data.writeInt(Integer.parseInt((String) paramMap.get("plusA")));
                data.writeInt(Integer.parseInt((String) paramMap.get("plusB")));
                remote.sendRequest(requestType, data, reply, option);
            } else if (requestType == RemoteConnectManagerIml.REQUEST_START_PLAY ||
                    requestType == RemoteConnectManagerIml.REQUEST_PAUSE_PLAY) {
                data.writeInt(requestType);
                remote.sendRequest(requestType, data, reply, option);
            } else {

            }

            ec = reply.readInt();
            if (ec != ERR_OK) {
                LogUtils.error(TAG, "RemoteException:");
            } else {
                if (requestType == RemoteConnectManagerIml.REQUEST_PLUS) {
                    result = reply.readInt();
                }
            }
        } catch (RemoteException e) {
            LogUtils.error(TAG, "RemoteException:");
        } finally {
            ec = ERR_OK;
            if (result != -1) {
                ec = result;
            }
            data.reclaim();
            reply.reclaim();
            //reclaim() 将 MessageParcel 对象添加到缓存池。
            //此方法用于清除不再使用的 MessageParcel 对象。
        }
        return ec;
    }
}
