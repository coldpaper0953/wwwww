package com.weng.weng;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** App 内设置窗口：聊天、好感度、勿扰、版本更新、退出（悬浮面板的替代品） */
public class SettingsActivity extends Activity {

    private TextView affLabel, verLabel, chatLog;
    private EditText input;
    private TextView dndBtn;
    private ScrollView scroller;

    private final Runnable uiRefresher = new Runnable() {
        @Override
        public void run() {
            refresh();
            if (PetService.instance != null) {
                PetService.instance.handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(20));
        root.setBackgroundColor(Color.parseColor("#F5F4EF"));

        TextView title = new TextView(this);
        title.setText("嗡嗡嗡");
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#111111"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        affLabel = styledText(14, "#555555", Gravity.CENTER);
        root.addView(affLabel);
        verLabel = styledText(12, "#999999", Gravity.CENTER);
        root.addView(verLabel);

        root.addView(gap(12));

        // 聊天记录区
        chatLog = new TextView(this);
        chatLog.setTextSize(13);
        chatLog.setTextColor(Color.parseColor("#111111"));
        chatLog.setLineSpacing(dp(3), 1f);
        chatLog.setText("（还没聊过天，下面说句话吧）");
        scroller = new ScrollView(this);
        scroller.addView(chatLog);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        clp.setMargins(0, dp(6), 0, dp(6));
        root.addView(scroller, clp);

        // 输入行
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        input = new EditText(this);
        input.setHint("跟它说点什么…");
        input.setTextSize(14);
        input.setMaxLines(1);
        input.setBackgroundColor(Color.WHITE);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable ib = new GradientDrawable();
        ib.setColor(Color.WHITE);
        ib.setCornerRadius(dp(10));
        ib.setStroke(dp(2), Color.parseColor("#111111"));
        input.setBackground(ib);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(input, ilp);
        row.addView(gapW(8));
        TextView send = button("发送");
        send.setOnClickListener(v -> {
            String s = input.getText().toString().trim();
            if (s.isEmpty() || PetService.instance == null) return;
            input.setText("");
            PetService.instance.userChat(s);
            refresh();
        });
        row.addView(send);
        root.addView(row);

        root.addView(gap(12));

        // 功能按钮行
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        dndBtn = button("🌙 勿扰");
        dndBtn.setOnClickListener(v -> {
            if (PetService.instance != null) {
                PetService.instance.toggleDnd();
                refresh();
            }
        });
        row2.addView(dndBtn);
        row2.addView(gapW(8));
        TextView upd = button("🔄 检查更新");
        upd.setOnClickListener(v -> {
            if (PetService.instance != null) {
                Toast.makeText(this, "检查更新中…", Toast.LENGTH_SHORT).show();
                PetService.instance.checkUpdate();
            }
        });
        row2.addView(upd);
        row2.addView(gapW(8));
        TextView quit = button("✖ 退出");
        quit.setOnClickListener(v -> {
            if (PetService.instance != null) PetService.instance.stopSelf();
            finish();
        });
        row2.addView(quit);
        root.addView(row2);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        uiRefresher.run();
    }

    @Override
    protected void onPause() {
        if (PetService.instance != null) PetService.instance.handler.removeCallbacks(uiRefresher);
        super.onPause();
    }

    private void refresh() {
        if (PetService.instance == null) {
            affLabel.setText("宠物未运行");
            return;
        }
        affLabel.setText("❤️ 好感度 " + PetService.instance.affection + "　·　第 " + PetService.instance.daysCount() + " 天");
        verLabel.setText("v" + PetService.instance.curVersion() + (PetService.instance.dnd ? "（勿扰中）" : ""));
        dndBtn.setText(PetService.instance.dnd ? "🌙 勿扰中" : "🌙 勿扰");
        String log = PetService.instance.chatLogText();
        if (!log.isEmpty()) {
            chatLog.setText(log);
            scroller.post(() -> scroller.fullScroll(ScrollView.FOCUS_DOWN));
        }
    }

    private TextView styledText(int size, String color, int gravity) {
        TextView t = new TextView(this);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor(color));
        t.setGravity(gravity);
        return t;
    }

    private TextView button(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.parseColor("#111111"));
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(12));
        g.setStroke(dp(2), Color.parseColor("#111111"));
        t.setBackground(g);
        return t;
    }

    private View gap(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h)));
        return v;
    }

    private View gapW(int w) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(w), 1));
        return v;
    }

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
