package com.jeason.springthrift.handler;

import com.alibaba.fastjson.JSONObject;
import com.jeason.springthrift.Response;

/**
 * @Auther: jeason
 * @Date: 2018/8/4 18:52
 * @Description:
 */
@FunctionalInterface
public interface ThriftServiceHandler {
    Response handle(String operation, JSONObject paramJson);

    default Response defaultSuccessResponse() {
        return defaultResponse(200, "success");
    }

    default Response defaultSuccessResponse(String data) {
        return defaultResponse(data, 200, "success");
    }

    default Response defaultResponse(int code, String message) {
        return defaultResponse(null, code, message);
    }

    default Response defaultResponse(String data, int code, String message) {
        return new Response(data, code, message);
    }
}
