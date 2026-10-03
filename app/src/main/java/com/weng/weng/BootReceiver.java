package com.weng.weng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 开机自启（设置页可开关，默认关） */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 广播回调里抛任何异常都会让整个进程直接崩掉（而且此时崩溃处理器还没装上），
        // 所以整体兜底：自启失败最多是不自启，绝不能把 App 崩了。
        try {
            if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
            // 开机广播跑在全新进程里，只会创建这个 Receiver，不会创建 PetService；
            // 而 DataStore 的 SharedPreferences 只在 PetService.onCreate 里初始化过 ——
            // 这里不补一次 init，下面读配置就是 P == null 的空指针（旧版一开机就崩）。
            DataStore.init(context);
            if (!DataStore.getBool("bootAuto", false)) return;
            // 悬浮窗权限在手才启动（否则等用户自己打开 App）
            if (!android.provider.Settings.canDrawOverlays(context)) return;
            context.startService(new Intent(context, PetService.class));
        } catch (Throwable ignored) {
        }
    }
}
