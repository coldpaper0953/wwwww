package com.weng.weng;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

/** 记忆本：查看原始/长期记忆、手动整理、阈值设置、副 API 配置、用户身份提示词 */
public class MemoryActivity extends Activity {

    private TextView statLine, rawBox, longBox, subLine;
    private TextView digestLine;
    private TextView digestBtn;
    private ProgressDialog digestPd;
    private boolean digesting = false;
    private boolean destroyed = false;

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
        title.setText("🧠 记忆本");
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#111111"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        statLine = small("#555555");
        statLine.setGravity(Gravity.CENTER);
        root.addView(statLine);
        root.addView(gap(10));

        // ---- 长期记忆 ----
        root.addView(cardLabel("⭐ 长期记忆（整理后，注入 AI 提示词）"));
        LinearLayout c1 = card();
        longBox = small("#111111");
        c1.addView(longBox);
        LinearLayout lRow = row();
        digestBtn = button("🌀 立即整理");
        digestBtn.setOnClickListener(v -> doDigest());
        lRow.addView(digestBtn);
        lRow.addView(gapW(6));
        TextView setTh = button("⚙ 阈值/保留数");
        setTh.setOnClickListener(v -> thresholdDialog());
        lRow.addView(setTh);
        c1.addView(lRow);
        // 整理状态行：弹窗被系统干掉时也能看到结果（不再只靠 Toast）
        digestLine = small("#777777");
        digestLine.setPadding(0, dp(6), 0, 0);
        digestLine.setText("空闲中");
        c1.addView(digestLine);
        root.addView(c1);
        root.addView(gap(10));

        // ---- 用户身份提示词 ----
        root.addView(cardLabel("🙋 用户身份（随每次 AI 请求一起发送）"));
        LinearLayout c2 = card();
        TextView personaNow = small("#777777");
        personaNow.setText(currentPersonaPreview());
        c2.addView(personaNow);
        LinearLayout pRow = row();
        TextView editP = button("✏️ 编辑我的身份");
        editP.setOnClickListener(v -> personaDialog());
        pRow.addView(editP);
        c2.addView(pRow);
        root.addView(c2);
        root.addView(gap(10));

        // ---- 副 API ----
        root.addView(cardLabel("🔀 副 API（记忆整理专用；留空＝用主 API）"));
        LinearLayout c3 = card();
        subLine = small("#777777");
        subLine.setText(subPreview());
        c3.addView(subLine);
        LinearLayout sRow = row();
        TextView editS = button("✏️ 配置副 API");
        editS.setOnClickListener(v -> subDialog());
        sRow.addView(editS);
        c3.addView(sRow);
        root.addView(c3);
        root.addView(gap(10));

        // ---- 原始记忆 ----
        root.addView(cardLabel("📃 原始记忆（每次 AI 回复自动记录，攒到 " + Memory.threshold() + " 条自动整理）"));
        LinearLayout c4 = card();
        rawBox = small("#111111");
        c4.addView(rawBox);
        LinearLayout rRow = row();
        TextView clearB = button("🗑 清空全部记忆");
        clearB.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("清空记忆")
                .setMessage("原始记忆和长期记忆都会清空，确定？")
                .setPositiveButton("清空", (d, w) -> {
                    Memory.clearAll();
                    refresh();
                    Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show());
        rRow.addView(clearB);
        c4.addView(rRow);
        root.addView(c4);
        root.addView(gap(10));

        TextView back = button("← 返回设置");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        refresh();
    }

    private String currentPersonaPreview() {
        String p = Memory.userPersona();
        return p.isEmpty() ? "（还没设置。例：我是大三学生，在准备考研，喜欢熬夜但知道不好）" : p;
    }

    private String subPreview() {
        if (Memory.subBase().isEmpty()) return "（未配置，整理将用主 API）";
        return Memory.subBase() + " · 模型 " + (Memory.subModel().isEmpty() ? "（默认用主模型）" : Memory.subModel());
    }

    private void refresh() {
        statLine.setText("原始记忆 " + Memory.rawCount() + "/" + Memory.threshold() + " 条 · 长期记忆 " + Memory.longTerm().size() + "/" + Memory.keep() + " 条");
        List<JSONObject> raw = Memory.raw();
        if (raw.isEmpty()) {
            rawBox.setText("（还没有原始记忆，跟蚊子聊几句就有了）");
        } else {
            StringBuilder sb = new StringBuilder();
            int from = Math.max(0, raw.size() - 30);
            for (int i = raw.size() - 1; i >= from; i--) {
                JSONObject o = raw.get(i);
                sb.append('·').append(o.optString("t", "")).append('\n')
                        .append("  ").append(o.optString("ev", "")).append('\n');
            }
            if (raw.size() > 30) sb.insert(0, "（最近 30 条，共 " + raw.size() + " 条）\n");
            rawBox.setText(sb.toString());
        }
        List<String> l = Memory.longTerm();
        longBox.setText(l.isEmpty() ? "（整理后的记忆会出现在这里）" : joinLines(l));
        subLine.setText(subPreview());
    }

    private String joinLines(List<String> l) {
        StringBuilder sb = new StringBuilder();
        for (String s : l) sb.append("· ").append(s).append('\n');
        return sb.toString().trim();
    }

    private void doDigest() {
        if (digesting) {
            Toast.makeText(this, "正在整理中，稍等一下…", Toast.LENGTH_SHORT).show();
            return;
        }
        if (PetService.instance == null) {
            Toast.makeText(this, "宠物未运行", Toast.LENGTH_SHORT).show();
            return;
        }
        digesting = true;
        if (digestBtn != null) digestBtn.setText(Ico.s(this, "🌀 整理中…"));
        if (digestLine != null) digestLine.setText("正在用副 API 整理记忆…（最长 90 秒）");
        try {
            digestPd = new ProgressDialog(this);
            digestPd.setMessage("正在用副 API 整理记忆…");
            digestPd.setCancelable(true);       // 允许取消，避免卡死在长请求里
            digestPd.show();
        } catch (Exception ignored) {
            digestPd = null;                    // 弹窗失败不影响主流程，页内有状态行兜底
        }
        Memory.digest(PetService.instance, (ok, d, e) -> {
            // 回调发生在后台线程，切回主线程再碰 View
            runOnUiThread(() -> {
                digesting = false;
                safeDismissPd();
                if (destroyed) return;          // 页面已经销毁，别再碰任何 View
                if (digestBtn != null) digestBtn.setText(Ico.s(this, "🌀 立即整理"));
                String msg = ok ? "整理完成，长期记忆已更新" : ("整理失败：" + (e == null ? "未知错误" : e));
                if (digestLine != null) digestLine.setText(msg);
                Toast.makeText(this, ok ? "整理完成" : msg, ok ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
                refresh();
            });
        });
    }

    /** ProgressDialog 的 dismiss 在 Activity 已销毁时会抛 "View not attached to window manager" —— 必须挡住 */
    private void safeDismissPd() {
        try {
            if (digestPd != null) {
                if (!isFinishing() && !isDestroyed()) digestPd.dismiss();
                digestPd = null;
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        safeDismissPd();
        super.onDestroy();
    }

    private void thresholdDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(8), dp(12), dp(4));
        final EditText thIn = editLine(String.valueOf(Memory.threshold()));
        TextView thLabel = small("#777777");
        thLabel.setText("自动整理阈值：原始记忆攒到多少条触发（30-1000）");
        box.addView(thLabel);
        box.addView(thIn);
        box.addView(gap(8));
        final EditText keepIn = editLine(String.valueOf(Memory.keep()));
        TextView keepLabel = small("#777777");
        keepLabel.setText("整理后保留条数：长期记忆最多留几条（3-50）");
        box.addView(keepLabel);
        box.addView(keepIn);
        new AlertDialog.Builder(this)
                .setTitle("记忆整理参数")
                .setView(box)
                .setPositiveButton("保存", (d, w) -> {
                    try {
                        Memory.setThreshold(Integer.parseInt(thIn.getText().toString().trim()));
                        Memory.setKeep(Integer.parseInt(keepIn.getText().toString().trim()));
                        refresh();
                        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "要填数字", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void personaDialog() {
        final EditText in = new EditText(this);
        in.setMinLines(4);
        in.setGravity(Gravity.TOP);
        in.setTextSize(13);
        in.setText(Memory.userPersona());
        new AlertDialog.Builder(this)
                .setTitle("我的身份（会拼进 AI 提示词）")
                .setView(in)
                .setPositiveButton("保存", (d, w) -> {
                    Memory.setUserPersona(in.getText().toString());
                    refresh();
                    Toast.makeText(this, "已保存，下次对话生效", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void subDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(8), dp(12), dp(4));
        final EditText baseIn = editLine(Memory.subBase());
        TextView l1 = small("#777777");
        l1.setText("接口地址（留空＝用主 API）");
        box.addView(l1);
        box.addView(baseIn);
        box.addView(gap(6));
        final EditText keyIn = editLine(Memory.subKey());
        TextView l2 = small("#777777");
        l2.setText("API Key（留空＝用主 Key）");
        box.addView(l2);
        box.addView(keyIn);
        box.addView(gap(6));
        final EditText modelIn = editLine(Memory.subModel());
        TextView l3 = small("#777777");
        l3.setText("模型名");
        box.addView(l3);
        box.addView(modelIn);
        new AlertDialog.Builder(this)
                .setTitle("副 API（记忆整理专用）")
                .setView(box)
                .setPositiveButton("保存", (d, w) -> {
                    Memory.setSub(baseIn.getText().toString(), keyIn.getText().toString(), modelIn.getText().toString());
                    refresh();
                    Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ---------------- 样式 ----------------

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }

    private EditText editLine(String text) {
        EditText e = new EditText(this);
        e.setText(text);
        e.setTextSize(13);
        e.setMaxLines(1);
        e.setTextColor(Color.parseColor("#111111"));
        e.setPadding(dp(8), dp(6), dp(8), dp(6));
        e.setBackground(box());
        return e;
    }

    private TextView small(String color) {
        TextView t = new TextView(this);
        t.setTextSize(12);
        t.setTextColor(Color.parseColor(color));
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
        c.setPadding(dp(12), dp(10), dp(12), dp(10));
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
        t.setPadding(dp(14), dp(8), dp(14), dp(8));
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
