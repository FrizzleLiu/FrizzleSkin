package com.tpson.skinlibrary;

import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.tpson.skinlibrary.model.SkinCache;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * author: LWJ
 * date: 2020/8/24$
 * description
 */
public class SkinManager {
    private static SkinManager instance;
    private Application application;
    private Resources appResources;//app内置的资源
    private Resources skinResources;//皮肤包资源
    private String skinPackageName;//皮肤包所在包名
    private Map<String, SkinCache> cacheSkin;
    private boolean isDefaultSkin = true;//默认皮肤包(app内置)

    public boolean isDefaultSkin() {
        return isDefaultSkin;
    }

    private static final String ADD_ASSET_PATH = "addAssetPath"; //方法名

    private SkinManager(Application application) {
        this.application = application;
        this.appResources = application.getResources();
        cacheSkin = new HashMap<>();
    }

    public static void inti(Application application) {
        if (instance == null) {
            synchronized (SkinManager.class) {
                instance = new SkinManager(application);
            }
        }
    }

    public static SkinManager getInstance() {
        return instance;
    }


    /**
     * @param skinPath 加载皮肤包资源
     *                 如为空加载app内置资源
     */
    public void loadResource(String skinPath) {
        // 优化：如果没有皮肤包或者没做换肤动作，方法不执行直接返回！
        if (TextUtils.isEmpty(skinPath)) {
            isDefaultSkin = true;
            return;
        }

        // 优化：app冷启动、热启动可以取缓存对象
        if (cacheSkin.containsKey(skinPath)) {
            isDefaultSkin = false;
            SkinCache skinCache = cacheSkin.get(skinPath);
            if (null != skinCache) {
                skinResources = skinCache.getSkinResources();
                skinPackageName = skinCache.getSkinPackageName();
                return;
            }
        }
        //这种方式并不能实例化只能拿到对象 ,所以使用下面的放射方式
        //AssetManager assets = application.getAssets();
        try {
            //创建资源管理器
            AssetManager assetManager = AssetManager.class.newInstance();
            //方法被@hide 只能通过反射去拿
            // 如果担心@hide限制，可以反射addAssetPathInternal()方法，参考源码366行 + 387行
            Method addAssetPath = assetManager.getClass().getDeclaredMethod(ADD_ASSET_PATH, String.class);
            //设置私有方法可访问
            addAssetPath.setAccessible(true);
            //执行addAssetPath方法将外面的皮肤包加载到本应用(比如:com.frizzle.skinpackages)文件Resource
            addAssetPath.invoke(assetManager, skinPath);
            //创建加载外部皮肤包资源文件
            //本应用已经初始化,通过本应用的资源文件初始化外面需要加载的资源文件
            skinResources = new Resources(assetManager, appResources.getDisplayMetrics(), appResources.getConfiguration());
            //根据皮肤包文件获取包名
            skinPackageName = application.getPackageManager().getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES).packageName;
            //获取不到皮肤包的包名,加载内置皮肤
            isDefaultSkin = TextUtils.isEmpty(skinPackageName);
            if (!isDefaultSkin) {
                cacheSkin.put(skinPath, new SkinCache(skinResources, skinPackageName));
            }
            Log.e("SkinPackageName: ", skinPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            isDefaultSkin = true;
        }
    }

    /**
     * 通过资源的ID值获取Name和Type
     * 参考resource.arsc
     *
     * @param resourceId 资源id值 app内置的
     * @return 如果皮肤包没有资源id加载内置, 负责加载皮肤包
     */
    private int getSkinResourceIds(int resourceId) {
        if (isDefaultSkin) {
            return resourceId;
        }
        String resourceName = appResources.getResourceEntryName(resourceId);//比如: textview_bg
        String resourceType = appResources.getResourceTypeName(resourceId);//比如: drawable
        //动态获取皮肤包的资源id
        int skinResourceId = 0;
        if (skinResources != null) {
            skinResourceId = skinResources.getIdentifier(resourceName, resourceType, skinPackageName);
        }
        isDefaultSkin = skinResourceId == 0;
        return skinResourceId == 0 ? resourceId : skinResourceId;
    }

    public int getColor(int resourceId) {
        int ids = getSkinResourceIds(resourceId);
        return isDefaultSkin ? appResources.getColor(ids) : skinResources.getColor(ids);
    }

    public ColorStateList getColorStateList(int resourceId) {
        int ids = getSkinResourceIds(resourceId);
        return isDefaultSkin ? appResources.getColorStateList(ids) : skinResources.getColorStateList(ids);
    }

    // mipmap和drawable统一用法（待测）
    public Drawable getDrawableOrMipMap(int resourceId) {
        int ids = getSkinResourceIds(resourceId);
        return isDefaultSkin ? appResources.getDrawable(ids) : skinResources.getDrawable(ids);
    }

    public String getString(int resourceId) {
        int ids = getSkinResourceIds(resourceId);
        return isDefaultSkin ? appResources.getString(ids) : skinResources.getString(ids);
    }

    // 返回值特殊情况：可能是color / drawable / mipmap
    public Object getBackgroundOrSrc(int resourceId) {
        // 需要获取当前属性的类型名Resources.getResourceTypeName(resourceId)再判断
        String resourceTypeName = appResources.getResourceTypeName(resourceId);
        switch (resourceTypeName) {
            case "color":
                return getColor(resourceId);
            case "mipmap": // drawable / mipmap
            case "drawable":
                return getDrawableOrMipMap(resourceId);
        }
        return null;
    }

    // 获得字体
    public Typeface getTypeface(int resourceId) {
        // 通过资源ID获取资源path，参考：resources.arsc资源映射表
        String skinTypefacePath = getString(resourceId);
        // 路径为空，使用系统默认字体
        if (TextUtils.isEmpty(skinTypefacePath)) return Typeface.DEFAULT;
        return isDefaultSkin ? Typeface.createFromAsset(appResources.getAssets(), skinTypefacePath)
                : Typeface.createFromAsset(skinResources.getAssets(), skinTypefacePath);
    }

}
