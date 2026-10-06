package com.weng.weng;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 拉手聊天小窗：Dialog 主题，一条输入框+发送；点窗外部自动关闭，不挡屏幕其他区域 */
public class ChatActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FFFFFF"));
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), Color.parseColor("#E5E5E5"));
        root.setBackground(bg);

        // 顶栏：时段 + 当前 App 名
        TextView status = new TextView(this);
        status.setTextSize(11);
        status.setTextColor(Color.parseColor("#888888"));
        status.setPadding(dp(4), 0, dp(4), dp(6));
        String[] p = PetService.timePeriodStatic();
        String app = PetService.instance != null ? PetService.instance.currentAppLabelPublic() : null;
        status.setText(Ico.s(this, "🕐 " + p[0] + (app == null ? "" : " · 当前：" + app)));
        root.addView(status);

        // 模式快捷切换：专注 / 勿扰 / 工作 / jump（开着的标 ●）
        LinearLayout mRow = new LinearLayout(this);
        mRow.setOrientation(LinearLayout.HORIZONTAL);
        final TextView[] modes = new TextView[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            TextView btn = new TextView(this);
            btn.setTextSize(12);
            btn.setTypeface(Typeface.DEFAULT_BOLD);
            btn.setTextColor(Color.parseColor("#111111"));
            btn.setGravity(Gravity.CENTER);
            btn.setPadding(dp(8), dp(5), dp(8), dp(5));   // 边框到文字的留白收紧
            btn.setMinHeight(dp(36));
            btn.setMinimumHeight(dp(36));
            GradientDrawable mb = new GradientDrawable();
            mb.setColor(Color.WHITE);
            mb.setCornerRadius(dp(8));
            mb.setStroke(dp(1), Color.parseColor("#E5E5E5"));
            btn.setBackground(mb);
            btn.setOnClickListener(v -> {
                if (PetService.instance == null) return;
                if (idx == 0) PetService.instance.toggleFocusQuick();
                else if (idx == 1) PetService.instance.toggleDnd();
                else if (idx == 2) PetService.instance.toggleWork();
                else PetService.instance.toggleJumpMode();
                syncModeButtons(modes);
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.rightMargin = i < 3 ? dp(6) : 0;
            mRow.addView(btn, lp);
            modes[i] = btn;
        }
        root.addView(mRow);
        syncModeButtons(modes);
        TextView mHint = new TextView(this);
        mHint.setTextSize(10);
        mHint.setTextColor(Color.parseColor("#999999"));
        mHint.setPadding(dp(4), dp(4), dp(4), dp(6));
        mHint.setText("模式快捷切换（● = 已开启）");
        root.addView(mHint);

        // 输入 + 发送（就一行）
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        // 输入框和「发送」等高、垂直居中，边框才不错位（EditText 默认最小高度接近 48dp）
        row.setGravity(Gravity.CENTER_VERTICAL);
        final EditText in = new EditText(this);
        in.setHint("跟它说点什么…");
        in.setTextSize(14);
        in.setMaxLines(1);
        in.setTextColor(Color.parseColor("#111111"));
        in.setMinHeight(dp(36));
        in.setMinimumHeight(dp(36));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(in, ip);
        TextView send = new TextView(this);
        send.setText("发送");
        send.setTextSize(14);
        send.setTypeface(Typeface.DEFAULT_BOLD);
        send.setTextColor(Color.parseColor("#111111"));
        send.setGravity(Gravity.CENTER);
        send.setPadding(dp(14), dp(8), dp(14), dp(8));   // 边框到文字的留白收紧
        send.setMinHeight(dp(36));
        send.setMinimumHeight(dp(36));
        GradientDrawable sb = new GradientDrawable();
        sb.setColor(Color.WHITE);
        sb.setCornerRadius(dp(8));
        sb.setStroke(dp(1), Color.parseColor("#E5E5E5"));
        send.setBackground(sb);
        send.setOnClickListener(v -> {
            String s = in.getText().toString().trim();
            if (s.isEmpty() || PetService.instance == null) return;
            in.setText("");
            PetService.instance.userChat(s);
            finish();   // 发完就关，回复走宠物气泡
        });
        row.addView(send);
        root.addView(row);

        setContentView(root);
        // Dialog 主题窗口：宽度自适应、不暗化背景、点外部关闭
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        setFinishOnTouchOutside(true);
        // 自动聚焦输入框并弹软键盘
        in.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    /** 按当前状态刷新四个模式按钮的文字 */
    private void syncModeButtons(TextView[] b) {
        if (PetService.instance == null || b == null || b.length < 4) return;
        b[0].setText(Ico.s(this, (PetService.instance.isFocusing() ? "● " : "") + "专注"));
        b[1].setText(Ico.s(this, (PetService.instance.isDnd() ? "● " : "") + "勿扰"));
        b[2].setText(Ico.s(this, (PetService.instance.isWork() ? "● " : "") + "工作"));
        b[3].setText(Ico.s(this, (PetService.instance.isJumpMode() ? "● " : "") + "jump"));
    }

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
