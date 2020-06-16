package com.easyetz.http.callback;

import android.content.Context;
import android.widget.Toast;

import com.easyetz.R;
import com.easyetz.app.MyApp;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.ToastUtils;
import com.google.gson.JsonSyntaxException;
import com.lzy.okgo.callback.AbsCallback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import okhttp3.Response;

public class JsonCallback<T> extends AbsCallback<T> {
    private Type type;
    private Class<T> clazz;


    public JsonCallback() {

    }

    public JsonCallback(Type type) {
        this.type = type;
    }

    public JsonCallback(Class<T> clazz) {
        this.clazz = clazz;
    }


    /**
     * 该方法是子线程处理，不能做ui相关的工作
     * 主要作用是解析网络返回的 response 对象,生产onSuccess回调中需要的数据对象
     */
    @Override
    public T convertResponse(Response response) throws Throwable {
        if (type == null) {
            if (clazz == null) {
                Type genType = getClass().getGenericSuperclass();
                type = ((ParameterizedType) genType).getActualTypeArguments()[0];
            } else {
                JsonConvert<T> convert = new JsonConvert<>(clazz);
                return convert.convertResponse(response);
            }
        }

        JsonConvert<T> convert = new JsonConvert<>(type);
        return convert.convertResponse(response);
    }

    @Override
    public void onSuccess(com.lzy.okgo.model.Response<T> response) {
    }

    @Override
    public void onError(com.lzy.okgo.model.Response<T> response) {
        super.onError(response);
        int code = response.code();
//        if (code == 404) {
//            Toast.makeText(MyApp.getBreadContext(), R.string.url_error, Toast.LENGTH_LONG).show();
//        }
        MyLog.i("onError=="+response.getException().getMessage());
//        if (response.getException() instanceof SocketTimeoutException) {
//            Toast.makeText(MyApp.getBreadContext(), R.string.socket_time_out, Toast.LENGTH_LONG).show();
//            MyLog.i("onError=="+response.getException().getMessage());
//        } else if (response.getException() instanceof SocketException) {
////            Toast.makeText(MyApp.getBreadContext(), R.string.socket_exception, Toast.LENGTH_LONG).show();
//        } else if (response.getException() instanceof JsonSyntaxException) {
////            Toast.makeText(MyApp.getBreadContext(), R.string.error_parse, Toast.LENGTH_LONG).show();
//            MyLog.i("onError=="+response.getException().getMessage());
//        } else {
////            Toast.makeText(MyApp.getBreadContext(), response.getException().getMessage(), Toast.LENGTH_LONG).show();
//            MyLog.i("onError=="+response.getException().getMessage());
//        }
    }
}
