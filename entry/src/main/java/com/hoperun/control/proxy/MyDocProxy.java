package com.hoperun.control.proxy;

import com.hoperun.control.utils.LogUtils;
import ohos.rpc.*;

public class MyDocProxy implements IRemoteBroker,Proxy<String,Integer>{
    public static final int ERR_OK = 0;
    private static final String TAG = MyDocProxy.class.getSimpleName();
    private final IRemoteObject remote;
    public MyDocProxy(IRemoteObject remote) {
        this.remote = remote;
    }

    @Override
    public IRemoteObject asObject() {
        return remote;
    }

    @Override
    public String senDataToRemote(int requestType, Integer param){
        MessageParcel data = MessageParcel.obtain();
        //此类提供读写对象、接口令牌、文件描述符和大数据的方法。
        //obtain()创建一个 MessageParcel 对象。
        MessageParcel reply = MessageParcel.obtain();
        MessageOption option = new MessageOption(MessageOption.TF_SYNC);
        int ec = 1;
        String result = null;

        try {
            if (requestType == ConnectManagerIml.REQUEST_SEND_DATA) {
                remote.sendRequest(requestType, data, reply, option);
            }
        }
        catch (Exception e)
        {}

            ec = reply.readInt();
            if (ec != ERR_OK) {
                LogUtils.error(TAG, "RemoteException:");
            } else {
                if (requestType == ConnectManagerIml.REQUEST_SEND_DATA) {
                    result = reply.readString();
                }
            }
            data.reclaim();
            reply.reclaim();
            //reclaim() 将 MessageParcel 对象添加到缓存池。
            //此方法用于清除不再使用的 MessageParcel 对象。
        return result;
    }
}
