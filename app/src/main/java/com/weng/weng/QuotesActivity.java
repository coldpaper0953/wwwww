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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 台词编辑页：所有本地台词组（含新手教程）都可查看/编辑/恢复默认 */
public class QuotesActivity extends Activity {

    private LinearLayout list;

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
        title.setText("🗣 台词工坊（改了立即生效）");
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#111111"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView hint = new TextView(this);
        hint.setText("每组一行一条。带 | 的组按「标题|内容」或「类别|台词」格式写；教程每行一步，按顺序播放。");
        hint.setTextSize(11);
        hint.setTextColor(Color.parseColor("#888888"));
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(6), 0, dp(10));
        root.addView(hint);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        rebuild();
        root.addView(list);
        root.addView(gap(10));

        TextView reset = button("↩ 全部恢复默认");
        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("恢复默认")
                .setMessage("所有自定义台词都会清掉，恢复出厂文案。确定？")
                .setPositiveButton("恢复", (d, w) -> {
                    Quotes.reset();
                    rebuild();
                    Toast.makeText(this, "已恢复默认台词", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show());
        root.addView(reset);
        root.addView(gap(8));
        TextView back = button("← 返回设置");
        back.setOnClickListener(v -> finish());
        root.addView(back);
    }

    private void rebuild() {
        list.removeAllViews();
        Map<String, List<String>> all = Quotes.all();
        // 排序让基础组在前
        List<String> keys = new ArrayList<String>(all.keySet());
        java.util.Collections.sort(keys);
        for (final String k : keys) {
            LinearLayout card = card();
            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            TextView name = new TextView(this);
            name.setText(Quotes.label(k) + "（" + all.get(k).size() + "条）");
            name.setTextSize(13);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setTextColor(Color.parseColor("#111111"));
            name.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            head.addView(name, np);
            TextView edit = button("✏️");
            edit.setOnClickListener(v -> editDialog(k));
            head.addView(edit);
            card.addView(head);
            TextView preview = new TextView(this);
            StringBuilder sb = new StringBuilder();
            int max = Math.min(2, all.get(k).size());
            for (int i = 0; i < max; i++) sb.append(all.get(k).get(i)).append(i == max - 1 ? "" : "\n");
            if (all.get(k).size() > 2) sb.append("…");
            preview.setText(sb.toString());
            preview.setTextSize(11);
            preview.setTextColor(Color.parseColor("#777777"));
            preview.setPadding(0, dp(4), 0, 0);
            card.addView(preview);
            list.addView(card);
            list.addView(gap(8));
        }
    }

    private void editDialog(final String k) {
        final EditText in = new EditText(this);
        in.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        in.setMinLines(8);
        in.setTextSize(12);
        in.setGravity(Gravity.TOP);
        StringBuilder sb = new StringBuilder();
        for (String s : Quotes.get(k)) sb.append(s).append('\n');
        in.setText(sb.toString());
        new AlertDialog.Builder(this)
                .setTitle(Quotes.label(k))
                .setView(in)
                .setPositiveButton("保存", (d, w) -> {
                    List<String> lines = new ArrayList<String>();
                    for (String s : in.getText().toString().split("\n")) {
                        if (s.trim().length() > 0) lines.add(s.trim());
                    }
                    if (lines.isEmpty()) {
                        Toast.makeText(this, "至少留一条", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Quotes.save(k, lines);
                    rebuild();
                    Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("恢复这组默认", (d, w) -> {
                    Quotes.save(k, new ArrayList<String>());   // 空覆盖=回落默认
                    rebuild();
                    Toast.makeText(this, "这组已恢复默认", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ---------------- 样式 ----------------

    private TextView button(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.parseColor("#111111"));
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(14), dp(8), dp(14), dp(8));
        t.setBackground(box());
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(12));
        g.setStroke(dp(2), Color.parseColor("#111111"));
        c.setBackground(g);
        return c;
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

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
