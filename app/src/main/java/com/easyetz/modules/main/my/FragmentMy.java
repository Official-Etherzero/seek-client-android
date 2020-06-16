package com.easyetz.modules.main.my;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easyetz.R;
import com.easyetz.base.BaseFragment;
import com.easyetz.base.BaseUrl;
import com.easyetz.modules.main.my.Language.LanguageSelectionActivity;
import com.easyetz.modules.main.my.about.AboutActivity;
import com.easyetz.modules.main.my.concacts.ContactsActivity;
import com.easyetz.modules.main.my.displayCurrency.DisplayCurrencyActivity;
import com.easyetz.modules.main.my.selectnode.ETZNodeSelectionActivity;
import com.easyetz.modules.main.my.wallets.WalletsManageActivity;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.view.MText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentMy extends BaseFragment<NormalView, NormalPresenter> implements NormalView {
    @BindView(R.id.current_node)
    MText currentNode;
//    @BindView(R.id.current_currency)
//    MText currentCurrency;
//    @BindView(R.id.current_language)
//    MText currentLanguage;
    Unbinder unbinder;

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_my;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    @OnClick({R.id.my_wallet_manage, R.id.my_wallet_node, R.id.my_wallet_language, R.id.my_wallet_help, R.id.my_wallet_about})
    public void onViewClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.my_wallet_manage:
                intent = new Intent(getActivity(), WalletsManageActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
//            case R.id.my_wallet_contacts:
//                intent = new Intent(getActivity(), ContactsActivity.class);
//                startActivity(intent);
//                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
//                break;
            case R.id.my_wallet_node:
                intent = new Intent(getActivity(), ETZNodeSelectionActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
//            case R.id.my_wallet_currency:
//                intent = new Intent(getActivity(), DisplayCurrencyActivity.class);
//                startActivity(intent);
//                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
//                break;
            case R.id.my_wallet_help:
                break;
            case R.id.my_wallet_language:
                intent = new Intent(getActivity(), LanguageSelectionActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.my_wallet_about:
                intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        currentNode.setText(BaseUrl.getEthereumRpcUrl());
    }
}
