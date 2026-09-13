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
        t.setText("嗡嗡嗡崩溃了！把这段截图发回去即可：\n\n" + stack);
        sc.addView(t);
        setContentView(sc);
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
                    fos.write((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                            + "\n" + stack + "\n\n").getBytes("UTF-8"));
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
