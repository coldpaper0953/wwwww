package com.weng.weng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 开机自启（设置页可开关，默认关） */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        if (!DataStore.getBool("bootAuto", false)) return;
        // 悬浮窗权限在手才启动（否则等用户自己打开 App）
        if (!android.provider.Settings.canDrawOverlays(context)) return;
        context.startService(new Intent(context, PetService.class));
    }
}
