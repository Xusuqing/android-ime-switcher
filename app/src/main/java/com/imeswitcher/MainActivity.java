package com.imeswitcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvStatus = findViewById(R.id.tv_status);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        Button btnImeSettings = findViewById(R.id.btn_ime_settings);

        btnStart.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                );
                startActivityForResult(intent, REQUEST_OVERLAY);
                Toast.makeText(this, "请授予「悬浮窗」权限后再点击启动", Toast.LENGTH_LONG).show();
            } else {
                startFloatingService();
                tvStatus.setText("悬浮球运行中 ✓");
                this.finish();
            }
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, FloatingBallService.class));
            tvStatus.setText("已停止");
        });

        btnImeSettings.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
        });

        if (FloatingBallService.isRunning) {
            tvStatus.setText("悬浮球运行中 ✓");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                startFloatingService();
                TextView tvStatus = findViewById(R.id.tv_status);
                tvStatus.setText("悬浮球运行中 ✓");
                this.finish();
            } else {
                Toast.makeText(this, "未授权，无法启动悬浮球", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startFloatingService() {
        Intent intent = new Intent(this, FloatingBallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}
