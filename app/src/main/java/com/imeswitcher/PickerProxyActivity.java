package com.imeswitcher;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import java.util.List;

/**
 * 透明跳板 Activity：
 * 从后台服务无法直接弹出输入法选择器（Android 9+ 限制），
 * 通过启动此透明 Activity 来获取焦点窗口，再弹选择器。
 */
public class PickerProxyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showPicker();
    }

    private void showPicker() {
        InputMethodManager imm =
            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        List<InputMethodInfo> imes = imm.getEnabledInputMethodList();

        if (imes == null || imes.size() <= 1) {
            // 只有一个输入法，直接打开输入法设置
            startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
            finish();
            return;
        }

        // 构建选择列表
        String[] names = new String[imes.size()];
        for (int i = 0; i < imes.size(); i++) {
            names[i] = imes.get(i).loadLabel(getPackageManager()).toString();
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("切换输入法")
            .setItems(names, (dialog, which) -> {
                String imeId = imes.get(which).getId();
                // 切换输入法（需 WRITE_SECURE_SETTINGS 权限）
                // 若无此权限，退回到系统选择器
                try {
                    android.provider.Settings.Secure.putString(
                        getContentResolver(),
                        android.provider.Settings.Secure.DEFAULT_INPUT_METHOD,
                        imeId
                    );
                } catch (SecurityException e) {
                    // 无 WRITE_SECURE_SETTINGS 权限时，用系统选择器兜底
                    imm.showInputMethodPicker();
                }
                finish();
            })
            .setOnDismissListener(d -> finish())
            .show();
    }
}
