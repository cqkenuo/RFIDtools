package cn.rrg.rdv.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.dxl.common.interfaces.OnTouchListener;
import cn.dxl.common.util.DisplayUtil;
import cn.dxl.common.util.HexUtil;
import cn.dxl.common.util.LanguageUtil;
import cn.dxl.common.util.StatusBarUtil;
import cn.dxl.common.widget.ToastUtil;
import cn.dxl.common.util.VibratorUtils;
import cn.dxl.mifare.NfcTagListenUtils;
import cn.dxl.mifare.StdMifareIntent;
import cn.rrg.rdv.R;
import cn.rrg.rdv.application.Properties;
import cn.rrg.rdv.util.Commons;

/**
 * Created by DXL on 2017/10/26.
 */
public abstract class BaseActivity
        extends AppCompatActivity implements NfcTagListenUtils.OnNewTagListener {

    protected String LOG_TAG = this.getClass().getSimpleName();
    protected Context context = this;
    protected Activity activity = this;

    private StdMifareIntent mStdMfUtil = null;

    private ArrayList<OnTouchListener> onTouchListeners;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Act is create");
        //实例化一个标准NFC设备工具对象!
        mStdMfUtil = new StdMifareIntent(this);
        //添加全局的标签状态监听!
        NfcTagListenUtils.addListener(this);
        //建立事件观察数列!
        onTouchListeners = new ArrayList<>();

        // 在onCreate()也要设置一下语言，有可能attachBaseContext()不生效。
        LanguageUtil.setAppLanguage(this, Commons.getLanguage());
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setStatus(!DisplayUtil.isDarkModeStatus(this));
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatus(!DisplayUtil.isDarkModeStatus(this));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setStatus(!DisplayUtil.isDarkModeStatus(this));
    }

    public void setStatus(boolean darkMode) {
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, darkMode)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
        StatusBarUtil.setLightNavigationBar(this, darkMode);
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "Act is start");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "Act is pause");
        super.onPause();
        /*
         * 在这里解注册各大前台事件!
         * */
        mStdMfUtil.disableForegroundDispatch(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String language = Commons.getLanguage();
        if (language.equals("auto")) {
            //如果value = auto，则设置为跟随系统!
            super.attachBaseContext(newBase);
        } else {
            //否则国际化!
            super.attachBaseContext(LanguageUtil.setAppLanguage(newBase, language));
        }
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "Act is resume");
        super.onResume();
        /*
         * 在这里注册各大前台事件
         * */
        mStdMfUtil.enableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //有新的意图时会启动
        Bundle data = intent.getExtras();
        if (data != null) {
            Tag tag = data.getParcelable(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                //存入全局操作域!
                NfcTagListenUtils.setTag(tag);
                //显示 UID！
                ToastUtil.show(this, getString(R.string.tips_uid) + HexUtil.toHexString(tag.getId()), true);
                //震动一下!
                VibratorUtils.runOneAsDelay(context, 1000);
                NfcTagListenUtils.notifyOnNewTag(tag);
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Log.d(LOG_TAG, "观察者数量: " + onTouchListeners.size());
        //在act获得了event的时候回调!
        if (onTouchListeners.size() == 0) return super.dispatchTouchEvent(ev);
        for (OnTouchListener listener : onTouchListeners) {
            // 进行相关的事件下发!
            if (listener != null) {
                //Log.d(LOG_TAG, "观察者: " + listener.toString());
                try {
                    listener.onTouch(ev);
                } catch (Exception ignored) {
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void addTouchListener(OnTouchListener listener) {
        //Log.d(LOG_TAG, "添加观察者: " + listener.toString());
        onTouchListeners.add(listener);
    }

    public void removeTouchListener(OnTouchListener listener) {
        //Log.d(LOG_TAG, "移除观察者: " + listener.toString());
        onTouchListeners.remove(listener);
    }

    @Override
    protected void onStop() {
        //Log.d(LOG_TAG, "Act is stop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //Log.d(LOG_TAG, "Act is destory");
        super.onDestroy();
        NfcTagListenUtils.removeListener(this);
    }

    @Override
    public void onNewTag(Tag tag) {

    }
}
