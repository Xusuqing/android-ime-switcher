package com.imeswitcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

/**
 * 透明跳板 Activity：
 * Android 9+ 禁止从后台 Service 直接弹出输入法选择器，
 * 启动此无感知透明 Activity 获取焦点窗口后再弹选择器。
 *
 * 注意：不能在 onCreate 里紧跟 finish()，否则窗口销毁太快，
 * 选择器弹出前已失去焦点。改为 onStop() 时再关闭。
 */
public class PickerProxyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InputMethodManager imm =
            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showInputMethodPicker();
        }
        // 不在这里 finish()，等选择器弹出、Activity 退到后台后再关
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 用户选择完毕或按返回键后，Activity 进入 Stop 状态，此时关闭
        finish();
    }
}
