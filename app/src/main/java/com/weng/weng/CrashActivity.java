package com.weng.weng;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 崩溃兜底：把异常栈写到外部存储 crash_log.txt 并显示出来（可截图发回） */
public class CrashActivity extends Activity {

    /** 源代码 / 问题反馈地址：连崩溃日志一起带上，方便直接定位到仓库与版本 */
    private static final String SOURCE_URL = "https://github.com/" + Edition.GITHUB_REPO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String stack = getIntent().getStringExtra("stack");
        if (stack == null) stack = "无崩溃记录";

        ScrollView sc = new ScrollView(this);
        sc.setBackgroundColor(Color.parseColor("#101418"));
        TextView t = new TextView(this);
        t.setTextColor(Color.parseColor("#FF7060"));
        t.setTextSize(11);
        t.setPadding(24, 24, 24, 24);
        t.setText(getString(R.string.app_name) + "崩溃了！把这段截图发回去即可\n"
                + "版本 v" + versionName() + "\n"
                + "源代码 / 反馈：" + SOURCE_URL + "\n\n" + stack);
        sc.addView(t);
        setContentView(sc);
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "?";
        }
    }

    public static void install(final android.content.Context app) {
        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                String stack = sw.toString();
                File dir = app.getExternalFilesDir(null);
                if (dir != null) {
                    FileOutputStream fos = new FileOutputStream(new File(dir, "crash_log.txt"), true);
                    String head;
                    try {
                        head = app.getString(R.string.app_name) + " v"
                                + app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName;
                    } catch (Exception e2) {
                        head = app.getString(R.string.app_name);
                    }
                    fos.write((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                            + "  " + head + "\n"
                            + "源代码 / 反馈：" + SOURCE_URL
                            + "　（把这段日志连同上面的版本号一起发过去即可定位）\n"
                            + stack + "\n\n").getBytes("UTF-8"));
                    fos.close();
                }
                Intent i = new Intent(app, CrashActivity.class);
                i.putExtra("stack", stack);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                app.startActivity(i);
            } catch (Exception ignored) {}
            if (prev != null) prev.uncaughtException(thread, throwable);
            else System.exit(2);
        });
    }
}
