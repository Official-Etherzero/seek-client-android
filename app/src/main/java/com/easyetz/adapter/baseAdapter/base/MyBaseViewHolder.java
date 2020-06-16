package com.easyetz.adapter.baseAdapter.base;

import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.view.View;

import com.chad.library.adapter.base.BaseViewHolder;

public class MyBaseViewHolder extends BaseViewHolder{
    public MyBaseViewHolder(View view) {
        super(view);
    }
    /**
     * Will set background color of a view.
     *
     * @param viewId The view id.
     * @param drawable
     * @return The BaseViewHolder for chaining.
     */
    public BaseViewHolder setBackgroundDrawable(@IdRes int viewId, Drawable drawable) {
        View view = getView(viewId);
        view.setBackground(drawable);
        return this;
    }
}
