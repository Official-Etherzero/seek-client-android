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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.easyetz.app.MyApp;
import com.easyetz.blockchain.walletutils.BtcWalletUtil;

import org.bitcoinj.wallet.Wallet;


/**
 * @author Andreas Schildbach
 */
public abstract class AbstractWalletLiveData<T> extends ThrottelingLiveData<T> {
    private final MyApp application;
    private final LocalBroadcastManager broadcastManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Wallet wallet;

    public AbstractWalletLiveData(final MyApp application) {
        super();
        this.application = application;
        this.broadcastManager = LocalBroadcastManager.getInstance(application);
    }

    public AbstractWalletLiveData(final MyApp application, final long throttleMs) {
        super(throttleMs);
        this.application = application;
        this.broadcastManager = LocalBroadcastManager.getInstance(application);
    }

    @Override
    protected final void onActive() {
        broadcastManager.registerReceiver(walletReferenceChangeReceiver,
                new IntentFilter(MyApp.class.getPackage().getName()));
        loadWallet();
    }

    @Override
    protected final void onInactive() {
        // TODO cancel async loading
        if (wallet != null)
            onWalletInactive(wallet);
        broadcastManager.unregisterReceiver(walletReferenceChangeReceiver);
    }

    private void loadWallet() {
        BtcWalletUtil.getWalletAsync(onWalletLoadedListener);
    }

    protected Wallet getWallet() {
        return wallet;
    }

    private final OnWalletLoadedListener onWalletLoadedListener = new OnWalletLoadedListener() {
        @Override
        public void onWalletLoaded(final Wallet wallet) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AbstractWalletLiveData.this.wallet = wallet;
                    onWalletActive(wallet);
                }
            });
        }
    };

    private final BroadcastReceiver walletReferenceChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (wallet != null)
                onWalletInactive(wallet);
            loadWallet();
        }
    };

    protected abstract void onWalletActive(Wallet wallet);

    protected void onWalletInactive(final Wallet wallet) {
        // do nothing by default
    };
}
