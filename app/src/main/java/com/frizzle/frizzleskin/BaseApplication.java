package com.frizzle.frizzleskin;

import android.app.Application;

import com.tpson.skinlibrary.SkinManager;

/**
 * author: LWJ
 * date: 2020/8/24$
 * description
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.inti(this);
    }
}
