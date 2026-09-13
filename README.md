# 嗡嗡嗡 · 悬浮窗宠物 APK（GitHub 云端构建）

**形态与电脑版一致**：蚊子悬浮在手机所有界面上飞（桌面、微信、浏览器上面都能看到它），
不是全屏页面。功能开关是可拖动的悬浮面板。

## 使用流程

1. 首次打开 App → 弹窗引导开启「显示在其它应用上层」权限（系统设置里找到嗡嗡嗡打开开关）
2. 返回后蚊子自动出现在屏幕上开始飞
3. **交互与电脑版相同**：
   - 点一下 = 戳（「嗯？」/「别闹…」，加好感）
   - 按住不动约 0.7 秒 = 摸头（开心动画 + AI 回应）
   - 按住约 1.5 秒 = **打开/关闭功能悬浮面板**
   - 拖着甩出去 = 扔飞（抱怨+坠落）
   - 1.2 秒内快速点三下 = 拍扁（变灰坠落，2 秒后伤心复活）
4. **悬浮面板**（可拖动）：💬 聊天（弹出输入条，AI 用凌九霄口吻回复）、
   ❤️ 好感度、🌙 勿扰（停住睡觉）、✖ 退出
5. AI 请求走安卓原生转发（无浏览器跨域限制），回复慢时显示「对方正在回应中...」

## 推送到 GitHub 触发云端编译

```
git remote add origin https://github.com/<你的用户名>/<私有仓库名>.git
git branch -M main
git push -u origin main
```

（仓库已在本地 init+commit 完；第一次 push 弹登录窗口用浏览器授权）

## 拿 APK

仓库页 → **Actions** → 等 2~4 分钟绿勾 → 点进运行 → 底部 **Artifacts** →
下载 `嗡嗡嗡手机版-APK` → 解压出 `app-debug.apk` → 传手机安装（允许未知来源）

## 结构

- `app/src/main/java/com/weng/weng/PetService.java` —— 悬浮窗本体（宠物/气泡/面板/输入条/AI）
- `app/src/main/java/com/weng/weng/MainActivity.java` —— 悬浮窗权限引导
- `app/src/main/res/drawable/*.png` —— 15 张帧图（与电脑版同源）
- `.github/workflows/build-apk.yml` —— 云端编译

## 注意

- key 与人设嵌在 PetService.java 里，**仓库务必设 Private**
- 省电策略激进的品牌（小米/华为）可能需要在设置里允许后台运行
