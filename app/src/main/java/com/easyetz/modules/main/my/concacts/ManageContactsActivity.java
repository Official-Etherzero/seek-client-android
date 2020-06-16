package com.easyetz.modules.main.my.concacts;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.base.Constants;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.sqlite.BRSQLiteHelper;
import com.easyetz.sqlite.ETZContactsDataStore;
import com.easyetz.utils.ClipboardManager;
import com.easyetz.utils.ETZAnimator;
import com.easyetz.utils.Util;
import com.easyetz.view.MEdit;
import com.easyetz.view.MText;
import com.gyf.barlibrary.ImmersionBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ManageContactsActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {

    @BindView(R.id.contacts_manage_cancel_btn)
    Button cancel;
    @BindView(R.id.contacts_manage_save_btn)
    Button save;
    @BindView(R.id.contacts_manage_edit_btn)
    Button edit;
    @BindView(R.id.contacts_manage_done_btn)
    Button done;
    @BindView(R.id.contacts_edit_name)
    MEdit eName;
    @BindView(R.id.contacts_edit_description)
    MEdit eRemarks;
    @BindView(R.id.contacts_edit_address)
    MEdit eAddress;
    @BindView(R.id.contacts_manage_ll_add)
    LinearLayout contactsAdd;
    @BindView(R.id.contacts_tv_name)
    MText tName;
    @BindView(R.id.contacts_tv_description)
    MText tRemarks;
    @BindView(R.id.contacts_tv_address)
    MText tAddress;
    @BindView(R.id.contacts_manage_ll_show)
    LinearLayout contactsInfo;
    @BindView(R.id.contacts_manage_back_btn)
    ImageButton back;
    @BindView(R.id.contacts_manage_delete_btn)
    Button delete;
    private int from = 0;//1.添加联系人2.显示联系人
    private static String savedMemo;
    private String caddress;
    private String cname;
    private String cremarks;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_manage_cintacts;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(true, 1f)
                .init();
        from = getIntent().getIntExtra("from", 0);
        if (from == 1) {
            contactsAdd.setVisibility(View.VISIBLE);
            contactsInfo.setVisibility(View.GONE);
            save.setVisibility(View.VISIBLE);
        } else if (from == 2) {
            cname = getIntent().getStringExtra("name");
            caddress = getIntent().getStringExtra("address");
            cremarks = getIntent().getStringExtra("remarks");
            tName.setText(cname);
            tAddress.setText(caddress);
            tRemarks.setText(cremarks);
            contactsAdd.setVisibility(View.GONE);
            contactsInfo.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
        } else {
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SCANNER_REQUEST) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra("result");
                int wen = result.indexOf("?");
                int aa = result.indexOf(":");
                if (wen > 0) {
                    result = result.substring(0, wen);
                } else if (aa > 0) {
                    result = result.substring(aa, result.length());
                }

                eAddress.setText(result);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    @OnClick({R.id.contacts_manage_back_btn, R.id.contacts_manage_cancel_btn, R.id.contacts_manage_save_btn, R.id.contacts_manage_edit_btn, R.id.contacts_manage_done_btn, R.id.contacts_manage_scan_btn, R.id.contacts_manage_delete_btn, R.id.contacts_manage_copy_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.contacts_manage_save_btn:
                String address = eAddress.getText().toString().trim();
                String name = eName.getText().toString().trim();
                String remarks = eRemarks.getText().toString().trim();
                if (Util.isNullOrEmpty(name)) return;
                if (Util.isNullOrEmpty(address)) return;

                ContentValues values = new ContentValues();
                values.put(BRSQLiteHelper.CONTACTS_NAME, name);
                values.put(BRSQLiteHelper.CONTACTS_WALLRT_ADDRESS, address);
                values.put(BRSQLiteHelper.CONTACTS_PHONE, "");
                values.put(BRSQLiteHelper.CONTACTS_REMARKS, remarks);
                boolean is = ETZContactsDataStore.getInstance(this).insertContacts(values);
                if (is) {
                    Toast.makeText(this, R.string.My_successfully_saved, Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case R.id.contacts_manage_done_btn:
                contactsAdd.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
                back.setVisibility(View.VISIBLE);
                contactsInfo.setVisibility(View.VISIBLE);
                String updataAddress = eAddress.getText().toString().trim();
                String updataName = eName.getText().toString().trim();
                String updataRemarks = eRemarks.getText().toString().trim();
                if (Util.isNullOrEmpty(updataName)) return;
                if (Util.isNullOrEmpty(updataAddress)) return;

                ContentValues updataDvalues = new ContentValues();
                updataDvalues.put(BRSQLiteHelper.CONTACTS_NAME, updataName);
                updataDvalues.put(BRSQLiteHelper.CONTACTS_WALLRT_ADDRESS, updataAddress);
                updataDvalues.put(BRSQLiteHelper.CONTACTS_PHONE, "");
                updataDvalues.put(BRSQLiteHelper.CONTACTS_REMARKS, updataRemarks);
                boolean updata = ETZContactsDataStore.getInstance(this).updataContacts(updataDvalues, caddress);
                if (updata) {
                    tName.setText(updataName);
                    tAddress.setText(updataAddress);
                    tRemarks.setText(updataRemarks);
                }
                break;
            case R.id.contacts_manage_edit_btn:
                eName.setText(cname);
                eAddress.setText(caddress);
                eRemarks.setText(cremarks);
                contactsInfo.setVisibility(View.GONE);
                edit.setVisibility(View.GONE);
                back.setVisibility(View.GONE);
                done.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                contactsAdd.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                break;
            case R.id.contacts_manage_delete_btn:
                boolean del = ETZContactsDataStore.getInstance(this).deleteContacts(caddress);
                if (del) {
                    Toast.makeText(this, R.string.My_successfully_deleted, Toast.LENGTH_LONG).show();
                    finish();
                }

                break;
            case R.id.contacts_manage_copy_btn:
                ClipboardManager.putClipboard(ManageContactsActivity.this, tAddress.getText().toString());
                Toast.makeText(ManageContactsActivity.this, getString(R.string.Receive_copied), Toast.LENGTH_LONG).show();
                break;
            case R.id.contacts_manage_scan_btn:
                ETZAnimator.openScanner(this, Constants.SCANNER_REQUEST);
                break;
            case R.id.contacts_manage_cancel_btn:
                contactsAdd.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
                back.setVisibility(View.VISIBLE);
                contactsInfo.setVisibility(View.VISIBLE);
                break;
            case R.id.contacts_manage_back_btn:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
