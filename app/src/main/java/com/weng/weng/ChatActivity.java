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
        bg.setColor(Color.parseColor("#F2FFFFFF"));
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(2), Color.parseColor("#111111"));
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

        // 输入 + 发送（就一行）
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        final EditText in = new EditText(this);
        in.setHint("跟它说点什么…");
        in.setTextSize(14);
        in.setMaxLines(1);
        in.setTextColor(Color.parseColor("#111111"));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(in, ip);
        TextView send = new TextView(this);
        send.setText("发送");
        send.setTextSize(14);
        send.setTypeface(Typeface.DEFAULT_BOLD);
        send.setTextColor(Color.parseColor("#111111"));
        send.setGravity(Gravity.CENTER);
        send.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable sb = new GradientDrawable();
        sb.setColor(Color.WHITE);
        sb.setCornerRadius(dp(12));
        sb.setStroke(dp(2), Color.parseColor("#111111"));
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

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
