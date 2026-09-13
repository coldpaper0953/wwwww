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

/** App 内设置窗口：除人设（内置加密锁定）外全部可手动编辑 */
public class SettingsActivity extends Activity {

    private TextView affLabel, verLabel, chatLog, speedLabel, dndBtn, affVal, workBtn;
    private EditText input;
    private ScrollView scroller;
    private android.app.AlertDialog pickDlg;
    private EditText apiBaseField, apiKeyField, apiModelField;
    private TextView emoLine, feedLine;
    private TextView prankScore, prankBtn;
    private EditText prankCount;
    private TextView[] glideBtns;
    private TextView tabBtn, senseBtn;
    private android.widget.SeekBar sizeSlider;
    private TextView sizeVal;

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
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        sizeRow.addView(sizeSlider, slp);
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

        // 状态卡：称号/情绪/饲养
        TextView statusLine = small("#555555", Gravity.LEFT);
        statusLine.setText("❤️ " + com.weng.weng.DataStore.titleFor(com.weng.weng.DataStore.getAff())
                + " · 今日好感 " + com.weng.weng.DataStore.sp().getInt("dailyAff", 0) + "/50");
        statusLine.setPadding(dp(4), 0, 0, dp(4));
        petCard.addView(statusLine);
        TextView emoLine = small("#777777", Gravity.LEFT);
        emoLine.setPadding(dp(4), dp(2), 0, dp(4));
        petCard.addView(emoLine);
        TextView feedLine = small("#777777", Gravity.LEFT);
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
        TextView persona = button("🔒 人设已锁定");
        persona.setOnClickListener(v ->
                Toast.makeText(this, "核心人设已内置加密保护，无法查看或修改", Toast.LENGTH_LONG).show());
        modeRow.addView(persona);
        petCard.addView(modeRow);
        root.addView(petCard);
        root.addView(gap(10));

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
        apiBaseField = apiInput("接口地址：填 https://api.xxx.com/v1 即可（v2/v3/本地http端口均可，自动补全）",
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
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        modelRow.addView(apiModelField, mlp);
        modelRow.addView(gapW(8));
        TextView fetchBtn = button("📡 拉取");
        fetchBtn.setOnClickListener(v -> fetchModelsDialog());
        modelRow.addView(fetchBtn);
        apiCard.addView(modelRow);
        apiCard.addView(gap(8));
        TextView saveApiBtn = button("💾 保存接口设置");
        saveApiBtn.setOnClickListener(v -> saveApi());
        apiCard.addView(saveApiBtn);
        root.addView(apiCard);
        root.addView(gap(10));

        // ============ 整蛊模式卡片 ============
        root.addView(cardLabel("🦟 整蛊模式（和电脑版一样的蚊群拍打游戏）"));
        LinearLayout prankCard = card();
        LinearLayout pRow = new LinearLayout(this);
        pRow.setOrientation(LinearLayout.HORIZONTAL);
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
        TextView fortune = button("🔮 今日运势");
        fortune.setOnClickListener(v ->
                Toast.makeText(this, com.weng.weng.Extras.fortune(), Toast.LENGTH_LONG).show());
        exRow.addView(fortune);
        exRow.addView(gapW(8));
        TextView guide = button("📖 饲养指南");
        guide.setOnClickListener(v ->
                new AlertDialog.Builder(this).setTitle("📖 饲养指南")
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
                new AlertDialog.Builder(this)
                        .setTitle("App 感知说明")
                        .setMessage("蚊子只读取「当前打开的应用名字」（不读内容）。\n\n要让它生效，请在接下来的系统页面里找到 嗡嗡嗡，允许「使用情况访问权限」。")
                        .setPositiveButton("去授权", (d, w) -> startActivity(new Intent(
                                android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)))
                        .setNegativeButton("取消", null)
                        .show();
            }
            refresh();
        });
        tabRow.addView(senseBtn);
        ixCard.addView(tabRow);
        root.addView(ixCard);
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
        TextView bootBtn = button("📲 开机自启：关");
        bootBtn.setOnClickListener(v -> {
            boolean on = !com.weng.weng.DataStore.getBool("bootAuto", false);
            com.weng.weng.DataStore.putBool("bootAuto", on);
            bootBtn.setText(on ? "📲 开机自启：开" : "📲 开机自启：关");
            Toast.makeText(this, on ? "已开启（重启后生效）" : "已关闭", Toast.LENGTH_SHORT).show();
        });
        if (com.weng.weng.DataStore.getBool("bootAuto", false)) bootBtn.setText("📲 开机自启：开");
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
        TextView about = small("#AAAAAA", Gravity.CENTER);
        about.setText("嗡嗡嗡手机版 · MADE by芬芳小鼠 · 还原版");
        aboutCard.addView(about);
        root.addView(aboutCard);

        ScrollView page = new ScrollView(this);
        page.addView(root);
        setContentView(page);
    }

    /** 保存接口设置：Key 留空＝沿用已保存的（避免把空串/打码串写进存档） */
    private void saveApi() {
        if (PetService.instance == null) return;
        String key = apiKeyField.getText().toString().trim();
        PetService.instance.setApi(
                apiBaseField.getText().toString().trim(),
                key.isEmpty() ? PetService.instance.apiKey : key,
                apiModelField.getText().toString().trim());
        Toast.makeText(this, "已保存，下一条消息生效", Toast.LENGTH_SHORT).show();
    }

    /** 拉取模型列表：先把输入框里的地址/Key/模型落盘，再 GET /models，弹窗筛选选择 */
    private void fetchModelsDialog() {
        if (PetService.instance == null) return;
        saveApi();
        final android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("正在拉取模型列表…");
        pd.setCancelable(false);
        pd.show();
        PetService.instance.fetchModels((models, error) -> {
            pd.dismiss();
            if (error != null || models == null || models.isEmpty()) {
                Toast.makeText(this, "拉取失败：" + (error == null ? "列表为空" : error), Toast.LENGTH_LONG).show();
                return;
            }
            pickModelDialog(models);
        });
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
                    if (pickDlg != null) pickDlg.dismiss();
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
        pickDlg = new AlertDialog.Builder(this)
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
        new AlertDialog.Builder(this).setTitle("🏆 成就与回忆").setMessage(sb.toString())
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
        e.setBackground(box());
        return e;
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
        if (emoLine != null) emoLine.setText("🧠 " + PetService.instance.emo.describe());
        if (feedLine != null) feedLine.setText("🩸 " + PetService.instance.feedStatusText());
        if (prankScore != null) prankScore.setText("📊 " + PetService.instance.prankScoreText());
        if (prankBtn != null) prankBtn.setText(PetService.instance.prankMode ? "🕊 结束整蛊" : "🦟 注入并隐藏");
        // 惯性档位高亮：选中档加粗+标 ●
        int lv = PetService.instance.glideLevel();
        String[] lvNames = {"关", "轻", "中", "强"};
        for (int i = 0; i < glideBtns.length; i++) {
            if (glideBtns[i] != null)
                glideBtns[i].setText((i == lv ? "● " : "") + lvNames[i]);
        }
        if (tabBtn != null) tabBtn.setText(com.weng.weng.DataStore.getBool("tabHandle", true) ? "💬 屏幕拉手：开" : "💬 屏幕拉手：关");
        if (senseBtn != null) {
            boolean on = com.weng.weng.DataStore.getBool("appSense", true);
            String cur = PetService.instance.currentAppLabelPublic();
            senseBtn.setText(on ? ("👁 App感知：开" + (cur == null ? "（未授权）" : "（当前:" + cur + "）")) : "👁 App感知：关");
        }
        verLabel.setText("v" + PetService.instance.curVersion() + (PetService.instance.dnd ? "（勿扰中）" : "") + (PetService.instance.workMode ? "（工作中）" : ""));
        dndBtn.setText(PetService.instance.dnd ? "🌙 勿扰中" : "🌙 勿扰");
        workBtn.setText(PetService.instance.workMode ? "📚 工作中" : "📚 工作");
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
