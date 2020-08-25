package com.frizzle.frizzleskin;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.tpson.skinlibrary.base.SkinActivity;
import com.tpson.skinlibrary.utils.PreferencesUtils;

import java.io.File;

public class MainActivity extends SkinActivity {

    private String skinPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //com.tpson.skinpackages
        skinPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "frizzle.skin";

        // 运行时权限申请（6.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 200);
            }
        }
        if (("frizzle").equals(PreferencesUtils.getString(this, "currentSkin"))) {
            skinDynamic(skinPath, R.color.skin_item_color);
        } else {
            defaultSkin(R.color.colorPrimary);
        }
    }

    @Override
    protected boolean openChangeSkin() {
        return true;
    }

    // 换肤按钮（api限制：5.0版本）
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void skinDynamic(View view) {
        // 真实项目中：需要先判断当前皮肤，避免重复操作！
        if (!("frizzle").equals(PreferencesUtils.getString(this, "currentSkin"))) {
            Log.e("frizzle >>> ", "-------------start-------------");
            long start = System.currentTimeMillis();

            skinDynamic(skinPath, R.color.skin_item_color);
            PreferencesUtils.putString(this, "currentSkin", "frizzle");

            long end = System.currentTimeMillis() - start;
            Log.e("frizzle >>> ", "换肤耗时（毫秒）：" + end);
            Log.e("frizzle >>> ", "-------------end---------------");
        }
    }

    // 默认按钮（api限制：5.0版本）
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void skinDefault(View view) {
        if (!("default").equals(PreferencesUtils.getString(this, "currentSkin"))) {
            Log.e("frizzle >>> ", "-------------start-------------");
            long start = System.currentTimeMillis();

            defaultSkin(R.color.colorPrimary);
            PreferencesUtils.putString(this, "currentSkin", "default");

            long end = System.currentTimeMillis() - start;
            Log.e("frizzle >>> ", "还原耗时（毫秒）：" + end);
            Log.e("frizzle >>> ", "-------------end---------------");
        }
    }

    // 跳转按钮
    public void jumpSelf(View view) {
        startActivity(new Intent(this, this.getClass()));
    }
}