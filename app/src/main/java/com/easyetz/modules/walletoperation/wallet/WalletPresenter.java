package com.easyetz.modules.walletoperation.wallet;

import android.content.Context;

import com.easyetz.base.BasePresent;
import com.easyetz.base.BaseUrl;
import com.easyetz.bean.ResponseResultBean;
import com.easyetz.bean.TokenLogsResponesBean;
import com.easyetz.bean.TransactionRecords;
import com.easyetz.bean.TransactionResponesBean;
import com.easyetz.http.HttpRequets;
import com.easyetz.http.callback.JsonCallback;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;
import com.lzy.okgo.model.Response;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletPresenter extends BasePresent<WalletView> {
    public void getTransactions(Context ctx, String wid, String address) {
        String url = null;
        if (wid.contains("SEEK")) {
            url = BaseUrl.getETZTransactionsUrl(address);
        } else if (wid.contains("ETH")) {
            url = BaseUrl.getEThTransactionsUrl(address);
        } else {
            url = null;
        }
        if (url == null) return;
        HttpRequets.getRequets(url, getClass(), new HashMap<>(), new JsonCallback<ResponseResultBean<List<TransactionResponesBean>>>() {
            @Override
            public void onSuccess(Response<ResponseResultBean<List<TransactionResponesBean>>> response) {
                List<TransactionResponesBean> tlist = response.body().result;
                MyLog.i("TransactionResponesBean----" + tlist.toString());
                if (view != null)
                    view.walletTransactions(getTransactionsList(tlist, address));
            }

            @Override
            public void onError(Response<ResponseResultBean<List<TransactionResponesBean>>> response) {
                if (view != null)
                    view.showError(response.getException().getMessage());
                super.onError(response);
            }
        });

    }

    private List<TransactionRecords> getTransactionsList(List<TransactionResponesBean> tlist, String address) {
        List<TransactionRecords> trList = new ArrayList<>();
        for (int i = 0; i < tlist.size(); i++) {
            TransactionResponesBean bean = tlist.get(i);
            MyLog.i("getTransactionsList=" + bean.toString());
            trList.add(new TransactionRecords(bean.getFrom(), bean.getTo(), bean.getValue(), bean.getTimestamp(), bean.getGasUsed(), bean.getGasPrice(), bean.getBlockNumber(), bean.getHash(),
                    bean.getStatus(), !address.equalsIgnoreCase(bean.getFrom())));
        }
        return trList;
    }

    public void getLogs(Context ctx, String wid, String address, String contractAdd) {
        String url = null;
        String fromBlock;
        Map<String, String> tokenBlock = new HashMap<>();
        tokenBlock.put("0x013b6e279989aa20819a623630fe678c9f43a48f", "6095689");
        tokenBlock.put("0x7be53cb8416ff6d528e336e62887ffd5542d37fa", "8821892");
        tokenBlock.put("0xe98a268ae148ecdfc4a8e029261d8e1d8c4d8783", "3379351");
        tokenBlock.put("0xb85581c73afbf7f899abacb84d795202abe5033d", "2530078");
        tokenBlock.put("0xf30d911f189c17635c3047538f98a1fedb8875ff", "104358");
        tokenBlock.put("0x0f078ac34a827c16dd1af5fb6d1549cda74e856e", "105249");
        tokenBlock.put("0xfddb863dbff0632d57571a5af38482966e722ab4", "377455");
        tokenBlock.put("0x290a82fd9c3b1d599faf3181739a17f6f8d4cff0", "1785447");

        String block = SharedPrefsUitls.getInstance().getTokenLogBlock(contractAdd + wid);

        if (Util.isNullOrEmpty(block)) {
            if (tokenBlock.containsKey(contractAdd)) {
                fromBlock = tokenBlock.get(contractAdd);
            } else {
                fromBlock = "8821892";
            }
        } else {
            fromBlock = block;
        }
        if (wid.contains("SEEK")) {
            url = BaseUrl.getETZTokenLogsUrl(address, contractAdd, fromBlock);
        } else if (wid.contains("ETH")) {
            url = BaseUrl.getEThTokenLogsUrl(address, contractAdd, "");
        } else {
            url = null;
        }
        if (url == null) return;
        HttpRequets.getRequets(url, getClass(), new HashMap<>(), new JsonCallback<ResponseResultBean<List<TokenLogsResponesBean>>>() {
            @Override
            public void onSuccess(Response<ResponseResultBean<List<TokenLogsResponesBean>>> response) {
                List<TokenLogsResponesBean> tlist = response.body().result;
                if (view != null)
                    view.walletTransactions(getTransactionsListFromLogs(tlist, address));
                if (tlist != null && tlist.size() > 0) {
                    SharedPrefsUitls.getInstance().putTokenLogBlock(contractAdd.toLowerCase() + wid, tlist.get(tlist.size() - 1).getBlockNumber());
                }

            }
        });
    }

    private List<TransactionRecords> getTransactionsListFromLogs(List<TokenLogsResponesBean> tlist, String address) {
        List<TransactionRecords> trList = new ArrayList<>();
        for (int i = 0; i < tlist.size(); i++) {
            TokenLogsResponesBean bean = tlist.get(i);
            String v = bean.getData();
            MyLog.i("getTransactionsListFromLogs=" + v);
            String data = new BigInteger(v.substring(2, v.length()), 16).toString();
//             String data=new BigInteger(v.substring(2, v.length()).replaceFirst("^0*", ""),16).toString();
//            MyLog.i("getTransactionsListFromLogs=" +Integer.parseInt(v.substring(2, v.length()).replaceFirst("^0*", ""), 16));
            trList.add(new TransactionRecords(bean.getFrom(), bean.getTo(), data, bean.getTimestamp(), bean.getGasUsed(), bean.getGasPrice(), bean.getBlockNumber(), bean.getTxHash(),
                    bean.getStatus(), !address.equalsIgnoreCase(bean.getFrom())));

        }
        return trList;
    }


}
