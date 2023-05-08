package com.hoperun.control.proxy;

import java.util.Map;

public interface Proxy<returnType,paramsType> {

    public returnType senDataToRemote(int requestType, paramsType param);
}
