package com.weng.weng;

import android.app.Activity;
import android.app.AlertDialog;
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

/** App 内设置窗口：除人设（内置加密锁定）外全部可手动编辑 */
public class SettingsActivity extends Activity {

    private TextView affLabel, verLabel, chatLog, speedLabel, dndBtn, affVal;
    private EditText input;
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
        root.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.setBackgroundColor(Color.parseColor("#F5F4EF"));

        TextView title = new TextView(this);
        title.setText("嗡嗡嗡 · 设置");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#111111"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        affLabel = small("#555555", Gravity.CENTER);
        root.addView(affLabel);
        verLabel = small("#999999", Gravity.CENTER);
        root.addView(verLabel);
        root.addView(gap(10));

        // ============ 聊天卡片 ============
        root.addView(cardLabel("💬 聊天"));
        LinearLayout chatCard = card();
        chatLog = new TextView(this);
        chatLog.setTextSize(13);
        chatLog.setTextColor(Color.parseColor("#111111"));
        chatLog.setLineSpacing(dp(3), 1f);
        chatLog.setText("（还没聊过天）");
        scroller = new ScrollView(this);
        scroller.addView(chatLog);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        chatCard.addView(scroller, clp);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        input = new EditText(this);
        input.setHint("跟它说点什么…");
        input.setTextSize(14);
        input.setMaxLines(1);
        input.setPadding(dp(12), dp(9), dp(12), dp(9));
        input.setBackground(box());
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
        chatCard.addView(row);
        root.addView(chatCard);
        root.addView(gap(10));

        // ============ 宠物设置卡片 ============
        root.addView(cardLabel("🐝 宠物设置"));
        LinearLayout petCard = card();

        petCard.addView(rowLabel("飞行速度"));
        LinearLayout speedRow = new LinearLayout(this);
        speedRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView slower = button("－");
        slower.setOnClickListener(v -> {
            if (PetService.instance != null) {
                PetService.instance.setSpeedMul(PetService.instance.getSpeedMul() - 0.2f);
                refresh();
            }
        });
        speedRow.addView(slower);
        speedLabel = new TextView(this);
        speedLabel.setTextSize(14);
        speedLabel.setTypeface(Typeface.DEFAULT_BOLD);
        speedLabel.setTextColor(Color.parseColor("#111111"));
        speedLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        speedRow.addView(speedLabel, slp);
        TextView faster = button("＋");
        faster.setOnClickListener(v -> {
            if (PetService.instance != null) {
                PetService.instance.setSpeedMul(PetService.instance.getSpeedMul() + 0.2f);
                refresh();
            }
        });
        speedRow.addView(faster);
        petCard.addView(speedRow);
        petCard.addView(gap(8));

        petCard.addView(rowLabel("好感度（可手动改）"));
        LinearLayout affRow = new LinearLayout(this);
        affRow.setOrientation(LinearLayout.HORIZONTAL);
        affVal = new TextView(this);
        affVal.setTextSize(14);
        affVal.setTypeface(Typeface.DEFAULT_BOLD);
        affVal.setTextColor(Color.parseColor("#111111"));
        affVal.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams avp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        affRow.addView(affVal, avp);
        TextView editAff = button("✏️ 修改");
        editAff.setOnClickListener(v -> editAffectionDialog());
        affRow.addView(editAff);
        petCard.addView(affRow);
        petCard.addView(gap(8));

        petCard.addView(rowLabel("模式"));
        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        dndBtn = button("🌙 勿扰");
        dndBtn.setOnClickListener(v -> {
            if (PetService.instance != null) {
                PetService.instance.toggleDnd();
                refresh();
            }
        });
        modeRow.addView(dndBtn);
        modeRow.addView(gapW(8));
        TextView persona = button("🔒 人设已锁定");
        persona.setOnClickListener(v ->
                Toast.makeText(this, "核心人设已内置加密保护，无法查看或修改", Toast.LENGTH_LONG).show());
        modeRow.addView(persona);
        petCard.addView(modeRow);
        root.addView(petCard);
        root.addView(gap(10));

        // ============ 更新与关于卡片 ============
        root.addView(cardLabel("⚙️ 更新与关于"));
        LinearLayout aboutCard = card();
        LinearLayout aboutRow = new LinearLayout(this);
        aboutRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView upd = button("🔄 检查更新");
        upd.setOnClickListener(v -> {
            if (PetService.instance != null) {
                Toast.makeText(this, "检查更新中…", Toast.LENGTH_SHORT).show();
                PetService.instance.checkUpdate();
            }
        });
        aboutRow.addView(upd);
        aboutRow.addView(gapW(8));
        TextView quit = button("✖ 退出");
        quit.setOnClickListener(v -> {
            if (PetService.instance != null) PetService.instance.stopSelf();
            finish();
        });
        aboutRow.addView(quit);
        aboutCard.addView(aboutRow);
        aboutCard.addView(gap(6));
        TextView about = small("#AAAAAA", Gravity.CENTER);
        about.setText("嗡嗡嗡手机版 · MADE by芬芳小鼠 · 还原版");
        aboutCard.addView(about);
        root.addView(aboutCard);

        ScrollView page = new ScrollView(this);
        page.addView(root);
        setContentView(page);
    }

    private void editAffectionDialog() {
        if (PetService.instance == null) return;
        final EditText in = new EditText(this);
        in.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        in.setText(String.valueOf(PetService.instance.affection));
        new AlertDialog.Builder(this)
                .setTitle("修改好感度")
                .setView(in)
                .setPositiveButton("确定", (d, w) -> {
                    try {
                        PetService.instance.setAffection(Integer.parseInt(in.getText().toString().trim()));
                        refresh();
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("取消", null)
                .show();
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
        speedLabel.setText("×" + String.format(java.util.Locale.US, "%.1f", PetService.instance.getSpeedMul()));
        if (affVal != null) affVal.setText(String.valueOf(PetService.instance.affection));
    }

    private TextView small(String color, int gravity) {
        TextView t = new TextView(this);
        t.setTextSize(12);
        t.setTextColor(Color.parseColor(color));
        t.setGravity(gravity);
        return t;
    }

    private TextView cardLabel(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(Color.parseColor("#666666"));
        t.setPadding(dp(4), 0, 0, dp(4));
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12), dp(10), dp(12), dp(12));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(12));
        g.setStroke(dp(2), Color.parseColor("#111111"));
        c.setBackground(g);
        return c;
    }

    private TextView rowLabel(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(12);
        t.setTextColor(Color.parseColor("#777777"));
        t.setPadding(0, dp(4), 0, dp(2));
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
        t.setBackground(box());
        return t;
    }

    private GradientDrawable box() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(10));
        g.setStroke(dp(2), Color.parseColor("#111111"));
        return g;
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
