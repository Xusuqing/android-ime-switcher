package com.imeswitcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.core.app.NotificationCompat;

public class FloatingBallService extends Service {

    public static boolean isRunning = false;

    private static final String CHANNEL_ID = "ime_switcher_channel";
    private static final int NOTIF_ID = 1;

    private WindowManager windowManager;
    private FrameLayout container;
    private EditText hiddenInput;
    private WindowManager.LayoutParams params;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private boolean isDragging = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());
        addFloatingBall();
    }

    private void addFloatingBall() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 300;

        // 容器：悬浮球图标 + 隐藏输入框（用于建立 InputConnection）
        container = new FrameLayout(this);

        View ball = LayoutInflater.from(this).inflate(R.layout.floating_ball, container, false);
        container.addView(ball);

        // 隐藏的 EditText，仅用于建立 InputConnection，对用户完全不可见
        hiddenInput = new EditText(this);
        hiddenInput.setAlpha(0f);
        hiddenInput.setWidth(1);
        hiddenInput.setHeight(1);
        container.addView(hiddenInput);

        windowManager.addView(container, params);

        ball.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    int dx = (int) (event.getRawX() - initialTouchX);
                    int dy = (int) (event.getRawY() - initialTouchY);
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) isDragging = true;
                    if (isDragging) {
                        params.x = initialX + dx;
                        params.y = initialY + dy;
                        windowManager.updateViewLayout(container, params);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isDragging) showPicker();
                    return true;
            }
            return false;
        });
    }

    private void showPicker() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) return;

        // 步骤 1：让浮窗变为可聚焦（允许接收输入焦点）
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(container, params);

        // 步骤 2：聚焦隐藏输入框，建立 InputConnection
        hiddenInput.requestFocus();
        imm.showSoftInput(hiddenInput, InputMethodManager.SHOW_IMPLICIT);

        // 步骤 3：延迟 150ms 弹选择器（等输入连接就绪）
        handler.postDelayed(() -> {
            imm.showInputMethodPicker();

            // 步骤 4：延迟 2s 后恢复 FLAG_NOT_FOCUSABLE
            // （选择器是系统对话框，会自行管理焦点，不需要我们主动监听关闭）
            handler.postDelayed(this::restoreFocus, 2000);
        }, 150);
    }

    private void restoreFocus() {
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (container.getWindowToken() != null) {
            windowManager.updateViewLayout(container, params);
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(hiddenInput.getWindowToken(), 0);
    }

    private Notification buildNotification() {
        Intent stopIntent = new Intent(this, FloatingBallService.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("输入法切换器")
            .setContentText("悬浮球运行中 · 通知栏可停止服务")
            .setSmallIcon(R.drawable.ic_keyboard)
            .setContentIntent(openPending)
            .addAction(0, "停止服务", stopPending)
            .setOngoing(true)
            .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) stopSelf();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
        if (container != null && windowManager != null) {
            windowManager.removeView(container);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "输入法切换器", NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("悬浮球服务保活通知");
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
