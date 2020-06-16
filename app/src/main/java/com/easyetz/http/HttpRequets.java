package com.easyetz.http;

import com.easyetz.http.callback.JsonCallback;
import com.lzy.okgo.OkGo;

import java.util.Map;

public class HttpRequets {
    /**
     * Gets requets.
     *
     * @param <T>      the type parameter
     * @param url      the url
     * @param tag      the tag
     * @param map      the map
     * @param callback the callback
     */
    public static <T> void getRequets(String url, Object tag, Map<String, String> map, JsonCallback<T> callback) {
        OkGo.<T>get(url)
                .tag(tag)
                .params(map)
                .execute(callback);
    }

    /**
     * Post request.
     *
     * @param <T>      the type parameter
     * @param url      the url
     * @param tag      the tag
     * @param map      the map
     * @param callback the callback
     */
    public static <T> void postRequest(String url, Object tag, Map<String, String> map, JsonCallback<T> callback) {
        OkGo.<T>post(url)
                .tag(tag)
                .params(map)
                .execute(callback);
    }

    /**
     * Post request.
     *
     * @param <T>      the type parameter
     * @param url      the url
     * @param tag      the tag
     * @param parms    the parms
     * @param callback the callback
     */
    public static <T> void postRequest(String url, Object tag, String parms, JsonCallback<T> callback) {
        OkGo.<T>post(url)
                .tag(tag)
                .upJson(parms)
                .execute(callback);
    }
}
