package com.example.wildcard.androidkeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author : Wells
 */
public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    private WebView mWebView;
    private EditText inputText;
    private RelativeLayout layoutMain;
    private Context mContext;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        mGestureDetector = new GestureDetector(mContext, this);
        mWebView = (WebView) findViewById(R.id.webview);
        inputText = (EditText) findViewById(R.id.input_text);
        layoutMain = (RelativeLayout) findViewById(R.id.layout_main);
        layoutMain.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) mContext
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(layoutMain.getWindowToken(), 0);
                }
            }
        });

        initWebview();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebview() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportZoom(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setBlockNetworkImage(true);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.loadUrl("http://zhangliwei.date");
    }
    // 状态栏的高度
    private int statusBarHeight;
    // 软键盘的高度
    private int keyboardHeight;

    // 软键盘的显示状态
    private boolean isShowKeyboard;
    // 最小键盘高度
    private int minKeyboardHeight = 150;

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {
            // 应用可以显示的区域。此处包括应用占用的区域，
            // 以及ActionBar和状态栏，但不含设备底部的虚拟按键。
            Rect r = new Rect();
            layoutMain.getWindowVisibleDisplayFrame(r);
            statusBarHeight= getStatusBarHeight(mContext);
            // 屏幕高度。这个高度不含虚拟按键的高度
            int screenHeight = layoutMain.getRootView().getHeight();
            int heightDiff = screenHeight - (r.bottom - r.top);

            // 在不显示软键盘时，heightDiff等于状态栏的高度
            // 在显示软键盘时，heightDiff会变大，等于软键盘加状态栏的高度。
            // 所以heightDiff大于状态栏高度时表示软键盘出现了，
            // 这时可算出软键盘的高度，即heightDiff减去状态栏的高度
            if (keyboardHeight == 0 && heightDiff > statusBarHeight) {
                keyboardHeight = heightDiff - statusBarHeight;
            }

            if (isShowKeyboard) {
                // 如果软键盘是弹出的状态，并且heightDiff小于等于状态栏高度，
                // 说明这时软键盘已经收起
                if (heightDiff - statusBarHeight < minKeyboardHeight) {
                    isShowKeyboard = false;
                    Toast.makeText(mContext,"键盘隐藏了",Toast.LENGTH_SHORT).show();
                }
            } else {
                // 如果软键盘是收起的状态，并且heightDiff大于状态栏高度，
                // 说明这时软键盘已经弹出
                if (heightDiff - statusBarHeight > minKeyboardHeight) {
                    isShowKeyboard = true;
                    Toast.makeText(mContext,"键盘显示了",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        if (Math.abs(e2.getY() - e1.getY()) > getScreenHeight(mContext) / 10 && Math.abs(velocityX) > 10) {
            if (isShowKeyboard) {
                closeInputMethod(mContext);
            }
        }
        float r= mWebView.getHeight();
        float webViewContentHeight = mWebView.getContentHeight()*mWebView.getScale();
        //WebView的现高度
        float webViewCurrentHeight = (mWebView.getHeight() + mWebView.getScrollY());

        if (!isShowKeyboard && e1.getY() - e2.getY() > getScreenHeight(mContext)  / 10 && webViewContentHeight - webViewCurrentHeight <= 10) {

            popInputMethod(inputText, mContext);
        }
        return false;
    }
/*
    用到的utils
 */
    public static int getScreenHeight(Context mContext){
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        return dm.heightPixels-getStatusBarHeight(mContext);
    }

    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static void popInputMethod(final EditText edittext, final Context activity) {
        edittext.requestFocus(); // edittext是一个EditText控件
        Timer timer = new Timer(); // 设置定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() { // 弹出软键盘的代码
                InputMethodManager imm = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edittext, InputMethodManager.RESULT_SHOWN);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        }, 200); // 设置200毫秒的时长

    }
    private static InputMethodManager imm;
    public static void closeInputMethod(Context act){
        try {
            if (null == imm) {
                imm = (InputMethodManager) act
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
            }
            if (imm.isActive()) {
                View view = ((Activity) act).getCurrentFocus();
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
