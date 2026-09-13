package com.weng.weng;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

/** 权限入口：申请"显示在其它应用上层"权限后启动悬浮窗服务并退出自身界面 */
public class MainActivity extends Activity {

    private static final int REQ_OVERLAY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Settings.canDrawOverlays(this)) {
            startPet();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("嗡嗡嗡 needs a floating window permission")
                    .setMessage("Just like the desktop version flies over all your windows, the little mosquito needs the \"Display over other apps\" permission to fly over your phone screen.\n\nAfter clicking OK, find 嗡嗡嗡 in the list and enable the switch, then it will automatically fly back.")
                    .setPositiveButton("Go enable", (d, w) -> {
                        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(i, REQ_OVERLAY);
                    })
                    .setNegativeButton("Cancel", (d, w) -> finish())
                    .setOnCancelListener(d -> finish())
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                startPet();
            } else {
                finish();
            }
        }
    }

    private void startPet() {
        startService(new Intent(this, PetService.class));
        moveTaskToBack(true);
    }
}
