package com.easyetz.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.app.ActivityUtils;
import com.easyetz.app.AppManager;
import com.easyetz.app.MyApp;
import com.easyetz.http.TimerHttpApiManage;
import com.easyetz.utils.KeyBoardUtil;
import com.easyetz.utils.LocalManageUtil;
import com.easyetz.utils.ToastUtils;
import com.easyetz.view.CustomDialog;
import com.gyf.barlibrary.ImmersionBar;
import com.gyf.barlibrary.KeyboardPatch;
import com.lzy.okgo.OkGo;
import com.zhy.autolayout.AutoLayoutActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity<V, P extends BasePresent<V>> extends AutoLayoutActivity implements BaseView {

    protected P presenter;
    protected Activity activity;
    protected ImmersionBar mImmersionBar;
    private Unbinder unbinder;
    private CustomDialog dialog;//进度条
    public Toolbar mCommonToolbar;

    public static final Point screenParametersPoint = new Point();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalManageUtil.setLocal(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        MyApp.setBreadContext(activity);
        TimerHttpApiManage.getInstance().startTimer(activity);
        // 添加Activity到堆栈
        AppManager.getAppManager().addActivity(activity);
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
            presenter = initPresenter();
            initBind();
            initViews(savedInstanceState);
            initData();
            initEvent();
        }

        //初始化沉浸式
        if (isImmersionBarEnabled()) {
            initImmersionBar();
        }
    }

    protected abstract int getLayoutId();

    public abstract P initPresenter();

    protected void initBind() {
        ButterKnife.bind(activity);
    }

    protected abstract void initViews(Bundle savedInstanceState);

    protected abstract void initData();

    public abstract void initEvent();

    /**
     * 是否可以使用沉浸式
     *
     * @return the boolean
     */
    protected boolean isImmersionBarEnabled() {
        return true;
    }

    @SuppressLint("ResourceAsColor")
    protected void initImmersionBar() {
        mImmersionBar = ImmersionBar.with(this);
        unbinder = ButterKnife.bind(this);
        mCommonToolbar = ButterKnife.findById(this, R.id.common_toolbar);

        if (mCommonToolbar != null) {
            ImmersionBar.with(this)
                    .titleBar(mCommonToolbar, false)
                    .transparentStatusBar()
                    .statusBarDarkFont(true, 1f)
                    .navigationBarColor(R.color.balance_crypto_color)
                    .init();
            setSupportActionBar(mCommonToolbar);
        }
        mImmersionBar.keyboardEnable(true); //解决软键盘与底部输入框冲突问题;
        KeyboardPatch.patch(this).enable();
        mImmersionBar.init();
    }

    protected <T extends View> T getId(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        ImmersionBar.with(this).destroy(); //必须调用该方法，防止内存泄漏
        // 关闭堆栈中的Activity
        AppManager.getAppManager().finishActivity(activity);
        //在onDestroy()生命周期中释放P中引用的V。
        presenter.detach();
        //在onDestroy()生命周期中取消所有子线程里面的网络连接。

        OkGo.getInstance().cancelTag(activity);
        if (mImmersionBar != null) {
            mImmersionBar.destroy();  //在BaseActivity里销毁
        }
        MyApp.activityCounter.decrementAndGet();

    }

    @Override
    public void showProgress() {
//        ShowDialog.showDialog(this, "", true, null);//加载动画
    }

    @Override
    public void hideProgress() {
//        ShowDialog.dissmiss();
    }

    @Override
    public void toast(CharSequence s) {
        ToastUtils.showShortToast(s);
    }

    @Override
    public void showNullLayout() {

    }

    @Override
    public void hideNullLayout() {

    }

    @Override
    public void showErrorLayout() {
      /*  getId(R.id.error_text).setVisibility(View.VISIBLE);
        getId(R.id.error_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initData();
            }
        });*/
    }
    /* *
     * 设置title
     *
     * @param title 文本*/

    @Override
    public void hideErrorLayout() {
        // getId(R.id.error_text).setVisibility(View.GONE);
    }


    /**
     * 设置左侧返回按钮
     */
    protected void setLeftImg() {
        getId(R.id.iv_back).setVisibility(View.GONE);
    }

    protected void setCenterTitle(String title) {
        if (title == null) {
            return;
        }
        TextView tvTitle = getId(R.id.tv_title);
        ImageView tvTitle1 = getId(R.id.iv_back);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
        goBack();
        if (!activity.getLocalClassName().contains("Vote")) {
//                tvTitle1.setImageResource(R.mipmap.back);
        }

    }
    /* *
     * 设置返回按钮事件*/

    protected void goBack() {

        getId(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyBoardUtil.getInstance(activity).hide();
                ActivityUtils.goBack(activity);
            }
        });
    }

    /**
     * 设置右侧文本
     *
     * @param title        文字
     * @param isVisibility 是否显示
     */

    protected void setRightTitle(String title, Boolean isVisibility) {
        if (title == null) {
            return;
        }
        TextView tvTitle = getId(R.id.tv_right_text);
        tvTitle.setText(title);
        if (isVisibility == true) {
            tvTitle.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
    }

    /**
     * 设置右侧
     *
     * @param isVisibility 是否显示
     */

    protected void setRightImg(Boolean isVisibility) {
        ImageView rightImg = getId(R.id.img_right);
        if (isVisibility == true) {
            rightImg.setVisibility(View.VISIBLE);
        } else {
            rightImg.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在Activity中初始化P，并且连接V
        presenter.attach((V) activity);

    }
    // dialog
    public CustomDialog getDialog() {
        if (dialog == null) {
            dialog = CustomDialog.instance(this);
            dialog.setCancelable(true);
        }
        return dialog;
    }

    public void hideDialog() {
        if (dialog != null)
            dialog.hide();
    }
    public void showDialog(String progressTip) {
        getDialog().show();
        if (progressTip != null) {
            getDialog().setTvProgress(progressTip);
        }
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onStop() {
        ToastUtils.cancelToast();
        super.onStop();
    }
}