/*
 * Copyright the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.easyetz.blockchain.data;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;

import com.easyetz.app.MyApp;
import com.easyetz.blockchain.BtcConfiguration;
import com.easyetz.blockchain.walletutils.BtcWalletUtil;
import com.easyetz.utils.MyLog;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.BalanceType;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;


/**
 * @author Andreas Schildbach
 */
public final class WalletBalanceLiveData extends AbstractWalletLiveData<Coin>
        implements OnSharedPreferenceChangeListener {
    private final BalanceType balanceType;
    private final BtcConfiguration config;

    public WalletBalanceLiveData(final MyApp application, final BalanceType balanceType) {
        super(application);
        this.balanceType = balanceType;
        this.config = application.getConfiguration();
    }

    public WalletBalanceLiveData(final MyApp application) {
        this(application, BalanceType.ESTIMATED);
    }

    @Override
    protected void onWalletActive(final Wallet wallet) {
        addWalletListener(wallet);
        config.registerOnSharedPreferenceChangeListener(this);
        load();
    }

    @Override
    protected void onWalletInactive(final Wallet wallet) {
        config.unregisterOnSharedPreferenceChangeListener(this);
        removeWalletListener(wallet);
    }

    private void addWalletListener(final Wallet wallet) {
        wallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addReorganizeEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addChangeEventListener(Threading.SAME_THREAD, walletListener);
    }

    private void removeWalletListener(final Wallet wallet) {
        wallet.removeChangeEventListener(walletListener);
        wallet.removeReorganizeEventListener(walletListener);
        wallet.removeCoinsSentEventListener(walletListener);
        wallet.removeCoinsReceivedEventListener(walletListener);
    }

    @Override
    protected void load() {
        final Wallet wallet = getWallet();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                org.bitcoinj.core.Context.propagate(new Context(BtcWalletUtil.getParams()));
                postValue(wallet.getBalance(balanceType));
                MyLog.i("COIN============="+wallet.getBalance(balanceType).getValue());
            }
        });
    }

    private final WalletListener walletListener = new WalletListener();

    private class WalletListener implements WalletCoinsReceivedEventListener, WalletCoinsSentEventListener,
            WalletReorganizeEventListener, WalletChangeEventListener {
        @Override
        public void onCoinsReceived(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                final Coin newBalance) {
            triggerLoad();
        }

        @Override
        public void onCoinsSent(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                final Coin newBalance) {
            triggerLoad();
        }

        @Override
        public void onReorganize(final Wallet wallet) {
            triggerLoad();
        }

        @Override
        public void onWalletChanged(final Wallet wallet) {
            triggerLoad();
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (BtcConfiguration.PREFS_KEY_BTC_PRECISION.equals(key))
            load();
    }
}
