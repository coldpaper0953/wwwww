# 嗡嗡嗡 · APK 云端构建工程

把这个文件夹推到 GitHub，Actions 会自动编译出 APK——**你本机不需要装任何安卓工具**。

## 推送步骤（三条命令）

在 `嗡嗡嗡_APK工程` 目录下打开终端，逐条执行：

```
git init
git add .
git commit -m "嗡嗡嗡手机版"
git branch -M main
git remote add origin https://github.com/<你的用户名>/<仓库名>.git
git push -u origin main
```

（没有仓库就先在 github.com 右上角 + → New repository 建一个空的，不要勾选任何初始化选项。
第一次 push 会弹登录窗口，用浏览器授权即可。）

## 拿 APK

1. 打开你的仓库页面 → 顶部 **Actions** 标签
2. 等左边那次运行转完绿勾（约 2~4 分钟）
3. 点进那次运行 → 底部 **Artifacts** → 下载 `嗡嗡嗡手机版-APK`
4. 解压出 `app-debug.apk` 传到手机安装（需允许"安装未知应用"）

## 结构

- `app/src/main/assets/index.html` —— 宠物本体（桌面手机版同一份，含人设和 key）
- `app/src/main/java/com/weng/weng/MainActivity.java` —— WebView 壳 + JS 桥
  （`file://` 页面里的 AI 请求经 `AndroidBridge.apiPost` 原生转发，不带 Origin 头，绕开 tokenrouter 的跨域封锁）
- `.github/workflows/build-apk.yml` —— 云端编译配置

## 注意

- key 和人设嵌在 index.html 和 MainActivity.java 里，**仓库请设为 Private**
- 以后想改人设/换 key：改 assets/index.html 里对应字段后 push，APK 自动重编
