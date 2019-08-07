package com.map.mapmobility.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    private Context mContext;
    private static ToastUtils mToast;
    private ToastUtils(){}

    public static ToastUtils INSTANCE() {
        if(mToast == null) {
            synchronized (ToastUtils.class){
                if(mToast == null){
                    mToast = new ToastUtils();
                }
            }
        }
        return mToast;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void destory() {
        mContext = null;
        mToast = null;
    }

    public void Toast(String msg) {
        if(StringUtils.isJudgeEmpty(msg) && mContext != null){
            Toast.makeText(mContext.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
