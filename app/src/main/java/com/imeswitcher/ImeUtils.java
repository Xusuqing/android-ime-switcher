package com.imeswitcher;

import android.content.Context;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import java.util.List;

public class ImeUtils {

    /**
     * 弹出系统输入法选择器（不需要任何额外权限）
     */
    public static void showImePicker(Context context) {
        InputMethodManager imm =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showInputMethodPicker();
        }
    }

    /**
     * 获取已安装的所有输入法列表
     */
    public static List<InputMethodInfo> getEnabledImes(Context context) {
        InputMethodManager imm =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.getEnabledInputMethodList();
        }
        return null;
    }
}
