package com.weng.weng;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 整蛊模式引擎：一批会繁殖/闪避的蚊子让你拍打。
 * 挂在 PetService 上（借它的 wm/handler/rnd），单只蚊=小 ImageView。
 */
public class PrankEngine {

    public interface Host {
        WindowManager wm();

        android.os.Handler handler();

        Random rnd();

        int screenW();

        int screenH();

        PetService pet();
    }

    private final Host h;
    private final List<Swarm> swarm = new ArrayList<Swarm>();
    private boolean running = false;
    private int kills = 0, wave = 1;
    private long startAt = 0;
    private int spawnBudget = 6;            // 初始数量（1-10，设置可调）
    private BossMiss boss = null;
    private boolean bossDefeated = false;
    private int bossHp = 35;
    private long lastPopAt = 0;
    private TextView banner = null;         // 波次红字
    private WindowManager.LayoutParams bannerLP;

    private static final int[] SPEEDS = {3, 4, 6, 8, 11};

    public PrankEngine(Host host) {
        this.h = host;
    }

    // ================= 生命周期 =================

    public boolean isRunning() { return running; }

    public int kills() { return kills; }

    public int wave() { return wave; }

    public void start(int count) {
        if (running) return;
        running = true;
        kills = 0;
        wave = 1;
        bossDefeated = false;
        spawnBudget = Math.max(1, Math.min(10, count));
        startAt = System.currentTimeMillis();
        lastPopAt = System.currentTimeMillis();
        for (int i = 0; i < spawnBudget; i++) spawn(false);
        h.handler().post(tick);
    }

    public void stop() {
        running = false;
        h.handler().removeCallbacks(tick);
        for (Swarm s : new ArrayList<Swarm>(swarm)) removeSwarm(s);
        swarm.clear();
        if (boss != null) {
            try { h.wm().removeView(boss.view); } catch (Exception ignored) {}
            boss = null;
        }
        if (banner != null) {
            try { h.wm().removeView(banner); } catch (Exception ignored) {}
            banner = null;
        }
        saveScore();
    }

    private void saveScore() {
        JSONObject o = DataStore.obj("prankScore");
        int best = o.optInt("bestWave", 0);
        int total = o.optInt("totalKills", 0);
        try {
            o.put("bestWave", Math.max(best, wave))
                    .put("totalKills", total + kills)
                    .put("lastKills", kills)
                    .put("lastAt", new SimpleDateFormat("MM-dd HH:mm", Locale.US).format(new Date()));
        } catch (Exception ignored) {
        }
        DataStore.saveObj("prankScore", o);
    }

    // ================= 蚊群 =================

    private static class Swarm {
        View view;
        WindowManager.LayoutParams lp;
        float x, y, vx, vy;
        int size;
        boolean dead;
        int speedIdx;
    }

    private void spawn(boolean fromWave) {
        if (swarm.size() >= 25) return;   // 场上上限 25
        final Swarm s = new Swarm();
        s.size = 48 + h.rnd().nextInt(24);
        s.speedIdx = Math.min(SPEEDS.length - 1, (wave - 1) / 1);
        float sp = SPEEDS[s.speedIdx];
        double a = h.rnd().nextDouble() * Math.PI * 2;
        s.vx = (float) Math.cos(a) * sp;
        s.vy = (float) Math.sin(a) * sp;
        ImageView iv = new ImageView(h.pet().getApplicationContext());
        // 借用主宠的帧图（第一帧）；波次高时换红/紫/黑 tint
        int res = h.pet().mosquitoRes();
        iv.setImageResource(res);
        if (wave >= 2) iv.setColorFilter(redTint(wave));
        s.view = iv;
        s.lp = new WindowManager.LayoutParams(s.size, s.size,
                android.os.Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        s.lp.gravity = Gravity.TOP | Gravity.START;
        s.x = h.rnd().nextInt(Math.max(1, h.screenW() - s.size));
        s.y = 60 + h.rnd().nextInt(Math.max(1, h.screenH() / 2));
        s.lp.x = (int) s.x;
        s.lp.y = (int) s.y;
        try { h.wm().addView(iv, s.lp); } catch (Exception e) { return; }
        iv.setOnTouchListener((v, ev) -> {
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                kill(s);
                return true;
            }
            return false;
        });
        swarm.add(s);
    }

    private android.graphics.PorterDuffColorFilter redTint(int w) {
        int c = w >= 4 ? Color.BLACK : (w >= 3 ? Color.rgb(120, 0, 160) : Color.RED);
        return new android.graphics.PorterDuffColorFilter(c, android.graphics.PorterDuff.Mode.SRC_ATOP);
    }

    private void kill(Swarm s) {
        if (s.dead) return;
        s.dead = true;
        kills++;
        removeSwarm(s);
        // 难度成长：前 15 杀每 5 杀 +1 蚊；15-30 杀每 10 杀提速加量；30 杀后每 5 杀提速加量
        if (kills <= 15 && kills % 5 == 0) spawn(true);
        else if (kills > 15 && kills <= 30 && kills % 10 == 0) { wave++; onWave(); }
        else if (kills > 30 && kills % 5 == 0) { wave++; onWave(); }
        // BOSS 解锁：杀满 40 或撑 10 分钟
        if (!bossDefeated && boss == null && (kills >= 40 || System.currentTimeMillis() - startAt > 600000L)) {
            if (kills >= 38 && kills < 40) {
                h.pet().showBubble("⚠️ BOSS 即将出现！做好准备！", 3000);
            }
            spawnBoss();
        }
        // 杀满 15 只后小概率弹伪造报错
        if (kills > 15 && h.rnd().nextInt(100) < 5) popFakeError();
        // 躲猫猫：击杀数到 5/20 全体消失 5 秒
        if (kills == 5 || kills == 20) hideAndSeek();
    }

    private void removeSwarm(Swarm s) {
        swarm.remove(s);
        try { h.wm().removeView(s.view); } catch (Exception ignored) {}
    }

    // ================= 波次 / 躲猫猫 =================

    private void onWave() {
        // 每波 +3 蚊（速度由 speedIdx 随 wave 提升）
        for (int i = 0; i < 3; i++) spawn(true);
        showBanner("🌊 第 " + wave + " 波感染！速度+1 数量+3");
    }

    private void hideAndSeek() {
        for (Swarm s : new ArrayList<Swarm>(swarm)) removeSwarm(s);
        h.pet().showBubble("嗡～躲猫猫开始！", 2500);
        h.handler().postDelayed(() -> {
            if (!running) return;
            for (int i = 0; i < Math.min(spawnBudget + wave * 2, 10); i++) spawn(true);
        }, 5000);
    }

    private void showBanner(String text) {
        if (banner == null) {
            banner = new TextView(h.pet().getApplicationContext());
            banner.setTextSize(20);
            banner.setTextColor(Color.RED);
            banner.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            banner.setGravity(Gravity.CENTER);
            bannerLP = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    android.os.Build.VERSION.SDK_INT >= 26
                            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                            : WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
            bannerLP.gravity = Gravity.CENTER;
            bannerLP.y = 120;
            try { h.wm().addView(banner, bannerLP); } catch (Exception ignored) { return; }
        }
        banner.setText(Ico.s(h.pet(), text));
        banner.setVisibility(View.VISIBLE);
        h.handler().postDelayed(() -> { if (banner != null) banner.setVisibility(View.GONE); }, 2500);
    }

    // ================= 伪造报错弹窗 =================

    

    private void popFakeError() {
        List<String> es = Quotes.get("fake_error");
        if (es.isEmpty()) return;
        String e = es.get(h.rnd().nextInt(es.size()));
        String[] parts = e.split("\\|");
        // 借主宠的气泡+增援一只
        h.pet().showBubble("⚠ " + parts[0] + "\n" + (parts.length > 1 ? parts[1] : ""), 4000);
        spawn(true);
    }
    
    /** 周期弹报错（约 100 秒一个）+增援 */
    private void fakeErrorTick() {
        if (!running) return;
        if (System.currentTimeMillis() - lastPopAt > 100000L) {
            lastPopAt = System.currentTimeMillis();
            popFakeError();
        }
    }

    // ================= BOSS =================

    private static class BossMiss {
        ImageView view;
        WindowManager.LayoutParams lp;
        float x, y;
        int hp = 35;
        long lastTele = 0;
    }

    private void spawnBoss() {
        final BossMiss b = new BossMiss();
        bossHp = 35;
        b.hp = bossHp;
        ImageView iv = new ImageView(h.pet().getApplicationContext());
        iv.setImageResource(h.pet().mosquitoRes());
        iv.setColorFilter(new android.graphics.PorterDuffColorFilter(Color.rgb(160, 0, 0), android.graphics.PorterDuff.Mode.SRC_ATOP));
        iv.setScaleX(2f);
        iv.setScaleY(2f);
        b.view = iv;
        b.lp = new WindowManager.LayoutParams(140, 140,
                android.os.Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        b.lp.gravity = Gravity.TOP | Gravity.START;
        b.x = h.screenW() / 2f - 70;
        b.y = h.screenH() / 3f;
        b.lp.x = (int) b.x;
        b.lp.y = (int) b.y;
        try { h.wm().addView(iv, b.lp); } catch (Exception e) { return; }
        iv.setOnTouchListener((v, ev) -> {
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                hitBoss();
                return true;
            }
            return false;
        });
        boss = b;
        showBanner("⚠️ BOSS 出现！35 血！点它！");
    }

    private void hitBoss() {
        if (boss == null) return;
        boss.hp--;
        // 每次被击中瞬移 + 可能召唤 1-3 只小蚊
        if (h.rnd().nextInt(100) < 40) {
            int n = 1 + h.rnd().nextInt(3);
            for (int i = 0; i < n; i++) spawn(true);
            h.pet().showBubble("BOSS 召唤了 " + n + " 只小蚊！", 2000);
        }
        // 血量 <15 泛红暴走
        if (boss.hp < 15) {
            boss.view.setColorFilter(new android.graphics.PorterDuffColorFilter(
                    Color.rgb(255, 40, 40), android.graphics.PorterDuff.Mode.SRC_ATOP));
        }
        if (boss.hp <= 0) {
            defeatBoss();
            return;
        }
        // 瞬移
        boss.x = h.rnd().nextInt(Math.max(1, h.screenW() - 140));
        boss.y = 60 + h.rnd().nextInt(Math.max(1, h.screenH() / 2));
        boss.lp.x = (int) boss.x;
        boss.lp.y = (int) boss.y;
        try { h.wm().updateViewLayout(boss.view, boss.lp); } catch (Exception ignored) {}
        showBanner("BOSS 还剩 " + boss.hp + " 血！");
    }

    private void defeatBoss() {
        bossDefeated = true;
        if (boss != null) {
            try { h.wm().removeView(boss.view); } catch (Exception ignored) {}
            boss = null;
        }
        // 分身术彩蛋 5-15%
        if (h.rnd().nextInt(100) < 15) {
            h.pet().showBubble("分身术！", 1500);
            for (int i = 0; i < 2; i++) spawn(true);
            h.handler().postDelayed(() -> h.pet().showBubble("查克拉耗尽…", 1500), 8000);
        }
        // 终局演出：报错→BSOD→救赎终蚊
        endGame();
    }

    // ================= 终局 =================

    private void endGame() {
        h.pet().showBubble("⚠ CRITICAL_SYSTEM_FAILURE\n蚊子系统崩溃中…", 4000);
        h.handler().postDelayed(this::showBsod, 4000);
    }

    /** 全屏假蓝屏 + 救赎终蚊 */
    private void showBsod() {
        TextView bsod = new TextView(h.pet().getApplicationContext());
        bsod.setBackgroundColor(Color.rgb(20, 60, 170));
        bsod.setTextColor(Color.WHITE);
        bsod.setTextSize(16);
        bsod.setPadding(60, 200, 60, 0);
        String txt = ":(\n\n你的电脑遇到问题，需要重新拍蚊。\n\n我们只收集部分蚊子尸体信息，然后为你重启。\n\n终止代码：MOSQUITO_EXCEPTION_NOT_HANDLED\n\n完成进度：0%";
        bsod.setText(txt);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                android.os.Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                0, PixelFormat.TRANSLUCENT);
        try { h.wm().addView(bsod, lp); } catch (Exception e) { return; }
        // 假进度条
        final int[] pct = {0};
        Runnable prog = new Runnable() {
            @Override
            public void run() {
                pct[0] = Math.min(100, pct[0] + h.rnd().nextInt(9));
                bsod.setText(":(\n\n你的电脑遇到问题，需要重新拍蚊。\n\n我们只收集部分蚊子尸体信息，然后为你重启。\n\n终止代码：MOSQUITO_EXCEPTION_NOT_HANDLED\n\n完成进度：" + pct[0] + "%");
                if (pct[0] < 100) h.handler().postDelayed(this, 400);
            }
        };
        h.handler().post(prog);
        // 2 秒后放救赎终蚊
        h.handler().postDelayed(() -> finalMosquito(bsod), 2500);
    }

    private void finalMosquito(final TextView bsod) {
        final ImageView fin = new ImageView(h.pet().getApplicationContext());
        fin.setImageResource(h.pet().mosquitoRes());
        fin.setScaleX(1.5f);
        fin.setScaleY(1.5f);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(140, 140,
                android.os.Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.CENTER;
        try { h.wm().addView(fin, lp); } catch (Exception e) { return; }
        TextView tip = new TextView(h.pet().getApplicationContext());
        tip.setText("是时候结束这一切了，点我吧");
        tip.setTextColor(Color.WHITE);
        tip.setTextSize(14);
        WindowManager.LayoutParams tlp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                android.os.Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        tlp.gravity = Gravity.CENTER;
        tlp.y = 120;
        try { h.wm().addView(tip, tlp); } catch (Exception ignored) {}
        fin.setOnTouchListener((v, ev) -> {
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                // 收场：移除所有视图、存战绩、成绩单气泡
                try { h.wm().removeView(fin); } catch (Exception ignored) {}
                try { h.wm().removeView(tip); } catch (Exception ignored) {}
                try { h.wm().removeView(bsod); } catch (Exception ignored) {}
                stop();
                String report = "🪧 整蛊战报\n击杀 " + kills + " 只 · 存活 " +
                        (System.currentTimeMillis() - startAt) / 1000 + " 秒 · 最高第 " + wave + " 波\n蚊子大军已被你终结！";
                h.pet().showBubble(report, 9000);
                return true;
            }
            return false;
        });
        // 终蚊慢速漂
        Runnable drift = new Runnable() {
            @Override
            public void run() {
                lp.x = (int) (Math.sin(System.currentTimeMillis() / 2000.0) * 40);
                lp.y = (int) (Math.cos(System.currentTimeMillis() / 2500.0) * 25);
                try { h.wm().updateViewLayout(fin, lp); } catch (Exception ignored) {}
                h.handler().postDelayed(this, 50);
            }
        };
        h.handler().post(drift);
    }

    // ================= 主循环 =================

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            h.handler().postDelayed(this, 30);
            fakeErrorTick();
            // 蚊群移动 + 繁殖（场上<50% 时 1% 概率繁殖）+ 闪避（靠近屏幕点击点算不了，改成随机急转）
            for (int i = swarm.size() - 1; i >= 0; i--) {
                Swarm s = swarm.get(i);
                s.x += s.vx;
                s.y += s.vy;
                boolean bounce = false;
                if (s.x < 0) { s.x = 0; s.vx = -s.vx; bounce = true; }
                if (s.x > h.screenW() - s.size) { s.x = h.screenW() - s.size; s.vx = -s.vx; bounce = true; }
                if (s.y < 40) { s.y = 40; s.vy = -s.vy; bounce = true; }
                if (s.y > h.screenH() - s.size - 60) { s.y = h.screenH() - s.size - 60; s.vy = -s.vy; bounce = true; }
                if (bounce && h.rnd().nextInt(100) < 30) {
                    double a = h.rnd().nextDouble() * Math.PI * 2;
                    s.vx = (float) Math.cos(a) * SPEEDS[s.speedIdx];
                    s.vy = (float) Math.sin(a) * SPEEDS[s.speedIdx];
                }
                // 随机闪避急转
                if (h.rnd().nextInt(500) == 0) {
                    s.vx = -s.vx;
                    s.vy = -s.vy;
                }
                s.lp.x = (int) s.x;
                s.lp.y = (int) s.y;
                try { h.wm().updateViewLayout(s.view, s.lp); } catch (Exception ignored) {}
            }
            // 繁殖
            if (swarm.size() < spawnBudget && h.rnd().nextInt(100) < 2) spawn(true);
        }
    };
}
