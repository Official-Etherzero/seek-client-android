package com.easyetz.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.utils.MyLog;

public class LoadingDialog extends Dialog {
    private TextView conten;
    public LoadingDialog(Context context) {
        super(context, R.style.MyDialog);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.loading_dialog);
        this.conten=findViewById(R.id.loading_content);

    }

    @Override
    public void show() {
        try {
            super.show();
        }catch (Exception e){
            MyLog.i("************"+e.getMessage());
        }

    }
    public void setLoadingContent(String msg){
        conten.setText(msg);
    }
}
