package com.weng.weng;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** AI 小剧场二选一弹窗（全屏透明，漫画风两选项） */
public class TheaterActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String scene = getIntent().getStringExtra("scene");
        final String a = getIntent().getStringExtra("a");
        final String b2 = getIntent().getStringExtra("b");
        if (scene == null) {
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xE6F5F4EF);
        root.setBackground(bg);

        TextView title = new TextView(this);
        title.setText(Ico.s(this, "🎭 小剧场"));
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#111111"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sc = new TextView(this);
        sc.setText("「" + scene + "」");
        sc.setTextSize(15);
        sc.setTextColor(Color.parseColor("#222222"));
        sc.setPadding(0, dp(14), 0, dp(18));
        sc.setGravity(Gravity.CENTER);
        root.addView(sc);

        TextView btnA = big(a);
        btnA.setOnClickListener(v -> {
            if (PetService.instance != null) {
                String r = Extras.theaterResult(0, scene, a);
                PetService.instance.showBubbleMajor(r, 6000);
                PetService.instance.aiChat("小剧场：场景「" + scene + "」，用户选了「" + a + "」，结果" + r);
            }
            finish();
        });
        root.addView(btnA);
        root.addView(gap(12));
        TextView btnB = big(b2);
        btnB.setOnClickListener(v -> {
            if (PetService.instance != null) {
                String r = Extras.theaterResult(1, scene, b2);
                PetService.instance.showBubbleMajor(r, 6000);
                PetService.instance.aiChat("小剧场：场景「" + scene + "」，用户选了「" + b2 + "」，结果" + r);
            }
            finish();
        });
        root.addView(btnB);
        root.addView(gap(16));
        TextView skip = new TextView(this);
        skip.setText("（装没看见，滑走）");
        skip.setTextSize(12);
        skip.setTextColor(Color.parseColor("#999999"));
        skip.setGravity(Gravity.CENTER);
        skip.setPadding(dp(10), dp(8), dp(10), dp(8));
        skip.setOnClickListener(v -> finish());
        root.addView(skip);

        setContentView(root);
    }

    private TextView big(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(Color.parseColor("#111111"));
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(14));
        g.setStroke(dp(2), Color.parseColor("#111111"));
        t.setBackground(g);
        return t;
    }

    private android.view.View gap(int h) {
        android.view.View v = new android.view.View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h)));
        return v;
    }

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
