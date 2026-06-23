# 输入法切换器 · 悬浮球

点一下悬浮球，立即弹出系统输入法选择器。小巧 APK，不依赖任何第三方自动化工具。

---

## 功能

- 后台常驻悬浮球（前台服务保活）
- 任意拖动位置
- 短按弹出系统输入法选择器
- 通知栏一键停止服务
- APK 体积约 200–400 KB

## 用 GitHub Actions 打包（全程不需要 Android Studio）

### 第一步：把代码上传到 GitHub

1. 登录 [github.com](https://github.com)，点右上角 **+** → **New repository**
2. 仓库名随意（如 `ime-switcher`），选 **Public**（免费），点 **Create repository**
3. 把 `android-ime-switcher/` 文件夹里的所有内容上传：
   - 方式 A：直接拖拽上传（在仓库页面点 **Add file → Upload files**，把文件夹里所有东西拖进去）
   - 方式 B：用 Git 命令行

> ⚠️ 上传时要保持目录结构，`.github/` 文件夹也必须上传。

### 第二步：等待自动构建

上传完成后，GitHub 会自动触发 Actions 构建。

- 点仓库顶部的 **Actions** 标签
- 点最新那条 workflow run（名字是 **Build APK**）
- 等待约 3–5 分钟，全绿即成功

### 第三步：下载 APK

构建成功后，在 Actions 页面底部 **Artifacts** 区域，点 **ImeSwitcher-release** 下载 ZIP，解压后即是 APK。

---

## 安装和使用

1. 把 APK 传到手机，允许"安装未知来源应用"后安装
2. 打开 App，点"**启动悬浮球**"
3. 弹出悬浮窗权限申请 → 找到"输入法切换器"→ 打开开关 → 返回
4. 再次点"**启动悬浮球**"，屏幕上出现蓝色键盘图标即成功
5. 在任意 App 中**短按蓝色悬浮球** → 系统输入法选择器弹出 → 选择目标输入法

---

## 常见问题

**Q：为什么每次都是弹出选择框，而不是直接切换？**

A：直接切换（无弹框）需要 `adb shell pm grant com.imeswitcher android.permission.WRITE_SECURE_SETTINGS` 命令，需要连接电脑执行一次。若不想连电脑，弹选择框是最简洁的免 root 方案。

**Q：手机重启后悬浮球消失了？**

A：在手机"自启动管理"里允许本应用自启动即可（部分国产 ROM 默认禁止）。

**Q：MIUI/ColorOS 等系统悬浮球不显示？**

A：在系统"悬浮窗权限"设置里手动允许本应用。

---

## 项目结构

```
app/src/main/
├── java/com/imeswitcher/
│   ├── MainActivity.java       # 主界面 · 权限申请 · 服务控制
│   ├── FloatingBallService.java # 前台服务 · 悬浮球创建与拖动
│   └── ImeUtils.java           # 输入法工具类
├── res/
│   ├── layout/activity_main.xml
│   ├── layout/floating_ball.xml
│   └── drawable/…
└── AndroidManifest.xml
```
