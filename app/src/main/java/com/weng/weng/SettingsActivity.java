package com.weng.weng;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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

/** App 内设置窗口：宠物形象、人设、对话与各种玩法参数都在这里调 */
public class SettingsActivity extends Activity {

    private TextView affLabel, verLabel, chatLog, speedLabel, dndBtn, affVal, workBtn;
    private EditText input;
    private ScrollView scroller;
    private android.app.AlertDialog pickDlg;
    private EditText apiBaseField, apiKeyField, apiModelField;
    /** API 预设：当前预设名显示 + 切换预设弹窗引用 */
    private TextView presetLabel;
    private android.app.AlertDialog presetDlg;
    private TextView emoLine, feedLine;
    private TextView prankScore, prankBtn;
    private EditText prankCount;
    private TextView[] glideBtns;
    private TextView tabBtn, senseBtn;
    private android.widget.SeekBar sizeSlider;
    private TextView sizeVal;
    private LinearLayout satBar;
    private View satFill, satRest;
    private TextView satLabel;
    private android.widget.SeekBar chatGapSlider, chatJitSlider, chatCapSlider;
    private TextView chatGapVal, chatJitVal, chatCapVal, chatCountVal;
    private android.widget.SeekBar jumpPctSlider;
    private TextView jumpPctVal;
    private android.widget.SeekBar standLiftSlider;
    private TextView standLiftVal;
    private TextView jumpModeBtn;
    /** 更新按钮：存档里有待更新版本时，文案会变成「有新版本 …」 */
    private TextView updBtn;
    /** Bug 反馈 / 建议许愿的输入框与署名 */
    private EditText fbBug, fbWish, fbName;
    /** 反馈/许愿折叠区的标题（点击展开/收起）与内容容器 */
    private TextView fbFoldTitle;
    private LinearLayout fbFoldBody;
    /** 反馈收件邮箱（App 里没有服务器，走系统邮件 App 发出去） */
    private static final String FEEDBACK_MAIL = "mudaor0953@outlook.com";
    /** 署名默认值（第一次用预填它，改过就记住新的） */
    private static final String SIGN_DEFAULT = "cn";
    private boolean chatDragging = false;
    /** 自定义形象：每个动作一行的状态文本（仅桌宠2.0 显示） */
    private TextView[] skinRows;
    /** 正在等待相册返回的动作 key */
    private String pickingKey;
    private static final int REQ_SKIN = 0x5c01;
    /** 自定义人设：状态文本（仅桌宠2.0 显示） */
    private TextView personaRow;
    /** 填人设输入框时用的示例 */
    private static final String SAMPLE_PERSONA =
            "你叫小黑，是一只住在这台手机里的黑猫。性格高冷、话少，其实很在意主人，但嘴上不肯承认。"
                    + "喜欢待在屏幕边上看主人干活，偶尔冒出一句吐槽，每句结尾有时带个「喵」。";

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
        title.setText(getString(R.string.app_name) + " · 设置");
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
        // 输入框和按钮等高、垂直居中，两者的边框才不会上下错位
        row.setGravity(Gravity.CENTER_VERTICAL);
        input = new EditText(this);
        input.setHint("跟它说点什么…");
        input.setTextSize(14);
        input.setMaxLines(1);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));
        input.setMinHeight(dp(36));
        input.setMinimumHeight(dp(36));
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
        root.addView(gap(8));

        // 手账入口
        TextView plannerBtn = button("📝 我的手账（待办/习惯/喝水/专注/随手记）");
        plannerBtn.setOnClickListener(v -> startActivity(new Intent(this, PlannerActivity.class)));
        root.addView(plannerBtn);
        root.addView(gap(10));

        // ============ 宠物设置卡片 ============
        root.addView(cardLabel("🐝 宠物设置"));
        LinearLayout petCard = card();

        petCard.addView(rowLabel("蚊子大小（捏合也可调）"));
        LinearLayout sizeRow = new LinearLayout(this);
        sizeRow.setOrientation(LinearLayout.HORIZONTAL);
        sizeSlider = new android.widget.SeekBar(this);
        sizeSlider.setMax(224);   // 32-256px
        sizeSlider.setProgress((int) (PetService.instance != null ? PetService.instance.petScale * 96 : 96) - 32);
        LinearLayout.LayoutParams szlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        sizeRow.addView(sizeSlider, szlp);
        sizeVal = new TextView(this);
        sizeVal.setTextSize(13);
        sizeVal.setTypeface(Typeface.DEFAULT_BOLD);
        sizeVal.setTextColor(Color.parseColor("#111111"));
        sizeVal.setGravity(Gravity.CENTER);
        sizeVal.setMinWidth(dp(48));
        sizeRow.addView(sizeVal);
        sizeSlider.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar sb, int p, boolean fromUser) {
                sizeVal.setText((p + 32) + "px");
                if (fromUser && PetService.instance != null) {
                    PetService.instance.setSizeByPx(p + 32);
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar sb) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar sb) {
                if (PetService.instance != null) PetService.instance.saveSizeNow();
            }
        });
        petCard.addView(sizeRow);
        petCard.addView(gap(8));

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

        // 跳跃高度（占屏幕高度的百分比）—— 管的是"一跳多高"，不管站在哪
        petCard.addView(rowLabel("跳跃高度（一下跳多高，占屏高百分之几）"));
        LinearLayout jhRow = new LinearLayout(this);
        jhRow.setOrientation(LinearLayout.HORIZONTAL);
        jumpPctSlider = new android.widget.SeekBar(this);
        jumpPctSlider.setMax(36);                       // 4% ~ 40%
        jumpPctSlider.setProgress(clampInt(DataStore.getInt("jumpPct", 14), 4, 40) - 4);
        jhRow.addView(jumpPctSlider,
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        jumpPctVal = valueLabel();
        jhRow.addView(jumpPctVal);
        jumpPctSlider.setOnSeekBarChangeListener(seeker(() -> {
            int pct = jumpPctSlider.getProgress() + 4;
            DataStore.putInt("jumpPct", pct);
            jumpPctVal.setText(pct + "%");
        }));
        petCard.addView(jhRow);
        petCard.addView(gap(8));

        // 站立高度（离屏幕底部多高）—— 管的是"站在哪一层"
        petCard.addView(rowLabel("站立高度（离屏幕底部多高，0% ＝ 贴底）"));
        LinearLayout shRow = new LinearLayout(this);
        shRow.setOrientation(LinearLayout.HORIZONTAL);
        standLiftSlider = new android.widget.SeekBar(this);
        standLiftSlider.setMax(50);                     // 0% ~ 50%
        standLiftSlider.setProgress(clampInt(DataStore.getInt("standLift", 0), 0, 50));
        shRow.addView(standLiftSlider,
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        standLiftVal = valueLabel();
        shRow.addView(standLiftVal);
        standLiftSlider.setOnSeekBarChangeListener(seeker(() -> {
            int lp = standLiftSlider.getProgress();
            DataStore.putInt("standLift", lp);
            if (PetService.instance != null) PetService.instance.setStandLift(lp);   // 立刻生效，可当场看效果
            standLiftVal.setText(lp == 0 ? "贴底" : "离底 " + lp + "%");
        }));
        petCard.addView(shRow);
        petCard.addView(gap(8));

        petCard.addView(rowLabel("好感度（可手动改）"));
        LinearLayout affRow = new LinearLayout(this);
        affRow.setOrientation(LinearLayout.HORIZONTAL);
        affVal = new TextView(this);
        affVal.setTextSize(14);
        affVal.setTypeface(Typeface.DEFAULT_BOLD);
        affVal.setTextColor(Color.parseColor("#111111"));
        affVal.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams avp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        affRow.addView(affVal, avp);
        TextView editAff = button("✏️ 修改");
        editAff.setOnClickListener(v -> editAffectionDialog());
        affRow.addView(editAff);
        petCard.addView(affRow);
        petCard.addView(gap(8));

        // 状态卡：称号/心情/饲养
        TextView statusLine = small("#555555", Gravity.LEFT);
        statusLine.setText(Ico.s(this, "❤ " + com.weng.weng.DataStore.titleFor(com.weng.weng.DataStore.getAff())
                + " · 今日好感 " + com.weng.weng.DataStore.sp().getInt("dailyAff", 0) + "/50"));
        statusLine.setPadding(dp(4), 0, 0, dp(4));
        petCard.addView(statusLine);
        emoLine = small("#777777", Gravity.LEFT);
        emoLine.setPadding(dp(4), dp(2), 0, dp(4));
        petCard.addView(emoLine);
        buildSatietyBar(petCard);          // 饱食度可视化进度条
        feedLine = small("#777777", Gravity.LEFT);
        feedLine.setPadding(dp(4), dp(2), 0, dp(4));
        petCard.addView(feedLine);

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
        workBtn = button("📚 工作");
        workBtn.setOnClickListener(v -> {
            if (PetService.instance != null) {
                PetService.instance.toggleWork();
                refresh();
            }
        });
        modeRow.addView(workBtn);
        modeRow.addView(gapW(8));
        TextView feedBtn = button("🩸 献血");
        feedBtn.setOnClickListener(v -> {
            if (PetService.instance != null) {
                PetService.instance.startFeed();
                refresh();
            }
        });
        modeRow.addView(feedBtn);
        modeRow.addView(gapW(8));
        TextView persona = button(Edition.CUSTOM_PERSONA ? "🎭 人设可自定义" : "🔒 人设已锁定");
        persona.setOnClickListener(v -> {
            if (Edition.CUSTOM_PERSONA) {
                personaDialog();          // 直接开编辑框，和下面的「自定义人设」卡片是同一个入口
            } else {
                Toast.makeText(this, "核心人设已内置加密保护，无法查看或修改", Toast.LENGTH_LONG).show();
            }
        });
        modeRow.addView(persona);
        petCard.addView(modeRow);
        root.addView(petCard);
        root.addView(gap(10));

        // ============ 自定义形象（仅桌宠2.0 出现） ============
        if (Edition.CUSTOM_SKIN) {
            root.addView(cardLabel("🎨 自定义形象"));
            LinearLayout skinCard = card();
            skinCard.addView(rowLabel("从相册挑图替换宠物形象：一次可选多张（按顺序当动画帧），只选一张就是静态图。"));
            skinRows = new TextView[Skin.KEYS.length];
            for (int i = 0; i < Skin.KEYS.length; i++) {
                final String key = Skin.KEYS[i];
                LinearLayout r = new LinearLayout(this);
                r.setOrientation(LinearLayout.HORIZONTAL);
                r.setGravity(Gravity.CENTER_VERTICAL);
                TextView name = new TextView(this);
                name.setText(Skin.LABELS[i]);
                name.setTextSize(13);
                name.setTextColor(Color.parseColor("#111111"));
                r.addView(name, new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                TextView state = small("#888888", Gravity.END);
                skinRows[i] = state;
                r.addView(state);
                r.addView(gapW(6));
                TextView pick = button("选图");
                pick.setOnClickListener(v -> pickSkin(key));
                r.addView(pick);
                r.addView(gapW(6));
                TextView def = button("默认");
                def.setOnClickListener(v -> {
                    Skin.clear(SettingsActivity.this, key);
                    if (PetService.instance != null) PetService.instance.reloadFrames();
                    refreshSkinRows();
                });
                r.addView(def);
                skinCard.addView(r);
                skinCard.addView(gap(6));
            }
            TextView resetAll = button("全部恢复默认形象");
            resetAll.setOnClickListener(v -> {
                Skin.clearAll(SettingsActivity.this);
                if (PetService.instance != null) PetService.instance.reloadFrames();
                refreshSkinRows();
                Toast.makeText(this, "已恢复内置形象", Toast.LENGTH_SHORT).show();
            });
            skinCard.addView(resetAll);
            root.addView(skinCard);
            root.addView(gap(10));
        }

        // ============ 自定义人设（仅桌宠2.0 出现） ============
        if (Edition.CUSTOM_PERSONA) {
            root.addView(cardLabel("🎭 自定义人设"));
            LinearLayout pCard = card();
            pCard.addView(rowLabel("写它是谁、什么性格、怎么说话。留空就回到默认的宠物口吻。"));
            personaRow = small("#888888", Gravity.LEFT);
            personaRow.setPadding(0, dp(2), 0, dp(6));
            pCard.addView(personaRow);
            LinearLayout pRow = new LinearLayout(this);
            pRow.setOrientation(LinearLayout.HORIZONTAL);
            TextView editP = button("✏️ 写人设");
            editP.setOnClickListener(v -> personaDialog());
            pRow.addView(editP);
            pRow.addView(gapW(6));
            TextView clearP = button("清空");
            clearP.setOnClickListener(v -> {
                Memory.setPetPersona("");
                refreshPersonaRow();
                Toast.makeText(this, "已清空，回到默认口吻", Toast.LENGTH_SHORT).show();
            });
            pRow.addView(clearP);
            pCard.addView(pRow);
            root.addView(pCard);
            root.addView(gap(10));
        }

        // ============ 语录卡片（旧5组入口保留） → 全量台词工坊 ============
        root.addView(cardLabel("🗣 台词与记忆"));
        LinearLayout qCard = card();
        LinearLayout qRow = new LinearLayout(this);
        qRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView quotesAll = button("🗣 台词工坊（全部台词含教程）");
        quotesAll.setOnClickListener(v -> startActivity(new Intent(this, QuotesActivity.class)));
        qRow.addView(quotesAll);
        qRow.addView(gapW(6));
        TextView memBtn = button("🧠 记忆本");
        memBtn.setOnClickListener(v -> startActivity(new Intent(this, MemoryActivity.class)));
        qRow.addView(memBtn);
        qCard.addView(qRow);
        qCard.addView(gap(6));
        TextView qHint = small("#999999", Gravity.LEFT);
        qHint.setText("台词工坊里可改：戳/扔/复活/时段问候/主动搭话题/小剧场/伪造报错/App吐槽/新手教程等全部文案；记忆本里可看 AI 的记忆、设副 API 和你的身份。");
        qHint.setPadding(dp(2), 0, 0, 0);
        qCard.addView(qHint);
        root.addView(qCard);
        root.addView(gap(10));

        // ============ API 卡片 ============
        root.addView(cardLabel("🔌 AI 接口（可换服务商）"));
        LinearLayout apiCard = card();
        // 新手指导：一行提示 + 教程按钮
        LinearLayout apiHelpRow = new LinearLayout(this);
        apiHelpRow.setOrientation(LinearLayout.HORIZONTAL);
        apiHelpRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView apiHelp = small("#999999", Gravity.LEFT);
        apiHelp.setText("第一次配置？看教程照着填，三步就好");
        LinearLayout.LayoutParams ahp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        apiHelpRow.addView(apiHelp, ahp);
        apiHelpRow.addView(gapW(6));
        TextView helpBtn = button("❓ 新手教程");
        helpBtn.setOnClickListener(v -> showApiTutorial());
        apiHelpRow.addView(helpBtn);
        apiCard.addView(apiHelpRow);
        apiCard.addView(gap(8));
        // 预设行：显示当前预设 + 存为预设 + 切换预设（多个 API 配置一键切换）
        LinearLayout presetRow = new LinearLayout(this);
        presetRow.setOrientation(LinearLayout.HORIZONTAL);
        presetRow.setGravity(Gravity.CENTER_VERTICAL);
        presetLabel = small("#666666", Gravity.LEFT);
        presetLabel.setText("预设：" + currentPresetName());
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        presetRow.addView(presetLabel, plp);
        presetRow.addView(gapW(6));
        TextView savePresetBtn = button("📌 存为预设");
        savePresetBtn.setOnClickListener(v -> saveAsPreset());
        presetRow.addView(savePresetBtn);
        presetRow.addView(gapW(6));
        TextView switchPresetBtn = button("🔁 切换");
        switchPresetBtn.setOnClickListener(v -> switchPreset());
        presetRow.addView(switchPresetBtn);
        apiCard.addView(presetRow);
        apiCard.addView(gap(8));
        apiBaseField = apiInput("接口地址：填 https://api.xxx.com/v1 即可（忘了 /v1 保存时会自动补）",
                PetService.instance != null ? PetService.instance.apiBase : "");
        apiCard.addView(apiBaseField);
        apiCard.addView(gap(6));
        // Key 留空＝沿用已保存的 Key；想换 Key 就填新的
        apiKeyField = apiInput("API Key（留空＝沿用已保存的 Key）", "");
        apiCard.addView(apiKeyField);
        apiCard.addView(gap(6));
        apiModelField = apiInput("模型名", PetService.instance != null ? PetService.instance.apiModel : "");
        LinearLayout modelRow = new LinearLayout(this);
        modelRow.setOrientation(LinearLayout.HORIZONTAL);
        modelRow.setGravity(Gravity.CENTER_VERTICAL);   // 模型输入框 + 「📡 拉取」按钮对齐
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        modelRow.addView(apiModelField, mlp);
        modelRow.addView(gapW(8));
        TextView fetchBtn = button("📡 拉取");
        fetchBtn.setOnClickListener(v -> fetchModelsDialog());
        modelRow.addView(fetchBtn);
        apiCard.addView(modelRow);
        apiCard.addView(gap(8));
        TextView saveApiBtn = button("💾 保存并测试");
        saveApiBtn.setOnClickListener(v -> saveApi());
        apiCard.addView(saveApiBtn);
        root.addView(apiCard);
        root.addView(gap(10));

        // ============ 整蛊模式卡片 ============
        root.addView(cardLabel("🦟 整蛊模式（和电脑版一样的蚊群拍打游戏）"));
        LinearLayout prankCard = card();
        LinearLayout pRow = new LinearLayout(this);
        pRow.setOrientation(LinearLayout.HORIZONTAL);
        pRow.setGravity(Gravity.CENTER_VERTICAL);       // 蚊子数量输入框 + 「🦟 注入并隐藏」按钮对齐
        prankCount = apiInput("蚊子数量(1-10)", String.valueOf(com.weng.weng.DataStore.getInt("prankCount", 6)));
        LinearLayout.LayoutParams pcp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        pRow.addView(prankCount, pcp);
        pRow.addView(gapW(6));
        prankBtn = button("🦟 注入并隐藏");
        prankBtn.setOnClickListener(v -> {
            if (PetService.instance == null) return;
            int n = 6;
            try {
                n = Integer.parseInt(prankCount.getText().toString().trim());
            } catch (Exception ignored) {
            }
            n = Math.max(1, Math.min(10, n));
            com.weng.weng.DataStore.putInt("prankCount", n);
            if (PetService.instance.prankMode) {
                PetService.instance.stopPrank();
                Toast.makeText(this, "整蛊结束，蚊子回来了", Toast.LENGTH_SHORT).show();
            } else {
                PetService.instance.startPrank(n);
                Toast.makeText(this, "蚊群已注入！回到桌面拍打它们", Toast.LENGTH_SHORT).show();
            }
            refresh();
        });
        pRow.addView(prankBtn);
        prankCard.addView(pRow);
        prankCard.addView(gap(6));
        prankScore = small("#777777", Gravity.LEFT);
        prankScore.setPadding(dp(4), 0, 0, dp(2));
        prankCard.addView(prankScore);
        root.addView(prankCard);
        root.addView(gap(10));

        // ============ 更多玩法卡片 ============
        root.addView(cardLabel("🎮 更多玩法"));
        LinearLayout exCard = card();
        LinearLayout exRow = new LinearLayout(this);
        exRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView fortune = button("🔮 随机运势");
        fortune.setOnClickListener(v ->
                new AlertDialog.Builder(this, R.style.AppDialog).setTitle(Ico.s(this, "🔮 今日运势"))
                        .setMessage(Ico.s(this, com.weng.weng.Extras.fortune()))
                        .setPositiveButton("关闭", null).show());
        exRow.addView(fortune);
        exRow.addView(gapW(6));
        TextView aiFortune = button("🤖 AI 运势");
        aiFortune.setOnClickListener(v -> {
            if (PetService.instance != null && PetService.instance.apiBase != null
                    && !PetService.instance.apiBase.trim().isEmpty()
                    && PetService.instance.apiKey != null && !PetService.instance.apiKey.trim().isEmpty()) {
                PetService.instance.aiChat("用户想让你帮他看看今天的运势（"
                        + new java.text.SimpleDateFormat("yyyy年M月d日", java.util.Locale.US).format(new java.util.Date()) + "），"
                        + "请用人设风格预测一下他今天的运势，用简短的一两句话说就好");
                Toast.makeText(this, "已发送给宠物，它正在想…", Toast.LENGTH_SHORT).show();
            } else {
                // 没配 API → 退回本地随机
                new AlertDialog.Builder(this, R.style.AppDialog).setTitle(Ico.s(this, "🔮 今日运势（随机）"))
                        .setMessage(Ico.s(this, "⚠ 未配置 AI 接口，使用随机运势。\n\n"
                                + com.weng.weng.Extras.fortune()))
                        .setPositiveButton("关闭", null).show();
            }
        });
        exRow.addView(aiFortune);
        exRow.addView(gapW(6));
        TextView guide = button("📖 饲养指南");
        guide.setOnClickListener(v ->
                new AlertDialog.Builder(this, R.style.AppDialog).setTitle("📖 饲养指南")
                        .setMessage(com.weng.weng.Extras.guideText()).setPositiveButton("懂了", null).show());
        exRow.addView(guide);
        exCard.addView(exRow);
        exCard.addView(gap(8));
        LinearLayout exRow2 = new LinearLayout(this);
        exRow2.setOrientation(LinearLayout.HORIZONTAL);
        TextView ach = button("🏆 成就/回忆录");
        ach.setOnClickListener(v -> showAchievements());
        exRow2.addView(ach);
        exRow2.addView(gapW(8));
        TextView resetTut = button("🔁 重置教程");
        resetTut.setOnClickListener(v -> {
            com.weng.weng.DataStore.putBool("tutorialDone", false);
            Toast.makeText(this, "下次启动会重新播放教程", Toast.LENGTH_SHORT).show();
        });
        exRow2.addView(resetTut);
        exCard.addView(exRow2);
        root.addView(exCard);
        root.addView(gap(10));

        // ============ 交互与感知卡片 ============
        root.addView(cardLabel("🖐 交互与感知"));
        LinearLayout ixCard = card();

        // 惯性档位
        ixCard.addView(rowLabel("甩动惯性（松手后滑行衰减）"));
        LinearLayout glideRow = new LinearLayout(this);
        glideRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] lvNames = {"关", "轻", "中", "强"};
        glideBtns = new TextView[4];
        for (int i = 0; i < 4; i++) {
            final int lv = i;
            TextView b = button(lvNames[i]);
            b.setOnClickListener(v -> {
                if (PetService.instance != null) {
                    PetService.instance.setGlideLevel(lv);
                    refresh();
                }
            });
            glideRow.addView(b);
            if (i < 3) glideRow.addView(gapW(6));
            glideBtns[i] = b;
        }
        ixCard.addView(glideRow);
        ixCard.addView(gap(8));

        // 拉手开关
        LinearLayout tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabBtn = button("💬 屏幕拉手：开");
        tabBtn.setOnClickListener(v -> {
            if (PetService.instance == null) return;
            boolean on = !com.weng.weng.DataStore.getBool("tabHandle", true);
            PetService.instance.setTabHandleVisible(on);
            refresh();
        });
        tabRow.addView(tabBtn);
        tabRow.addView(gapW(8));
        senseBtn = button("👁 App感知：?");
        senseBtn.setOnClickListener(v -> {
            boolean on = !com.weng.weng.DataStore.getBool("appSense", true);
            com.weng.weng.DataStore.putBool("appSense", on);
            if (on) {
                // 引导去系统授权"使用情况访问权限"
                new AlertDialog.Builder(this, R.style.AppDialog)
                        .setTitle("App 感知说明")
                        .setMessage("蚊子只读取「当前打开的应用名字」（不读内容）。\n\n要让它生效，请在接下来的系统页面里找到 " + getString(R.string.app_name) + "，允许「使用情况访问权限」。")
                        .setPositiveButton("去授权", (d, w) -> startActivity(new Intent(
                                android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)))
                        .setNegativeButton("取消", null)
                        .show();
            }
            refresh();
        });
        tabRow.addView(senseBtn);
        ixCard.addView(tabRow);
        ixCard.addView(gap(6));
        // jump 模式（也可以在屏幕右缘拉手里快捷切换）
        jumpModeBtn = button("🏃 jump模式：关");
        jumpModeBtn.setOnClickListener(v -> {
            if (PetService.instance != null) {
                PetService.instance.toggleJumpMode();
                refresh();
            }
        });
        ixCard.addView(jumpModeBtn);
        root.addView(ixCard);
        root.addView(gap(10));

        // ============ 主动搭话节奏卡片 ============
        root.addView(cardLabel("⏰ 主动搭话节奏"));
        LinearLayout chatRhythmCard = card();

        chatRhythmCard.addView(rowLabel("基准间隔（每隔 n 分钟主动来搭理你一次）"));
        LinearLayout gapRow = new LinearLayout(this);
        gapRow.setOrientation(LinearLayout.HORIZONTAL);
        chatGapSlider = new android.widget.SeekBar(this);
        chatGapSlider.setMax(115);                       // 5 ~ 120 分钟
        chatGapSlider.setProgress(clampInt(Math.round(DataStore.getFloat("chatGapMin", 10f)) - 5, 0, 115));
        gapRow.addView(chatGapSlider,
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        chatGapVal = valueLabel();
        gapRow.addView(chatGapVal);
        chatGapSlider.setOnSeekBarChangeListener(seeker(() -> {
            DataStore.putFloat("chatGapMin", chatGapSlider.getProgress() + 5f);
            chatGapVal.setText((chatGapSlider.getProgress() + 5) + " 分");
        }));
        chatRhythmCard.addView(gapRow);

        chatRhythmCard.addView(rowLabel("动态抖动（在这个间隔上下浮动，避免每次都一样准点）"));
        LinearLayout jitRow = new LinearLayout(this);
        jitRow.setOrientation(LinearLayout.HORIZONTAL);
        chatJitSlider = new android.widget.SeekBar(this);
        chatJitSlider.setMax(100);
        chatJitSlider.setProgress(clampInt(DataStore.getInt("chatJitter", 50), 0, 100));
        jitRow.addView(chatJitSlider,
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        chatJitVal = valueLabel();
        jitRow.addView(chatJitVal);
        chatJitSlider.setOnSeekBarChangeListener(seeker(() -> {
            DataStore.putInt("chatJitter", chatJitSlider.getProgress());
            chatJitVal.setText("±" + chatJitSlider.getProgress() + "%");
        }));
        chatRhythmCard.addView(jitRow);

        chatRhythmCard.addView(rowLabel("每天最多主动说几条（0 ＝ 不限）"));
        LinearLayout capRow = new LinearLayout(this);
        capRow.setOrientation(LinearLayout.HORIZONTAL);
        chatCapSlider = new android.widget.SeekBar(this);
        chatCapSlider.setMax(60);
        chatCapSlider.setProgress(clampInt(DataStore.getInt("chatDailyCap", 0), 0, 60));
        capRow.addView(chatCapSlider,
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        chatCapVal = valueLabel();
        capRow.addView(chatCapVal);
        chatCapSlider.setOnSeekBarChangeListener(seeker(() -> {
            DataStore.putInt("chatDailyCap", chatCapSlider.getProgress());
            chatCapVal.setText(chatCapSlider.getProgress() == 0 ? "不限" : chatCapSlider.getProgress() + " 条");
        }));
        chatRhythmCard.addView(capRow);

        chatCountVal = small("#777777", Gravity.LEFT);
        chatCountVal.setPadding(dp(4), dp(2), 0, dp(2));
        chatRhythmCard.addView(chatCountVal);
        root.addView(chatRhythmCard);
        root.addView(gap(10));

        // ============ 更新与关于卡片 ============
        root.addView(cardLabel("⚙️ 更新与关于"));
        LinearLayout aboutCard = card();
        LinearLayout aboutRow = new LinearLayout(this);
        aboutRow.setOrientation(LinearLayout.HORIZONTAL);
        updBtn = button("🔄 检查更新");
        updBtn.setOnClickListener(v -> onCheckUpdateClicked());
        aboutRow.addView(updBtn);
        aboutRow.addView(gapW(8));
        TextView bootBtn = button("📲 开机自启：关");
        bootBtn.setOnClickListener(v -> {
            boolean on = !com.weng.weng.DataStore.getBool("bootAuto", false);
            com.weng.weng.DataStore.putBool("bootAuto", on);
            bootBtn.setText(Ico.s(this, on ? "📲 开机自启：开" : "📲 开机自启：关"));
            Toast.makeText(this, on ? "已开启（重启后生效）" : "已关闭", Toast.LENGTH_SHORT).show();
        });
        if (com.weng.weng.DataStore.getBool("bootAuto", false)) bootBtn.setText(Ico.s(this, "📲 开机自启：开"));
        aboutRow.addView(bootBtn);
        aboutRow.addView(gapW(8));
        TextView quit = button("✖ 退出");
        quit.setOnClickListener(v -> {
            if (PetService.instance != null) PetService.instance.stopSelf();
            finish();
        });
        aboutRow.addView(quit);
        aboutCard.addView(aboutRow);
        aboutCard.addView(gap(6));
        // 崩溃日志查看器：Android 11+ 的 Android/data 没法用文件管理器浏览，只能在 App 内看
        TextView crashBtn = button("⚠ 崩溃日志");
        crashBtn.setOnClickListener(v -> showCrashLog());
        aboutCard.addView(crashBtn);
        aboutCard.addView(gap(6));
        TextView about = small("#AAAAAA", Gravity.CENTER);
        about.setText(Edition.CUSTOM_SKIN ? "桌宠 2.0 · 人设与形象都可自定义" : "嗡嗡嗡手机版 · 还原版");
        aboutCard.addView(about);
        root.addView(aboutCard);
        root.addView(gap(10));

        // ============ Bug 反馈 & 建议许愿（接在崩溃日志下面，折叠栏） ============
        fbFoldTitle = new TextView(this);
        fbFoldTitle.setText(Ico.s(this, "▶ 💬 Bug 反馈 & 建议许愿"));
        fbFoldTitle.setTextSize(13);
        fbFoldTitle.setTypeface(Typeface.DEFAULT_BOLD);
        fbFoldTitle.setTextColor(Color.parseColor("#666666"));
        fbFoldTitle.setPadding(dp(4), dp(6), 0, dp(6));
        root.addView(fbFoldTitle);

        LinearLayout fbCard = card();
        fbCard.addView(rowLabel("哪里不对、想要什么功能，写下来发给我。内容会自动带上版本号、机型和最近的崩溃日志。"));
        fbCard.addView(gap(4));
        fbCard.addView(rowLabel("🐛 Bug 反馈：哪里不对、怎么复现"));
        fbBug = fbInput("比如：点开手账之后，屏幕底部会多出一条黑边…");
        fbCard.addView(fbBug, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(76)));
        fbCard.addView(gap(8));
        fbCard.addView(rowLabel("🌠 建议 / 许愿：想要什么功能、什么玩法"));
        fbWish = fbInput("比如：希望它能记住我昨天说过的话…");
        fbCard.addView(fbWish, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(76)));
        fbCard.addView(gap(8));
        LinearLayout fbRow = new LinearLayout(this);
        fbRow.setOrientation(LinearLayout.HORIZONTAL);
        fbRow.setGravity(Gravity.CENTER_VERTICAL);
        fbName = new EditText(this);
        fbName.setHint("署名");
        fbName.setText(DataStore.getString("fbSign", SIGN_DEFAULT));
        fbName.setTextSize(12);
        fbName.setMaxLines(1);
        fbName.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        fbName.setTextColor(Color.parseColor("#111111"));
        fbName.setHintTextColor(Color.parseColor("#999999"));
        fbName.setPadding(dp(10), dp(8), dp(10), dp(8));
        fbName.setMinHeight(dp(36));
        fbName.setMinimumHeight(dp(36));
        fbName.setBackground(box());
        fbRow.addView(fbName, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        fbRow.addView(gapW(6));
        TextView fbSend = button("📧 发送");
        fbSend.setOnClickListener(v -> sendFeedback());
        fbRow.addView(fbSend);
        fbCard.addView(fbRow);
        fbCard.addView(gap(6));
        TextView fbTip = small("#AAAAAA", Gravity.LEFT);
        fbTip.setText("发到 " + FEEDBACK_MAIL + "：点「发送」会先把内容复制到剪贴板兜底，再帮你打开邮件 App");
        fbCard.addView(fbTip);

        fbFoldBody = fbCard;
        fbFoldBody.setVisibility(View.GONE);   // 默认收起
        root.addView(fbFoldBody);
        fbFoldTitle.setOnClickListener(v -> toggleFeedbackFold());

        ScrollView page = new ScrollView(this);
        // 避免内容不足一屏时露出窗口底色（见 res/values/styles.xml 的说明）
        page.setBackgroundColor(Color.parseColor("#F5F4EF"));
        page.setFillViewport(true);
        page.addView(root);
        setContentView(page);
    }

    /** 保存接口设置（保存即测试）：Key 留空＝沿用已保存的（避免把空串/打码串写进存档）。
     *  保存前地址自动补 /v1 版本段（没填且需要时）；保存后自动测试连通性。 */
    private void saveApi() {
        saveApiQuiet();
        // 保存即测试：GET /models 一次请求同时验证「地址对不对 + Key 能不能用」，并返回可用模型列表
        testApiConnection();
    }

    /** 只落盘不测试（拉取模型按钮走这里：它自己马上就会发请求，再测一次就重复了） */
    private void saveApiQuiet() {
        if (PetService.instance == null) return;
        // 地址规范化：没有 /v1（或 /v2 /v3…）版本段时自动补 /v1（详见 PetService.normalizeBase）
        String base = apiBaseField.getText().toString().trim();
        String fixed = PetService.normalizeBase(base);
        if (!fixed.equals(base)) {
            apiBaseField.setText(fixed);
            apiBaseField.setSelection(fixed.length());
            Toast.makeText(this, "已自动补全为 " + fixed + "（服务商路径特殊？末尾加 # 可强制原样）", Toast.LENGTH_LONG).show();
            base = fixed;
        }
        String key = apiKeyField.getText().toString().trim();
        PetService.instance.setApi(base,
                key.isEmpty() ? PetService.instance.apiKey : key,
                apiModelField.getText().toString().trim());
    }

    /** 保存后自动测试连接：成功顺带检查模型名是否在服务商列表里（不在就提醒重新拉取选择） */
    private void testApiConnection() {
        final android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("正在测试连接…");
        pd.setCancelable(false);
        pd.show();
        final String model = apiModelField.getText().toString().trim();
        PetService.instance.fetchModels((models, error) -> {
            // 网络请求最长几十秒，期间用户可能已关掉设置页（见 fetchModelsDialog 的同款守卫）
            if (isFinishing() || isDestroyed()) return;
            dismissQuietly(pd);
            if (error != null || models == null || models.isEmpty()) {
                Toast.makeText(this, "已保存，但连接测试失败：" + (error == null ? "列表为空" : error)
                        + "\n请对照新手教程检查地址和 Key", Toast.LENGTH_LONG).show();
                return;
            }
            String msg = "已保存，连接正常（发现 " + models.size() + " 个模型）";
            if (!model.isEmpty() && !models.contains(model)) {
                msg += "\n注意：模型「" + model + "」不在列表里，建议点 📡拉取 重新选择";
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }

    /** API 配置新手教程弹窗（长文本套 ScrollView，白底弹窗见 AppDialog） */
    private void showApiTutorial() {
        ScrollView sv = new ScrollView(this);
        TextView tv = new TextView(this);
        tv.setTextSize(13);
        tv.setTextColor(Color.parseColor("#111111"));
        tv.setPadding(dp(18), dp(4), dp(18), dp(4));
        tv.setText(Ico.s(this, "三步配好 AI：\n\n"
                + "① 接口地址 —— 填服务商给你的地址，一般长这样：\n"
                + "    https://api.xxx.com/v1\n"
                + "  · 填到 /v1 就行，后面的 /chat/completions 会自动补全\n"
                + "  · 忘了填 /v1？保存时会自动帮你补上\n"
                + "  · 服务商路径特殊、不想被自动补全：地址末尾加 #\n\n"
                + "② API Key —— 在服务商网站注册 → 创建密钥（多为 sk- 开头）→ 复制粘贴到这里。\n"
                + "  留空＝沿用已保存的 Key。\n\n"
                + "③ 模型名 —— 点「📡 拉取」列出你这个 Key 能用的全部模型，搜一下点一个，比手打靠谱。\n\n"
                + "填完点「💾 保存并测试」，App 会自动测连接：\n"
                + "  · 连接正常（发现 N 个模型）＝配置成功\n"
                + "  · 401 ＝ Key 不对或过期\n"
                + "  · 404 ＝ 地址不对（多半是 /v1 的问题）\n"
                + "  · 超时 ＝ 网络不通（手机要能访问服务商）\n\n"
                + "模型名不在列表里也会提醒你重新拉取选择。"));
        sv.addView(tv);
        new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle(Ico.s(this, "🔌 API 配置教程"))
                .setView(sv)
                .setPositiveButton("懂了", null)
                .show();
    }

    // ================= API 预设（多个配置一键切换） =================

    /** 当前生效的 base/key/model 是否正好等于某个预设，是则返回预设名，否则「自定义」 */
    private String currentPresetName() {
        if (PetService.instance == null) return "未配置";
        String b = PetService.instance.apiBase == null ? "" : PetService.instance.apiBase.trim();
        String k = PetService.instance.apiKey == null ? "" : PetService.instance.apiKey.trim();
        String m = PetService.instance.apiModel == null ? "" : PetService.instance.apiModel.trim();
        java.util.List<org.json.JSONObject> ps = DataStore.arr("apiPresets");
        for (org.json.JSONObject p : ps) {
            if (b.equals(p.optString("base", "").trim())
                    && k.equals(Crypto.decrypt(p.optString("key", "")).trim())
                    && m.equals(p.optString("model", "").trim())) {
                return p.optString("name", "");
            }
        }
        return "自定义";
    }

    private void updatePresetLabel() {
        if (presetLabel != null) presetLabel.setText("预设：" + currentPresetName());
    }

    /** 把当前输入框里的地址/Key/模型存成一个命名预设（同名覆盖）。先落盘规范化，再取实际生效值存。 */
    private void saveAsPreset() {
        if (PetService.instance == null) return;
        saveApiQuiet();
        final String base = PetService.instance.apiBase == null ? "" : PetService.instance.apiBase.trim();
        final String key = PetService.instance.apiKey == null ? "" : PetService.instance.apiKey.trim();
        final String model = PetService.instance.apiModel == null ? "" : PetService.instance.apiModel.trim();
        if (base.isEmpty() || key.isEmpty()) {
            Toast.makeText(this, "请先填好接口地址和 API Key", Toast.LENGTH_SHORT).show();
            return;
        }
        final EditText nameIn = new EditText(this);
        nameIn.setHint("预设名（如：硅基流动、本地 Ollama）");
        nameIn.setTextSize(13);
        nameIn.setTextColor(Color.parseColor("#111111"));
        nameIn.setHintTextColor(Color.parseColor("#999999"));
        nameIn.setPadding(dp(10), dp(8), dp(10), dp(8));
        nameIn.setMinHeight(dp(36));
        nameIn.setMinimumHeight(dp(36));
        nameIn.setBackground(box());
        new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle(Ico.s(this, "📌 存为预设"))
                .setView(nameIn)
                .setPositiveButton("保存", (d, w) -> {
                    String name = nameIn.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "预设名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    java.util.List<org.json.JSONObject> ps = DataStore.arr("apiPresets");
                    org.json.JSONObject target = null;
                    for (org.json.JSONObject p : ps) {
                        if (name.equals(p.optString("name", ""))) { target = p; break; }
                    }
                    if (target == null) { target = new org.json.JSONObject(); ps.add(target); }
                    try {
                        target.put("name", name);
                        target.put("base", base);
                        target.put("key", Crypto.encrypt(key));
                        target.put("model", model);
                    } catch (Exception ignored) {
                    }
                    DataStore.saveArr("apiPresets", ps);
                    updatePresetLabel();
                    Toast.makeText(this, "已存预设「" + name + "」", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /** 弹窗列出所有预设，点「用」一键切换，点「删」移除 */
    private void switchPreset() {
        java.util.List<org.json.JSONObject> ps = DataStore.arr("apiPresets");
        if (ps.isEmpty()) {
            Toast.makeText(this, "还没有预设。先填好配置，点「📌 存为预设」保存一个", Toast.LENGTH_LONG).show();
            return;
        }
        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dp(4), 0, dp(4));
        for (org.json.JSONObject p : ps) {
            final String name = p.optString("name", "");
            final String base = p.optString("base", "");
            final String key = Crypto.decrypt(p.optString("key", ""));
            final String model = p.optString("model", "");
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));
            TextView nameTv = new TextView(this);
            nameTv.setText(name + "  ·  " + model);
            nameTv.setTextSize(13);
            nameTv.setTextColor(Color.parseColor("#111111"));
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(nameTv, nlp);
            row.addView(gapW(6));
            TextView useBtn = button("用");
            useBtn.setOnClickListener(v -> {
                applyPreset(name, base, key, model);
                if (presetDlg != null) dismissQuietly(presetDlg);
            });
            row.addView(useBtn);
            row.addView(gapW(6));
            TextView delBtn = button("删");
            delBtn.setOnClickListener(v -> confirmDeletePreset(name));
            row.addView(delBtn);
            list.addView(row);
        }
        sv.addView(list);
        presetDlg = new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle(Ico.s(this, "🔁 切换预设"))
                .setView(sv)
                .setNegativeButton("关闭", null)
                .show();
    }

    /** 应用某个预设：写入存档（setApi 会落盘）+ 回填输入框 + 更新标签。Key 框保持留空（沿用已保存的，不把明文摆出来）。 */
    private void applyPreset(String name, String base, String key, String model) {
        if (PetService.instance == null) return;
        PetService.instance.setApi(base, key, model);
        apiBaseField.setText(base);
        apiKeyField.setText("");
        apiModelField.setText(model);
        updatePresetLabel();
        Toast.makeText(this, "已切换到「" + name + "」", Toast.LENGTH_SHORT).show();
    }

    private void confirmDeletePreset(String name) {
        new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle(Ico.s(this, "删除预设"))
                .setMessage("确定删除预设「" + name + "」？")
                .setPositiveButton("删除", (d, w) -> {
                    java.util.List<org.json.JSONObject> ps = DataStore.arr("apiPresets");
                    for (int i = 0; i < ps.size(); i++) {
                        if (name.equals(ps.get(i).optString("name", ""))) { ps.remove(i); break; }
                    }
                    DataStore.saveArr("apiPresets", ps);
                    updatePresetLabel();
                    if (presetDlg != null) dismissQuietly(presetDlg);
                    Toast.makeText(this, "已删除「" + name + "」", Toast.LENGTH_SHORT).show();
                    switchPreset();   // 重新打开列表，反映删除结果
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /** 拉取模型列表：先把输入框里的地址/Key/模型落盘（不测试），再 GET /models，弹窗筛选选择 */
    private void fetchModelsDialog() {
        if (PetService.instance == null) return;
        saveApiQuiet();
        final android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("正在拉取模型列表…");
        pd.setCancelable(false);
        pd.show();
        PetService.instance.fetchModels((models, error) -> {
            // 拉取是网络请求（最长几十秒），这期间用户完全可能退回桌面、把设置页关掉。
            // 对着已经销毁的 Activity 调 dismiss()/show() 会抛
            // "View not attached to window manager" 或 BadTokenException —— 两版都会崩。
            if (isFinishing() || isDestroyed()) return;
            dismissQuietly(pd);
            if (error != null || models == null || models.isEmpty()) {
                Toast.makeText(this, "拉取失败：" + (error == null ? "列表为空" : error), Toast.LENGTH_LONG).show();
                return;
            }
            pickModelDialog(models);
        });
    }

    /** 安全关闭对话框：Activity 已销毁时 dismiss 会抛异常 */
    private void dismissQuietly(android.app.Dialog d) {
        try { if (d != null && d.isShowing()) d.dismiss(); } catch (Throwable ignored) {}
    }

    /** 模型选择弹窗：顶部搜索框实时筛选，点击条目回填到模型输入框 */
    private void pickModelDialog(final java.util.List<String> models) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(4));
        final EditText search = new EditText(this);
        search.setHint("🔍 输入关键字筛选（如 glm / deepseek）");
        search.setTextSize(13);
        search.setMaxLines(1);
        search.setBackground(box());
        search.setPadding(dp(10), dp(8), dp(10), dp(8));
        search.setMinHeight(dp(36));
        search.setMinimumHeight(dp(36));
        box.addView(search);
        box.addView(gap(6));
        final LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        ScrollView sc = new ScrollView(this);
        sc.addView(list);
        box.addView(sc, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Math.min(dp(360), dp(42) * Math.min(models.size(), 10))));
        Runnable rebuild = () -> {
            list.removeAllViews();
            String kw = search.getText().toString().trim().toLowerCase();
            int shown = 0;
            for (final String m : models) {
                if (!kw.isEmpty() && !m.toLowerCase().contains(kw)) continue;
                TextView t = new TextView(this);
                t.setText(m);
                t.setTextSize(13);
                t.setTextColor(Color.parseColor("#111111"));
                t.setPadding(dp(10), dp(11), dp(10), dp(11));
                t.setBackground(box());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = dp(4);
                t.setLayoutParams(lp);
                t.setOnClickListener(v -> {
                    apiModelField.setText(m);
                    apiModelField.setSelection(m.length());
                    Toast.makeText(this, "已选择：" + m, Toast.LENGTH_SHORT).show();
                    dismissQuietly(pickDlg);
                });
                list.addView(t);
                shown++;
            }
            if (shown == 0) {
                TextView none = small("#999999", Gravity.CENTER);
                none.setText("没有匹配的模型");
                list.addView(none);
            }
        };
        rebuild.run();
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { rebuild.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        pickDlg = new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle("📡 选择模型（共 " + models.size() + " 个）")
                .setView(box)
                .setNegativeButton("取消", null)
                .show();
    }

    /** 成就墙 + 剧情回忆录弹窗 */
    private void showAchievements() {
        StringBuilder sb = new StringBuilder("—— 已解锁 ——\n");
        boolean any = false;
        for (String[] a : com.weng.weng.Extras.ACHIEVEMENTS) {
            boolean got;
            switch (a[0]) {
                case "灵魂契约 💖": got = com.weng.weng.DataStore.getBool("ach_soul", false); break;
                case "自律达人 📅": got = com.weng.weng.DataStore.getBool("ach_streak", false); break;
                case "血祭之王 🩸": got = com.weng.weng.DataStore.getBool("ach_blood", false); break;
                case "专注大师 🍅": got = com.weng.weng.DataStore.getBool("ach_focus", false); break;
                case "水润少年 💧": got = com.weng.weng.DataStore.getBool("ach_water", false); break;
                case "话痨之友 💬": got = com.weng.weng.DataStore.getBool("ach_talk", false); break;
                default: got = false;
            }
            if (got) {
                sb.append("🏆 ").append(a[0]).append("　").append(a[1]).append('\n');
                any = true;
            }
        }
        if (!any) sb.append("（还没有，快去解锁！）\n");
        sb.append("\n—— 剧情回忆录 ——\n");
        java.util.List<org.json.JSONObject> log = com.weng.weng.DataStore.arr("theaterLog");
        if (log.isEmpty()) sb.append("（还没有小剧场记录）");
        else {
            int from = Math.max(0, log.size() - 10);
            for (int i = log.size() - 1; i >= from; i--) {
                org.json.JSONObject o = log.get(i);
                sb.append('·').append(o.optString("t", "")).append(' ')
                        .append(o.optString("choice", "")).append("（好感 ")
                        .append(o.optInt("delta", 0) >= 0 ? "+" : "").append(o.optInt("delta", 0)).append("）\n");
            }
        }
        sb.append("\n—— 心情走势 ——\n").append(com.weng.weng.Emotion.chart());
        new AlertDialog.Builder(this, R.style.AppDialog).setTitle("🏆 成就与回忆").setMessage(sb.toString())
                .setPositiveButton("关闭", null).show();
    }

    private EditText apiInput(String hint, String text) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(text);
        e.setTextSize(12);
        e.setMaxLines(1);
        e.setTextColor(Color.parseColor("#111111"));
        e.setPadding(dp(10), dp(8), dp(10), dp(8));
        // 压掉系统默认的最小高度（约 48dp），和 button() 统一成 36dp —— 并排时边框才对得齐
        e.setMinHeight(dp(36));
        e.setMinimumHeight(dp(36));
        e.setBackground(box());
        return e;
    }

    private void editAffectionDialog() {
        if (PetService.instance == null) return;
        final EditText in = new EditText(this);
        in.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        in.setText(String.valueOf(PetService.instance.affection));
        new AlertDialog.Builder(this, R.style.AppDialog)
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

    @Override
    protected void onDestroy() {
        // 页面要没了：停掉每秒刷新，并把可能还开着的对话框收干净，
        // 免得网络回调晚一步回来时对着一堆已销毁的窗口操作。
        if (PetService.instance != null) PetService.instance.handler.removeCallbacks(uiRefresher);
        dismissQuietly(pickDlg);
        pickDlg = null;
        super.onDestroy();
    }

    /** 点「检查更新」：存档里已有待更新信息就直接弹确认框，否则先查询、查到再弹 */
    private void onCheckUpdateClicked() {
        if (PetService.instance == null) {
            Toast.makeText(this, "宠物还没启动，稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        // 走 pendingUpdateTag()：它会把「不比当前版本新」的残留记录清掉，
        // 避免升级完之后按钮还显示「有新版本」（见 PetService.pendingUpdateTag 的说明）
        String tag = PetService.instance.pendingUpdateTag();
        String url = DataStore.getString(PetService.K_UPD_URL, "");
        if (!tag.isEmpty() && !url.isEmpty()) {
            showUpdateDialog(tag, DataStore.getString(PetService.K_UPD_NOTES, ""),
                    DataStore.getLong(PetService.K_UPD_SIZE, 0), url);
            return;
        }
        Toast.makeText(this, "检查更新中…", Toast.LENGTH_SHORT).show();
        PetService.instance.checkUpdate(true, (hasNew, t, notes, size, u, error) -> {
            if (isFinishing() || isDestroyed()) return;
            if (error != null) {
                Toast.makeText(this, "检查失败：" + error, Toast.LENGTH_LONG).show();
                return;
            }
            if (!hasNew) {
                Toast.makeText(this, "已是最新版本 v" + PetService.instance.curVersion(), Toast.LENGTH_SHORT).show();
                return;
            }
            showUpdateDialog(t, notes, size, u);
        });
    }

    /** 更新确认框：先把本次更新内容摆出来，用户点「下载并安装」才开始下载 */
    private void showUpdateDialog(String tag, String notes, long size, String url) {
        String body = (notes == null || notes.trim().isEmpty())
                ? "（这个版本没有写更新说明）" : notes.trim();
        // 说明可能很长，限制一下字数，免得弹窗撑满整屏（完整版在 GitHub 更新页面上）
        final int MAX_CHARS = 600;
        if (body.length() > MAX_CHARS) {
            body = body.substring(0, MAX_CHARS) + "\n……（剩余内容见 GitHub 更新页面）";
        }
        ScrollView sv = new ScrollView(this);
        TextView tv = new TextView(this);
        tv.setTextSize(13);
        tv.setTextColor(Color.parseColor("#111111"));
        tv.setPadding(dp(18), dp(4), dp(18), dp(4));
        tv.setText("最新版本：" + tag + (size > 0 ? "（" + (size / 1024) + " KB）" : "") + "\n\n"
                + "本次更新内容：\n" + body + "\n\n"
                + "点「下载并安装」后才会开始下载。");
        sv.addView(tv);
        new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle("发现新版本")
                .setView(sv)
                .setPositiveButton("下载并安装", (d, w) -> {
                    Toast.makeText(this, "开始下载，完成后会拉起安装", Toast.LENGTH_SHORT).show();
                    PetService.instance.downloadAndInstall(url);
                })
                .setNegativeButton("以后再说", null)
                .show();
    }

    private void refresh() {
        refreshChatSettings();
        refreshSkinRows();
        refreshPersonaRow();
        if (updBtn != null) {
            // 同样走校验接口：已装上新版就别再显示「有新版本」
            String ut = (PetService.instance == null) ? "" : PetService.instance.pendingUpdateTag();
            updBtn.setText(Ico.s(this, ut.isEmpty() ? "🔄 检查更新" : "⬆ 有新版本 " + ut + "，点此更新"));
        }
        if (PetService.instance == null) {
            affLabel.setText("宠物未运行");
            return;
        }
        affLabel.setText(Ico.s(this, "❤ 好感度 " + PetService.instance.affection + "　·　第 " + PetService.instance.daysCount() + " 天"));
        if (emoLine != null) emoLine.setText(Ico.s(this, "🧠 " + PetService.instance.emo.describe()));
        if (feedLine != null) feedLine.setText(Ico.s(this, "🩸 " + PetService.instance.donationStatusText()));
        if (jumpModeBtn != null) jumpModeBtn.setText(Ico.s(this,
                PetService.instance.isJumpMode() ? "🏃 jump模式：开" : "🏃 jump模式：关"));
        refreshSatietyBar();
        if (prankScore != null) prankScore.setText(Ico.s(this, "📊 " + PetService.instance.prankScoreText()));
        if (prankBtn != null) prankBtn.setText(Ico.s(this, PetService.instance.prankMode ? "🕊 结束整蛊" : "🦟 注入并隐藏"));
        // 惯性档位高亮：选中档加粗+标 ●
        int lv = PetService.instance.glideLevel();
        String[] lvNames = {"关", "轻", "中", "强"};
        for (int i = 0; i < glideBtns.length; i++) {
            if (glideBtns[i] != null)
                glideBtns[i].setText(Ico.s(this, (i == lv ? "● " : "") + lvNames[i]));
        }
        if (tabBtn != null) tabBtn.setText(Ico.s(this, com.weng.weng.DataStore.getBool("tabHandle", true) ? "💬 屏幕拉手：开" : "💬 屏幕拉手：关"));
        if (senseBtn != null) {
            boolean on = com.weng.weng.DataStore.getBool("appSense", true);
            String cur = PetService.instance.currentAppLabelPublic();
            senseBtn.setText(Ico.s(this, on ? ("👁 App感知：开" + (cur == null ? "（未授权）" : "（当前:" + cur + "）")) : "👁 App感知：关"));
        }
        verLabel.setText("v" + PetService.instance.curVersion() + (PetService.instance.dnd ? "（勿扰中）" : "") + (PetService.instance.workMode ? "（工作中）" : ""));
        dndBtn.setText(Ico.s(this, PetService.instance.dnd ? "🌙 勿扰中" : "🌙 勿扰"));
        workBtn.setText(Ico.s(this, PetService.instance.workMode ? "📚 工作中" : "📚 工作"));
        speedLabel.setText("×" + String.format(java.util.Locale.US, "%.1f", PetService.instance.getSpeedMul()));
        if (affVal != null) affVal.setText(String.valueOf(PetService.instance.affection));
    }

    // ================= 崩溃日志 =================

    private java.io.File crashLogFile() {
        java.io.File dir = getExternalFilesDir(null);
        if (dir == null) dir = getFilesDir();
        return new java.io.File(dir, "crash_log.txt");
    }

    private String readCrashLog() {
        java.io.File f = crashLogFile();
        if (!f.exists()) {
            return "还没有崩溃记录～\n\n（日志会写到：" + f.getAbsolutePath() + "）";
        }
        try {
            java.io.FileInputStream in = new java.io.FileInputStream(f);
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            in.close();
            String s = bos.toString("UTF-8");
            if (s.length() > 20000) s = "…（太长，只显示最后 20000 字）\n" + s.substring(s.length() - 20000);
            return s;
        } catch (Exception e) {
            return "读取失败：" + e;
        }
    }

    private void showCrashLog() {
        final ScrollView sv = new ScrollView(this);
        TextView t = new TextView(this);
        t.setTextSize(11);
        t.setTextColor(Color.parseColor("#111111"));
        t.setPadding(dp(12), dp(10), dp(12), dp(10));
        t.setTextIsSelectable(true);
        t.setText(readCrashLog());
        sv.addView(t);
        new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle("崩溃日志")
                .setView(sv)
                .setPositiveButton("关闭", null)
                .setNeutralButton("清空", (d, w) -> {
                    try { crashLogFile().delete(); } catch (Exception ignored) {}
                    Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // ================= Bug 反馈 / 建议许愿 =================

    /** 折叠栏：点标题展开/收起反馈与许愿区，箭头跟着切换 ▶/▼ */
    private void toggleFeedbackFold() {
        boolean show = fbFoldBody.getVisibility() != View.VISIBLE;
        fbFoldBody.setVisibility(show ? View.VISIBLE : View.GONE);
        fbFoldTitle.setText(Ico.s(this, (show ? "▼" : "▶") + " 💬 Bug 反馈 & 建议许愿"));
    }

    /** 反馈/许愿用的多行输入框（白底细黑边，和 App 里其它输入框同款） */
    private EditText fbInput(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(12);
        e.setGravity(Gravity.TOP | Gravity.LEFT);
        e.setTextColor(Color.parseColor("#111111"));
        e.setHintTextColor(Color.parseColor("#999999"));
        e.setPadding(dp(10), dp(8), dp(10), dp(8));
        e.setBackground(box());
        e.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        e.setVerticalScrollBarEnabled(true);
        return e;
    }

    /**
     * 点「发送」：先把整封内容复制到剪贴板兜底（手机没配邮箱也能自己去发），
     * 再调起邮件 App 把收件人/主题/正文填好，用户按一下发送就行。
     * 没有邮件 App 就退到系统分享面板，再不行就只提示邮箱地址。
     */
    private void sendFeedback() {
        String bug = fbBug.getText().toString().trim();
        String wish = fbWish.getText().toString().trim();
        String sign = fbName.getText().toString().trim();
        if (bug.isEmpty() && wish.isEmpty()) {
            Toast.makeText(this, "先写点内容再发吧～", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sign.isEmpty()) sign = SIGN_DEFAULT;
        DataStore.putString("fbSign", sign);          // 下次打开就记住这次的署名

        StringBuilder sb = new StringBuilder("—— " + getString(R.string.app_name) + " · 反馈 ——\n");
        if (!bug.isEmpty()) sb.append("\n【Bug 反馈】\n").append(bug).append('\n');
        if (!wish.isEmpty()) sb.append("\n【建议 / 许愿】\n").append(wish).append('\n');
        sb.append("\n【署名】").append(sign);
        sb.append("\n【版本】v").append(appVersion());
        sb.append("\n【包名】").append(getPackageName());
        sb.append("\n【机型】").append(android.os.Build.MANUFACTURER).append(' ')
                .append(android.os.Build.MODEL).append(" · Android ")
                .append(android.os.Build.VERSION.RELEASE).append(" (API ")
                .append(android.os.Build.VERSION.SDK_INT).append(')');
        String log = crashLogTail(2000);
        if (!log.isEmpty()) sb.append("\n\n【最近的崩溃日志】\n").append(log);
        final String body = sb.toString();

        copyToClipboard(body);

        String subject = getString(R.string.app_name) + "反馈（" + sign + "）";
        // mailto 太长部分邮件客户端会截断，超了就只预填前半段（完整版已进剪贴板）
        String mailBody = body.length() > 1800
                ? body.substring(0, 1800) + "\n…（内容较长，完整版已复制到剪贴板）" : body;
        try {
            Intent m = new Intent(Intent.ACTION_SENDTO);
            m.setData(android.net.Uri.parse("mailto:" + FEEDBACK_MAIL
                    + "?subject=" + urlEnc(subject) + "&body=" + urlEnc(mailBody)));
            startActivity(m);
            Toast.makeText(this, "已复制到剪贴板；在邮件里点发送就行", Toast.LENGTH_LONG).show();
        } catch (Exception noMail) {
            try {
                Intent s = new Intent(Intent.ACTION_SEND);
                s.setType("text/plain");
                s.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_MAIL});
                s.putExtra(Intent.EXTRA_SUBJECT, subject);
                s.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(s, "把反馈发出去"));
            } catch (Exception e) {
                Toast.makeText(this, "手机里没找到邮件 App，内容已复制，可手动发到 " + FEEDBACK_MAIL,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void copyToClipboard(String s) {
        try {
            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                    getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(android.content.ClipData.newPlainText("反馈", s));
        } catch (Exception ignored) {
        }
    }

    /** mailto 的参数要自己转义（URLEncoder 会把空格编成 +，邮件客户端不认，得换回 %20） */
    private static String urlEnc(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return "";
        }
    }

    /** 反馈邮件里附带的崩溃日志尾部；没有日志文件就返回空串 */
    private String crashLogTail(int max) {
        java.io.File f = crashLogFile();
        if (!f.exists()) return "";
        try {
            java.io.FileInputStream in = new java.io.FileInputStream(f);
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            in.close();
            String s = bos.toString("UTF-8").trim();
            if (s.length() > max) s = "…（只带最后 " + max + " 字）\n" + s.substring(s.length() - max);
            return s;
        } catch (Exception e) {
            return "";
        }
    }

    private String appVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "?";
        }
    }

    // ================= 主动搭话节奏 =================

    /** 同步三根滑块 + 今日已主动搭话条数 */
    private void refreshChatSettings() {
        if (chatGapSlider == null) return;
        int gap = clampInt(Math.round(DataStore.getFloat("chatGapMin", 10f)), 5, 120);        int jit = clampInt(DataStore.getInt("chatJitter", 50), 0, 100);
        int cap = clampInt(DataStore.getInt("chatDailyCap", 0), 0, 60);
        if (!chatDragging) {
            chatGapSlider.setProgress(gap - 5);
            chatJitSlider.setProgress(jit);
            chatCapSlider.setProgress(cap);
        }
        chatGapVal.setText(gap + " 分");
        chatJitVal.setText("±" + jit + "%");
        chatCapVal.setText(cap == 0 ? "不限" : cap + " 条");
        // 抖动后的实际区间，方便预览
        int lo = Math.max(1, Math.round(gap * (1f - jit / 100f)));
        int hi = Math.max(lo, Math.round(gap * (1f + jit / 100f)));
        int sent = DataStore.sp().getInt("chatCount", 0);
        chatCountVal.setText("实际触发 " + lo + " ~ " + hi + " 分钟一次　·　今天已主动搭话 "
                + sent + (cap == 0 ? " 条（不限量）" : "/" + cap + " 条"));
        // 跳跃高度
        int pct = clampInt(DataStore.getInt("jumpPct", 14), 4, 40);
        if (jumpPctSlider != null && !chatDragging) jumpPctSlider.setProgress(pct - 4);
        if (jumpPctVal != null) jumpPctVal.setText(pct + "%");
        // 站立高度
        int lift = clampInt(DataStore.getInt("standLift", 0), 0, 50);
        if (standLiftSlider != null && !chatDragging) standLiftSlider.setProgress(lift);
        if (standLiftVal != null) standLiftVal.setText(lift == 0 ? "贴底" : "离底 " + lift + "%");
    }

    private static int clampInt(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    /** 滑块右侧的数值标签 */
    private TextView valueLabel() {
        TextView t = new TextView(this);
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(Color.parseColor("#111111"));
        t.setGravity(Gravity.CENTER);
        t.setMinWidth(dp(64));
        return t;
    }

    /** 滑块监听：只在用户拖动时回调（refresh 回写进度不会反复触发） */
    private android.widget.SeekBar.OnSeekBarChangeListener seeker(final Runnable onChange) {
        return new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar sb, int p, boolean fromUser) {
                if (fromUser) onChange.run();
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar sb) { chatDragging = true; }
            @Override public void onStopTrackingTouch(android.widget.SeekBar sb) { chatDragging = false; }
        };
    }

    // ================= 饱食度可视化进度条 =================

    /** 搭一根饱食度条：外框=空槽，内部两个 View 按 饱食度 : (100-饱食度) 的权重分宽度 */
    private void buildSatietyBar(LinearLayout parent) {
        satLabel = small("#777777", Gravity.LEFT);
        satLabel.setPadding(dp(4), dp(2), 0, dp(2));
        parent.addView(satLabel);

        satBar = new LinearLayout(this);
        satBar.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable track = new GradientDrawable();
        track.setColor(Color.parseColor("#EDEBE3"));
        track.setCornerRadius(dp(3));
        track.setStroke(dp(1), Color.parseColor("#111111"));
        satBar.setBackground(track);
        int pad = dp(2);
        satBar.setPadding(pad, pad, pad, pad);

        satFill = new View(this);
        satRest = new View(this);
        satBar.addView(satFill, new LinearLayout.LayoutParams(0, dp(12), 1f));
        satBar.addView(satRest, new LinearLayout.LayoutParams(0, dp(12), 1f));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(2);
        lp.bottomMargin = dp(6);
        parent.addView(satBar, lp);
    }

    /** 刷新饱食度条：设置页开着时每秒刷一次，能看着它慢慢往下掉 */
    private void refreshSatietyBar() {
        if (satLabel == null || satFill == null || satRest == null) return;
        float sat = DataStore.getSatiety();
        int pct = Math.max(0, Math.min(100, (int) Math.round(sat)));
        satLabel.setText(Ico.s(this, "🍅 饱食度 " + pct + "/100 · " + DataStore.hungerText(sat)));

        int col = sat < 15f ? 0xFFE24B4A : (sat < 35f ? 0xFFEF9F27 : 0xFF639922);
        GradientDrawable fillBg = new GradientDrawable();
        fillBg.setColor(col);
        fillBg.setCornerRadius(dp(3));
        satFill.setBackground(fillBg);

        float rest = 100f - sat;
        satFill.setLayoutParams(new LinearLayout.LayoutParams(0, dp(12), Math.max(0.01f, sat)));
        satRest.setLayoutParams(new LinearLayout.LayoutParams(0, dp(12), Math.max(0.01f, rest)));
    }

    private TextView small(String color, int gravity) {        TextView t = new TextView(this);
        t.setTextSize(12);
        t.setTextColor(Color.parseColor(color));
        t.setGravity(gravity);
        return t;
    }

    private TextView cardLabel(String text) {
        TextView t = new TextView(this);
        t.setText(Ico.s(this, text));
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
        g.setCornerRadius(dp(3));
        g.setStroke(dp(1), Color.parseColor("#111111"));
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
        t.setText(Ico.s(this, text));
        t.setTextColor(Color.parseColor("#111111"));
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        // 边框到文字的留白收紧（原来 14/10，按钮显得太空）
        t.setPadding(dp(12), dp(6), dp(12), dp(6));
        // 统一最小高度：输入框（EditText）系统默认最小高度接近 48dp，按钮不撑到同一高度的话，
        // 两者并排时按钮会顶对齐、两条边框交错，看着就是「挤在一起、分不开」
        t.setMinHeight(dp(36));
        t.setMinimumHeight(dp(36));
        t.setBackground(box());
        return t;
    }

    private GradientDrawable box() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(3));
        g.setStroke(dp(1), Color.parseColor("#111111"));
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

    // ---------------- 自定义形象（仅桌宠2.0） ----------------

    private void pickSkin(String key) {
        pickingKey = key;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            startActivityForResult(Intent.createChooser(i,
                    "选图（最多 " + Skin.MAX_FRAMES + " 张，按顺序当动画帧）"), REQ_SKIN);
        } catch (Exception e) {
            pickingKey = null;
            Toast.makeText(this, "没有可用的相册/文件选择器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != REQ_SKIN) return;
        String key = pickingKey;
        pickingKey = null;
        if (res != RESULT_OK || data == null || key == null) return;
        java.util.List<android.net.Uri> uris = new java.util.ArrayList<android.net.Uri>();
        android.content.ClipData cd = data.getClipData();
        if (cd != null) {
            for (int i = 0; i < cd.getItemCount(); i++) uris.add(cd.getItemAt(i).getUri());
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        int n = Skin.save(this, key, uris);
        if (n == 0) {
            Toast.makeText(this, "图片读取失败，换张图再试试", Toast.LENGTH_SHORT).show();
            return;
        }
        if (PetService.instance != null) PetService.instance.reloadFrames();
        refreshSkinRows();
        Toast.makeText(this, "已应用 " + n + " 张图：" + labelOf(key), Toast.LENGTH_SHORT).show();
    }

    private String labelOf(String key) {
        for (int i = 0; i < Skin.KEYS.length; i++) {
            if (Skin.KEYS[i].equals(key)) return Skin.LABELS[i];
        }
        return key;
    }

    /** 每个动作显示「默认 / 已自定义 n 张」 */
    private void refreshSkinRows() {
        if (skinRows == null) return;
        for (int i = 0; i < skinRows.length && i < Skin.KEYS.length; i++) {
            int n = Skin.count(this, Skin.KEYS[i]);
            skinRows[i].setText(n > 0 ? "已自定义 " + n + " 张" : "默认");
            skinRows[i].setTextColor(Color.parseColor(n > 0 ? "#1B7F3B" : "#888888"));
        }
    }

    // ---------------- 自定义人设（仅桌宠2.0） ----------------

    /** 写宠物人设：多行输入 + 字数统计 + 一键填示例；留空 = 回到默认宠物口吻 */
    private void personaDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(8), dp(14), dp(4));

        TextView hint = small("#888888", Gravity.LEFT);
        hint.setTextSize(12);
        hint.setText("写清它是谁、什么性格、怎么说话。比如：\n"
                + "你叫小黑，一只高冷的黑猫，说话爱答不理，每句结尾加个喵。\n"
                + "最多 " + PetService.PERSONA_MAX + " 字；留空就回到默认的宠物口吻。");
        box.addView(hint);
        box.addView(gap(8));

        final EditText in = new EditText(this);
        in.setMinLines(5);
        in.setGravity(Gravity.TOP);
        in.setTextSize(13);
        in.setTextColor(Color.parseColor("#111111"));
        // 弹窗已是浅色底，输入框也用 App 内同款「白底 + 细黑边」，别用系统下划线样式
        in.setHintTextColor(Color.parseColor("#999999"));
        in.setPadding(dp(10), dp(8), dp(10), dp(8));
        in.setBackground(box());
        in.setHint("你叫……");
        in.setText(Memory.petPersona());
        box.addView(in);
        box.addView(gap(6));

        LinearLayout cRow = new LinearLayout(this);
        cRow.setOrientation(LinearLayout.HORIZONTAL);
        cRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView demo = button("填入示例");
        demo.setOnClickListener(v -> {
            in.setText(SAMPLE_PERSONA);
            in.setSelection(in.getText().length());
        });
        cRow.addView(demo);
        cRow.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        final TextView count = small("#999999", Gravity.END);
        cRow.addView(count);
        box.addView(cRow);

        final Runnable upd = () -> count.setText(in.getText().toString().trim().length()
                + " / " + PetService.PERSONA_MAX + " 字");
        upd.run();
        in.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) { upd.run(); }
        });

        new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle("宠物人设（会拼进 AI 提示词）")
                .setView(box)
                .setPositiveButton("保存", (d, w) -> {
                    String t = in.getText().toString().trim();
                    if (t.length() > PetService.PERSONA_MAX) t = t.substring(0, PetService.PERSONA_MAX);
                    Memory.setPetPersona(t);
                    refreshPersonaRow();
                    Toast.makeText(this, t.isEmpty() ? "已清空，回到默认口吻" : "已保存，下次对话就用它",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /** 人设卡片的状态行 */
    private void refreshPersonaRow() {
        if (personaRow == null) return;
        String p = Memory.petPersona();
        if (p.isEmpty()) {
            personaRow.setText("当前：默认宠物口吻（还没写人设）");
            personaRow.setTextColor(Color.parseColor("#888888"));
            return;
        }
        String one = p.replace('\n', ' ').replace('\r', ' ');
        if (one.length() > 16) one = one.substring(0, 16) + "…";
        personaRow.setText("当前：" + one + "（" + p.length() + " 字）");
        personaRow.setTextColor(Color.parseColor("#1B7F3B"));
    }

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
