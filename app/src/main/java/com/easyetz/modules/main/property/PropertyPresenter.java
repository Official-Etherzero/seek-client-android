package com.easyetz.modules.main.property;

import com.easyetz.app.MyApp;
import com.easyetz.base.BasePresent;
import com.easyetz.base.BaseUrl;
import com.easyetz.bean.BalanceEntity;
import com.easyetz.bean.MinerMoreBean;
import com.easyetz.bean.Token;
import com.easyetz.bean.TokenInfo;
import com.easyetz.bean.WalletBean;
import com.easyetz.http.HttpRequets;
import com.easyetz.http.callback.JsonCallback;
import com.easyetz.modules.walletmanage.WalletsMaster;
import com.easyetz.sqlite.BalanceDataSource;
import com.easyetz.sqlite.WalletDataStore;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.TokenUtil;
import com.google.gson.Gson;
import com.lzy.okgo.model.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PropertyPresenter extends BasePresent<PropertyView> {


    public List<WalletBean> getWalletList() {
        return WalletDataStore.getInstance().queryAllWallets();
    }

    public List<Token> getTokens(WalletBean wallet) {
        List<Token> tokens = new ArrayList<>();
        Map<String, String> balances = BalanceDataSource.getInstance().getWalletTokensBalance(wallet.getId());
        MyLog.i("------------------1" + balances.toString());
        Token token;
        List<String> tokenList;
        List<TokenInfo> tokenInfos = null;
        if (wallet.getId().startsWith("BTC")) {
            tokenInfos = TokenUtil.getTokenItems(MyApp.getmInstance());
            token = new Token(new TokenInfo(wallet.getAddress(), "Bitcoin", "BTC", "", wallet.getStartColor(), wallet.getEndColor(), wallet.getDecimals()), balances.get("BTC"));
        } else if (wallet.getId().startsWith("ETH")) {
            tokenInfos = TokenUtil.getEthTokens(MyApp.getmInstance());
            token = new Token(new TokenInfo(wallet.getAddress(), "Ethereum", "ETH", "", wallet.getStartColor(), wallet.getEndColor(), wallet.getDecimals()), balances.get("ETH"));
        } else {
            tokenInfos = TokenUtil.getTokenItems(MyApp.getmInstance());
            token = new Token(new TokenInfo(wallet.getAddress(), "SeekChain", "SEEK", "", wallet.getStartColor(), wallet.getEndColor(), wallet.getDecimals()), balances.get("SEEK"));
        }
        tokens.add(token);
        MyLog.i("------------------2" + balances.toString());
        tokenList = SharedPrefsUitls.getInstance().getWalletTokenList(wallet.getId());
        if (tokenList == null) return tokens;
        for (String str : tokenList) {
            for (TokenInfo tokenInfo : tokenInfos) {
                MyLog.i("------------------3" + str + "****" + tokenInfo.symbol);
                if (str.equals(tokenInfo.symbol)) {
                    tokens.add(new Token(tokenInfo, balances.get(str)));
                    MyLog.i("------------------4" + balances.get(str));

                }
            }
        }
        return tokens;

    }

    public BigDecimal totalAssets(List<Token> tokens) {
        BigDecimal assets = new BigDecimal("0");

        for (Token token : tokens) {
            String iso = token.tokenInfo.symbol;
            assets = assets.add(WalletsMaster.getInstance().getWalletByIso(MyApp.getBreadContext(), iso).getFiatBalance(iso, token.balance));
        }
        return assets;
    }

    public void getMinerMore(String address) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("address", address);
        HttpRequets.postRequest(BaseUrl.getMiner_more(address), getClass(), hashMap, new JsonCallback<MinerMore>() {

            @Override
            public MinerMore convertResponse(okhttp3.Response response) throws Throwable {
                String minerJson = response.body().string();
                SharedPrefsUitls.getInstance().setMinerInfo(minerJson);
                Gson gson=new Gson();
                //序列化
                MinerMore user=gson.fromJson(minerJson,MinerMore.class);
                return user;
            }

            @Override
            public void onSuccess(Response<MinerMore> response) {
                if (view == null) return;
                if (response.body() != null && response.body().resp != null&&response.body().resp.getMiner()!=null) {
                    String vip = response.body().resp.getMiner().getLevel();
                    MyLog.i("getMinerMore====" + vip);
                    view.setMinerMore(vip);
                } else {
                    view.setMinerMore("");
                }

            }

            @Override
            public void onError(Response<MinerMore> response) {
                if (view == null) return;
                view.setMinerMore("");
                super.onError(response);
            }
        });


    }

    class MinerMore {
        MinerMoreBean resp;
    }
}
