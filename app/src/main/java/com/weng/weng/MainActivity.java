package com.weng.weng;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

/** 入口：权限齐了→启动悬浮宠物并打开 App 内设置窗口 */
public class MainActivity extends Activity {

    private static final int REQ_OVERLAY = 1;
    private boolean grantedOnEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        grantedOnEntry = Settings.canDrawOverlays(this);
        if (grantedOnEntry) {
            startPetAndOpenSettings();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("嗡嗡嗡 needs a floating window permission")
                    .setMessage("Just like the desktop version flies over all your windows, the little mosquito needs the \"Display over other apps\" permission to fly over your phone screen.\n\nAfter clicking OK, find 嗡嗡嗡 in the list and enable the switch, and it will automatically fly back and open the settings for you.")
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
                startPetAndOpenSettings();
            } else {
                finish();
            }
        }
    }

    private void startPetAndOpenSettings() {
        startService(new Intent(this, PetService.class));
        startActivity(new Intent(this, SettingsActivity.class));
        finish();
    }
}
