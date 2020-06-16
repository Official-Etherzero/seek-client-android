package com.easyetz.utils;


import android.support.annotation.NonNull;

import com.easyetz.base.BaseUrl;
import com.easyetz.base.Constants;
import com.easyetz.http.HttpRequets;
import com.easyetz.http.callback.JsonCallback;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class nodeUtiles {

    static List<SeekNode> list = new ArrayList<>();
    public static String[] nodes = {"http://104.236.240.227:8545", "http://182.61.139.140:8545", "http://13.251.6.203:8545"};


    public static void seekNodeList() {
        for (String str : nodes) {
            getBlockNumber(str);
        }

        try {
            Thread.sleep(10000);
            Collections.sort(list);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BaseUrl.node=list.get(0).url;
        SharedPrefsUitls.getInstance().putCurrentNode(BaseUrl.node);
//        for (SeekNode node : list) {
//            MyLog.i("nodeUtiles-----" + node.toString());
//
//        }


    }

    private static synchronized void getBlockNumber(String url) {
        final JSONObject payload = new JSONObject();
        final JSONArray params = new JSONArray();
        try {
            payload.put(Constants.METHOD, Constants.ETH_BLOCK_NUMBER);
            payload.put(Constants.PARAMS, params);
            payload.put(Constants.ID, "1");
            payload.put(Constants.JSONRPC, "2.0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpRequets.postRequest(url, "", payload.toString(), new JsonCallback<String>() {

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        long mi = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
                        JSONObject object = new JSONObject(response.body().string());
                        String b = object.optString("result");
                        int num = Integer.valueOf(new BigInteger(b.substring(2, b.length()), 16).toString());
                        list.add(new SeekNode(url, mi, num));
                        MyLog.i("nodeUtiles-----" + url + "--" + mi + "--" + num);
                        return super.convertResponse(response);
                    }

                }
        );
    }

//
//    curl http://182.61.139.140:8545 -X POST -H "Content-type:application/json" --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}'
//    curl http://104.236.240.227:8545 -X POST -H "Content-type:application/json" --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}'
//    curl http://13.251.6.203:8545 -X POST -H "Content-type:application/json" --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}'


    public static class SeekNode implements Comparable<SeekNode> {
        public String url;
        long responseTime;
        int blockNumber;

        @Override
        public String toString() {
            return "SeekNode{" +
                    "url='" + url + '\'' +
                    ", responseTime=" + responseTime +
                    ", blockNumber=" + blockNumber +
                    '}';
        }

        public SeekNode(String url, long responseTime, int blockNumber) {
            super();
            this.url = url;
            this.responseTime = responseTime;
            this.blockNumber = blockNumber;
        }

        @Override
        public int compareTo(@NonNull SeekNode o) {

            int i = o.blockNumber - this.blockNumber;//先按高度排序
            if (i == 0) {
                return (int) (this.responseTime - o.responseTime);//如果高度相等，再按响应时间排序
            }
            return i;
        }
    }

}
