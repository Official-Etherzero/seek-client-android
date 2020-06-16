package com.easyetz.modules.main.my.Language;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.LanguageAdapter;
import com.easyetz.app.ActivityUtils;
import com.easyetz.app.AppManager;
import com.easyetz.base.BaseActivity;
import com.easyetz.bean.languageEntity;
import com.easyetz.modules.main.MainActivity;
import com.easyetz.modules.main.my.displayCurrency.DisplayCurrencyActivity;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.utils.LocalManageUtil;
import com.easyetz.utils.SPLUtil;
import com.easyetz.utils.SharedPrefsUitls;
import com.gyf.barlibrary.ImmersionBar;

public class LanguageSelectionActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {

    private RecyclerView etz_node_lv;
    LanguageAdapter adapter;
    private LinearLayoutManager linearLayoutManager;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_etznode_selection;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setCenterTitle(getResources().getString(R.string.my_yysz));
        etz_node_lv = findViewById(R.id.etz_node_lv);
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(true, 1f)
                .init();
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        etz_node_lv.setLayoutManager(linearLayoutManager);
        adapter = new LanguageAdapter(R.layout.node_list_item, LocalManageUtil.getLanguageList(this));
        etz_node_lv.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                languageEntity item = (languageEntity) adapter.getItem(position);
                if (item.getLid() == 1) {
                    SharedPrefsUitls.getInstance().putPreferredFiatIso(LanguageSelectionActivity.this, "CNY");
                } else if (item.getLid() == 2) {
                    SharedPrefsUitls.getInstance().putPreferredFiatIso(LanguageSelectionActivity.this, "USD");
                } else if (item.getLid() == 3) {
                    SharedPrefsUitls.getInstance().putPreferredFiatIso(LanguageSelectionActivity.this, "KRW");
                }
                LocalManageUtil.saveSelectLanguage(activity, item.getLid());
                AppManager.getAppManager().finishAllActivity();
                ActivityUtils.next(activity, MainActivity.class);
//                adapter.notifyDataSetChanged();
            }

        });

    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
