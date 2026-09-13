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

import org.json.JSONObject;

import java.util.List;

/** 手账：待办(DDL)/习惯打卡/喝水8杯/随手记/专注监督器 */
public class PlannerActivity extends Activity {

    private LinearLayout todoList, habitList;
    private TextView waterLabel, focusLabel, focusBtnText;
    private EditText notesIn;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView page = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.setBackgroundColor(Color.parseColor("#F5F4EF"));
        page.addView(root);
        setContentView(page);

        TextView title = new TextView(this);
        title.setText("📝 我的手账");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#111111"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        root.addView(gap(10));

        // ============ 待办 ============
        root.addView(cardLabel("✅ 待办（可设截止时间）"));
        LinearLayout c1 = card();
        todoList = new LinearLayout(this);
        todoList.setOrientation(LinearLayout.VERTICAL);
        c1.addView(todoList);
        LinearLayout addRow = new LinearLayout(this);
        addRow.setOrientation(LinearLayout.HORIZONTAL);
        final EditText tIn = input("要做什么？");
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        addRow.addView(tIn, lp1);
        addRow.addView(gapW(6));
        final EditText dIn = input("DDL 时:分");
        dIn.setMinWidth(dp(90));
        dIn.setMaxWidth(dp(110));
        addRow.addView(dIn);
        addRow.addView(gapW(6));
        TextView addBtn = button("＋");
        addBtn.setOnClickListener(v -> {
            String t = tIn.getText().toString().trim();
            if (t.isEmpty()) return;
            String d = dIn.getText().toString().trim();
            if (d.length() == 5 && d.contains(":")) Planner.addTodo(t, d);
            else Planner.addTodo(t, "");
            tIn.setText("");
            dIn.setText("");
            rebuild();
            if (PetService.instance != null) {
                PetService.instance.logChat("蚊", "（新增待办：" + t + "，按人设点评一句）");
                PetService.instance.aiChat("用户新增了待办「" + t + "」，请用你的人设点评一句（简短）");
            }
        });
        addRow.addView(addBtn);
        c1.addView(addRow);
        root.addView(c1);
        root.addView(gap(10));

        // ============ 习惯 ============
        root.addView(cardLabel("📅 习惯打卡（喝水也有哦）"));
        LinearLayout c2 = card();
        habitList = new LinearLayout(this);
        habitList.setOrientation(LinearLayout.VERTICAL);
        c2.addView(habitList);
        // 喝水行
        LinearLayout wRow = new LinearLayout(this);
        wRow.setOrientation(LinearLayout.HORIZONTAL);
        waterLabel = text("");
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        wRow.addView(waterLabel, wp);
        TextView wBtn = button("💧 +1 杯");
        wBtn.setOnClickListener(v -> {
            int c = Planner.drinkWater();
            rebuild();
            if (c >= 8 && PetService.instance != null) {
                PetService.instance.awardAff(2);
                PetService.instance.showBubble("喝满 8 杯水！+2 好感～", 3000);
            }
        });
        wRow.addView(wBtn);
        c2.addView(wRow);
        c2.addView(gap(6));
        // 新增习惯
        LinearLayout hAdd = new LinearLayout(this);
        hAdd.setOrientation(LinearLayout.HORIZONTAL);
        final EditText hIn = input("新习惯名");
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        hAdd.addView(hIn, hp);
        hAdd.addView(gapW(6));
        TextView hBtn = button("＋ 习惯");
        hBtn.setOnClickListener(v -> {
            String n = hIn.getText().toString().trim();
            if (n.isEmpty()) return;
            Planner.addHabit(n);
            hIn.setText("");
            rebuild();
            if (PetService.instance != null) {
                PetService.instance.aiChat("用户新增了习惯「" + n + "」，请用你的人设点评一句（简短）");
            }
        });
        hAdd.addView(hBtn);
        c2.addView(hAdd);
        root.addView(c2);
        root.addView(gap(10));

        // ============ 专注监督器 ============
        root.addView(cardLabel("🍅 专注监督器"));
        LinearLayout c3 = card();
        focusLabel = text("选好时长，开始专注吧～");
        c3.addView(focusLabel);
        c3.addView(gap(6));
        LinearLayout fRow = new LinearLayout(this);
        fRow.setOrientation(LinearLayout.HORIZONTAL);
        final EditText minIn = input("分钟(1-120)");
        minIn.setText("25");
        minIn.setMinWidth(dp(80));
        minIn.setMaxWidth(dp(100));
        fRow.addView(minIn);
        fRow.addView(gapW(6));
        final EditText goalIn = input("本次目标(可选)");
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        fRow.addView(goalIn, gp);
        fRow.addView(gapW(6));
        focusBtnText = button("▶ 开始");
        focusBtnText.setOnClickListener(v -> {
            if (PetService.instance == null) return;
            org.json.JSONObject f = Planner.focus();
            if (f.optBoolean("active", false)) {
                if (f.optLong("pausedAt", 0) > 0) {
                    Planner.resumeFocus();
                    PetService.instance.showBubble("继续专注！加油～", 2000);
                } else {
                    Planner.pauseFocus();
                    PetService.instance.showBubble("呼……可以喘口气了", 2500);
                }
            } else {
                int m;
                try {
                    m = Integer.parseInt(minIn.getText().toString().trim());
                } catch (Exception e) {
                    m = 25;
                }
                m = Math.max(1, Math.min(120, m));
                Planner.startFocus(m, false, goalIn.getText().toString().trim(), false);
                PetService.instance.showBubble("专注开始！我会盯着你哦👀", 3000);
                PetService.instance.workMode = false;
            }
            rebuild();
        });
        fRow.addView(focusBtnText);
        c3.addView(fRow);
        c3.addView(gap(6));
        TextView fStop = button("⏹ 结束专注");
        fStop.setOnClickListener(v -> {
            Planner.stopFocus();
            rebuild();
        });
        c3.addView(fStop);
        c3.addView(gap(4));
        TextView fStat = text("");
        fStat.setText("今日专注 " + com.weng.weng.DataStore.getInt(focusTodayKey(), 0) + " 场 / "
                + com.weng.weng.DataStore.getInt(focusTodayKey() + "min", 0) + " 分钟　累计 "
                + com.weng.weng.DataStore.getInt("focusTotalN", 0) + " 场 / "
                + com.weng.weng.DataStore.getInt("focusTotalMin", 0) + " 分钟");
        c3.addView(fStat);
        root.addView(c3);
        root.addView(gap(10));

        // ============ 随手记 ============
        root.addView(cardLabel("🗒️ 随手记（自动保存）"));
        LinearLayout c4 = card();
        notesIn = new EditText(this);
        notesIn.setMinLines(4);
        notesIn.setGravity(Gravity.TOP);
        notesIn.setTextSize(13);
        notesIn.setTextColor(Color.parseColor("#111111"));
        notesIn.setPadding(dp(8), dp(8), dp(8), dp(8));
        notesIn.setText(Planner.notes());
        c4.addView(notesIn);
        root.addView(c4);
        root.addView(gap(10));

        TextView back = button("← 返回设置");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        rebuild();
    }

    private String focusTodayKey() {
        return "focusToday_" + new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(new java.util.Date());
    }

    private void rebuild() {
        // ---- 待办 ----
        todoList.removeAllViews();
        List<JSONObject> todos = Planner.todos();
        if (todos.isEmpty()) {
            TextView e = text("（还没有待办，加一个？）");
            todoList.addView(e);
        }
        for (int i = 0; i < todos.size(); i++) {
            final int idx = i;
            JSONObject o = todos.get(i);
            boolean done = o.optBoolean("done", false);
            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);
            String ddl = o.optString("ddl", "");
            TextView t = text((done ? "☑ " : "☐ ") + o.optString("text", "")
                    + (ddl.length() == 5 ? "　⏰今天 " + ddl : ""));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            r.addView(t, lp);
            if (!done) {
                TextView ok = button("完成");
                ok.setOnClickListener(v -> {
                    String txt = Planner.completeTodo(idx);
                    if (txt != null && PetService.instance != null) {
                        PetService.instance.awardAff(5);
                        DataStore.setBlood(Math.min(100, DataStore.getBlood() + 8));
                        PetService.instance.showBubble("完成了「" + txt + "」！+5 好感 +8 血池 🎉", 4000);
                        PetService.instance.aiChat("用户完成了待办「" + txt + "」，夸夸他");
                    }
                    rebuild();
                });
                r.addView(ok);
                r.addView(gapW(4));
            }
            TextView del = button("✖");
            del.setOnClickListener(v -> {
                Planner.removeTodo(idx);
                rebuild();
            });
            r.addView(del);
            todoList.addView(r);
            todoList.addView(gap(4));
        }
        // ---- 习惯 ----
        habitList.removeAllViews();
        List<JSONObject> habits = Planner.habits();
        if (habits.isEmpty()) {
            habitList.addView(text("（还没有习惯）"));
        }
        for (int i = 0; i < habits.size(); i++) {
            final int idx = i;
            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);
            TextView t = text(Planner.habitCard(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            r.addView(t, lp);
            TextView ok = button("打卡");
            ok.setOnClickListener(v -> {
                Object[] res = Planner.checkHabit(idx);
                if ((Boolean) res[0]) {
                    if (PetService.instance != null) {
                        PetService.instance.awardAff(2);
                        PetService.instance.showBubble("打卡成功！连续 " + res[1] + " 天 +2 好感～", 3000);
                    }
                } else {
                    Toast.makeText(this, "今天已打过卡啦", Toast.LENGTH_SHORT).show();
                }
                rebuild();
            });
            r.addView(ok);
            r.addView(gapW(4));
            TextView del = button("✖");
            del.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("删除习惯")
                        .setMessage("确定删除「" + Planner.habitCard(idx) + "」？")
                        .setPositiveButton("删", (d, w) -> {
                            Planner.removeHabit(idx);
                            rebuild();
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
            r.addView(del);
            habitList.addView(r);
            habitList.addView(gap(4));
        }
        waterLabel.setText("💧 今日喝水 " + Planner.waterToday() + "/8 杯");
        // ---- 专注 ----
        org.json.JSONObject f = Planner.focus();
        if (f.optBoolean("active", false)) {
            boolean paused = f.optLong("pausedAt", 0) > 0;
            focusLabel.setText("🍅 " + Planner.focusText() + "　目标：" + f.optString("goal", "—"));
            focusBtnText.setText(paused ? "▶ 继续" : "⏸ 暂停");
        } else {
            focusLabel.setText("选好时长，开始专注吧～");
            focusBtnText.setText("▶ 开始");
        }
        // 手账催办
        int open = Planner.openCount();
        if (open > 5 && PetService.instance != null) {
            PetService.instance.showBubble("未完成待办都 " + open + " 个了！别拖延啦！", 4000);
        }
    }

    @Override
    protected void onPause() {
        Planner.saveNotes(notesIn.getText().toString());
        super.onPause();
    }

    // ---------------- 样式（与设置页同款漫画风） ----------------

    private TextView text(String s) {
        TextView t = new TextView(this);
        t.setText(Ico.s(this, s));
        t.setTextSize(13);
        t.setTextColor(Color.parseColor("#111111"));
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setSingleLine(true);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        return t;
    }

    private TextView cardLabel(String s) {
        TextView t = new TextView(this);
        t.setText(Ico.s(this, s));
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

    private TextView button(String s) {
        TextView t = new TextView(this);
        t.setText(Ico.s(this, s));
        t.setTextColor(Color.parseColor("#111111"));
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(10), dp(8), dp(10), dp(8));
        t.setBackground(box());
        return t;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(13);
        e.setMaxLines(1);
        e.setTextColor(Color.parseColor("#111111"));
        e.setPadding(dp(8), dp(6), dp(8), dp(6));
        e.setBackground(box());
        return e;
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
