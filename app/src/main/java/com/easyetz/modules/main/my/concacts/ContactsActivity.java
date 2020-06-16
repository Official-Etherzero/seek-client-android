package com.easyetz.modules.main.my.concacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.ContactsAdapter;
import com.easyetz.base.BaseActivity;
import com.easyetz.bean.ContactsEntity;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.sqlite.ETZContactsDataStore;
import com.easyetz.utils.MyLog;
import com.easyetz.view.MText;
import com.gyf.barlibrary.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactsActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView{
    @BindView(R.id.contacts_lv)
    RecyclerView lView;
    @BindView(R.id.contacts_no)
    MText contacts_no;
    private ContactsAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    public static final int ADD_CONTACTS = 116;
    public static final int SHOW_CONTACTS = 117;
    private int form = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_contacts;
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
        form = getIntent().getIntExtra("from", 0);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        lView.setLayoutManager(linearLayoutManager);
        adapter = new ContactsAdapter(R.layout.contacts_item,new ArrayList<>());
        lView.setAdapter(adapter);
        adapter.setOnItemClickListener( new BaseQuickAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ContactsEntity item = (ContactsEntity) adapter.getItem(position);
                if (form == 0) {
                    Intent intent = new Intent(ContactsActivity.this, ManageContactsActivity.class);
                    intent.putExtra("from", 2);
                    intent.putExtra("name", item.getCname());
                    intent.putExtra("address", item.getWalletAddress());
                    intent.putExtra("remarks", item.getRemarks());
                    startActivityForResult(intent, SHOW_CONTACTS);
                } else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", item.getWalletAddress());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }

        });

    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }


    public void setListItem() {
        List<ContactsEntity> list = ETZContactsDataStore.getInstance(this).queryAllContacts();
        MyLog.i("ContactsActivity=" + list.size());
        if (list.size() <= 0) {
            contacts_no.setVisibility(View.VISIBLE);
        } else {
            contacts_no.setVisibility(View.GONE);
        }

        adapter.setItems(list);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.contacts_back_btn, R.id.contacts_add_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.contacts_back_btn:
                onBackPressed();
                break;
            case R.id.contacts_add_btn:
                Intent intent = new Intent(this, ManageContactsActivity.class);
                intent.putExtra("from", 1);
                startActivityForResult(intent, ADD_CONTACTS);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_CONTACTS:
                break;
            case SHOW_CONTACTS:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setListItem();
    }
}
