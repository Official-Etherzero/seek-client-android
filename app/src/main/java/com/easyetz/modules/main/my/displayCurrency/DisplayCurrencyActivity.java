package com.easyetz.modules.main.my.displayCurrency;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.base.Constants;
import com.easyetz.bean.CurrencyEntity;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.modules.walletmanage.WalletsMaster;
import com.easyetz.sqlite.RatesDataSource;
import com.easyetz.utils.CurrencyUtils;
import com.easyetz.utils.FontManager;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.view.MButton;
import com.easyetz.view.MText;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DisplayCurrencyActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {
    private static final String TAG = DisplayCurrencyActivity.class.getName();
    @BindView(R.id.left_button)
    MButton leftButton;
    @BindView(R.id.right_button)
    MButton rightButton;
    @BindView(R.id.exchange_text)
    MText exchangeText;
    @BindView(R.id.currency_list_view)
    ListView listView;
    private CurrencyListAdapter adapter;
    //    private String ISO;
//    private float rate;
    public static boolean appVisible = false;
    private static DisplayCurrencyActivity app;

    public static DisplayCurrencyActivity getApp() {
        return app;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_display_currency;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        adapter = new CurrencyListAdapter(this);
        List<CurrencyEntity> currencies = RatesDataSource.getInstance(this).getAllCurrencies(this, "BTC");
        List<CurrencyEntity> cleanList = cleanList(currencies);
        adapter.addAll(cleanList);
        int unit = SharedPrefsUitls.getInstance().getCryptoDenomination( "BTC"); // any iso, using one for all for now
        if (unit == Constants.CURRENT_UNIT_BITS) {
            setButton(true);
        } else {
            setButton(false);
        }
        updateExchangeRate();
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView currencyItemText = view.findViewById(R.id.currency_item_text);
                final String selectedCurrency = currencyItemText.getText().toString();
                String iso = selectedCurrency.substring(0, 3);
                SharedPrefsUitls.getInstance().putPreferredFiatIso(DisplayCurrencyActivity.this, iso);

                updateExchangeRate();

            }

        });
    }

    private List<CurrencyEntity> cleanList(List<CurrencyEntity> list) {

        Iterator<CurrencyEntity> iter = list.iterator();
        while (iter.hasNext()) {
            CurrencyEntity ent = iter.next();
            if (WalletsMaster.getInstance().isIsoCrypto(this, ent.name)) {
                iter.remove();
            }
        }
        return list;
    }

    private void updateExchangeRate() {
        //set the rate from the last saved
        String iso = SharedPrefsUitls.getInstance().getPreferredFiatIso();
        CurrencyEntity entity = RatesDataSource.getInstance(this).getCurrencyByCode(this, "BTC", iso);//hard code BTC for this one
        if (entity != null) {
            String formattedExchangeRate = CurrencyUtils.getFormattedAmount(DisplayCurrencyActivity.this, SharedPrefsUitls.getInstance().getPreferredFiatIso(), new BigDecimal(entity.rate));
            exchangeText.setText(String.format("%s = %s", CurrencyUtils.getFormattedAmount(this, "BTC", new BigDecimal(100000000)), formattedExchangeRate));
        }
        adapter.notifyDataSetChanged();
    }

    private void setButton(boolean left) {
        if (left) {
            SharedPrefsUitls.getInstance().putCryptoDenomination( "BTC", Constants.CURRENT_UNIT_BITS);
            leftButton.setTextColor(getResources().getColor(R.color.white));
            leftButton.setBackground(getDrawable(R.drawable.b_half_left_blue));
            rightButton.setTextColor(getResources().getColor(R.color.dark_blue));
            rightButton.setBackground(getDrawable(R.drawable.b_half_right_blue_stroke));
        } else {
            SharedPrefsUitls.getInstance().putCryptoDenomination( "BTC", Constants.CURRENT_UNIT_BITCOINS);
            leftButton.setTextColor(getResources().getColor(R.color.dark_blue));
            leftButton.setBackground(getDrawable(R.drawable.b_half_left_blue_stroke));
            rightButton.setTextColor(getResources().getColor(R.color.white));
            rightButton.setBackground(getDrawable(R.drawable.b_half_right_blue));
        }
        updateExchangeRate();

    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.back_button, R.id.left_button, R.id.right_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_button:
                onBackPressed();
                break;
            case R.id.left_button:
                setButton(true);
                break;
            case R.id.right_button:
                setButton(false);
                break;
        }
    }

    public class CurrencyListAdapter extends ArrayAdapter<CurrencyEntity> {
        public final String TAG = CurrencyListAdapter.class.getName();

        private final Context mContext;
        private final int layoutResourceId;
        private TextView textViewItem;
        private final Point displayParameters = new Point();

        public CurrencyListAdapter(Context mContext) {

            super(mContext, R.layout.currency_list_item);

            this.layoutResourceId = R.layout.currency_list_item;
            this.mContext = mContext;
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getSize(displayParameters);
//        currencyListAdapter = this;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final String oldIso = SharedPrefsUitls.getInstance().getPreferredFiatIso();
            if (convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            // get the TextView and then set the text (item name) and tag (item ID) values
            textViewItem = convertView.findViewById(R.id.currency_item_text);
            FontManager.overrideFonts(textViewItem);
            String iso = getItem(position).code;
            Currency c = null;
            try {
                c = Currency.getInstance(iso);
            } catch (IllegalArgumentException ignored) {
            }
            textViewItem.setText(c == null ? iso : String.format("%s (%s)", iso, c.getSymbol()));
            ImageView checkMark = convertView.findViewById(R.id.currency_checkmark);

            if (iso.equalsIgnoreCase(oldIso)) {
                checkMark.setVisibility(View.VISIBLE);
            } else {
                checkMark.setVisibility(View.GONE);
            }
            normalizeTextView();
            return convertView;

        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

        private boolean isTextSizeAcceptable(TextView textView) {
            textView.measure(0, 0);
            int textWidth = textView.getMeasuredWidth();
            int checkMarkWidth = 76 + 20;
            return (textWidth <= (displayParameters.x - checkMarkWidth));
        }

        private boolean normalizeTextView() {
            int count = 0;
            while (!isTextSizeAcceptable(textViewItem)) {
                count++;
                float textSize = textViewItem.getTextSize();
                textViewItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize - 2);
                this.notifyDataSetChanged();
            }
            return (count > 0);
        }

    }


}
