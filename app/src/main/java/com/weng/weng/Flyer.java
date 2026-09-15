package com.weng.weng;

import android.os.Handler;
import android.view.WindowManager;

import java.util.Random;

/**
 * 蚊子运动控制器：原版 20+ 种飞行状态移植。
 * PetService 持 px/py/speed，每 tick 调 step() 更新位置，写回 WindowManager。
 */
public class Flyer {

    public interface Host {
        float px();

        float py();

        void setPos(float x, float y);

        int screenW();

        int screenH();

        int size();

        float speedMul();

        float glideFriction();

        void onBounce();

        Random rnd();

        Handler handler();
    }

    private final Host h;
    public String state = "cruise";
    public int stateTimer = 0;          // 剧情状态倒计时（帧）
    public float vx = 2f, vy = 1.5f;    // cruise 用
    public float ax = 0, ay = 0;        // 锚点（各状态基准）
    public float baseY = 0;             // 条带状态基准高度
    public int dir = 1;                 // 水平方向
    public float ang = 0;               // 相位
    public boolean perched = false;
    public int perchX = -1, perchY = -1; // window_perch/text_crawl 落点（-1 待初始化）
    public long lastSwitchAt = 0;

    public Flyer(Host host) {
        this.h = host;
        randomizeVelocity();
    }

    public void randomizeVelocity() {
        float sp = 1.5f + h.rnd().nextFloat() * 2.5f;
        double a = h.rnd().nextDouble() * Math.PI * 2;
        vx = (float) Math.cos(a) * sp;
        vy = (float) Math.sin(a) * sp;
    }

    public void switchTo(String s, int frames) {
        state = s;
        stateTimer = frames;
        lastSwitchAt = System.currentTimeMillis();
        perched = false;
        ang = 0;
        ax = h.px();
        ay = h.py();
        baseY = h.py();
        dir = h.rnd().nextBoolean() ? 1 : -1;
    }

    /** 甩动惯性：以松手速度继续滑行，按 Host.glideFriction() 衰减 */
    public void glide(float vx0, float vy0) {
        state = "glide";
        vx = vx0;
        vy = vy0;
        stateTimer = 999999;
    }

    private void clampPos() {
        float w = h.screenW() - h.size();
        float hh = h.screenH() - h.size() - 60;
        float x = h.px(), y = h.py();
        boolean bounced = false;
        if (x < 0) { x = 0; vx = Math.abs(vx); bounced = true; }
        if (x > w) { x = w; vx = -Math.abs(vx); bounced = true; }
        if (y < 40) { y = 40; vy = Math.abs(vy); bounced = true; }
        if (y > hh) { y = hh; vy = -Math.abs(vy); bounced = true; }
        h.setPos(x, y);
        if (bounced) h.onBounce();
    }

    /** 每 tick 调用（约 30ms 一次），返回是否应重绘 */
    public boolean step() {
        float x = h.px(), y = h.py();
        int W = h.screenW(), H = h.screenH() - 60 - h.size();
        Random r = h.rnd();
        float m = h.speedMul();
        switch (state) {
            case "glide": {
                // 惯性滑行：速度按摩擦系数衰减，撞边反弹打折
                float f = h.glideFriction();
                x += vx;
                y += vy;
                vx *= f;
                vy *= f;
                h.setPos(x, y);
                if (x < 0) { h.setPos(0, y); vx = -vx * 0.5f; h.onBounce(); }
                if (x > W - h.size()) { h.setPos(W - h.size(), y); vx = -vx * 0.5f; h.onBounce(); }
                if (y < 40) { h.setPos(x, 40); vy = -vy * 0.5f; h.onBounce(); }
                if (y > H) { h.setPos(x, H); vy = -vy * 0.5f; h.onBounce(); }
                if (Math.abs(vx) < 0.15f && Math.abs(vy) < 0.15f) {
                    switchTo("cruise", 0);
                    randomizeVelocity();
                }
                return true;
            }
            case "cruise":
                x += vx * m;
                y += vy * m;
                h.setPos(x, y);
                clampPos();
                if (r.nextInt(100) < 4) randomizeVelocity();
                return true;
            case "dash":
                x += vx * 3 * m;
                y += vy * 3 * m;
                h.setPos(x, y);
                clampPos();
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            case "zigzag":
                x += vx * m;
                y += vy * m;
                if (r.nextInt(100) < 5) vx = -vx;
                if (r.nextInt(100) < 5) vy = -vy;
                h.setPos(x, y);
                clampPos();
                return true;
            case "hover_jitter":
                // 悬停微颤：几个不同周期的正弦叠加成平滑轨迹。
                // 注意别写成"每 tick 重新随机一个偏移"——tick 是 30ms，那就是 33Hz 的随机跳变，看起来是在抽搐。
                if (!perched) { ax = x; ay = y; perched = true; ang = 0; }
                ang += 0.22f;
                h.setPos(ax + (float) (Math.sin(ang) * 5 + Math.sin(ang * 2.7) * 2.5),
                        ay + (float) (Math.cos(ang * 1.4) * 4 + Math.sin(ang * 3.1) * 2));
                if (System.currentTimeMillis() - lastSwitchAt > 2200) switchTo("cruise", 0);
                return true;
            case "edge_walk": {
                // 沿四边滑行：dir 表示当前边 0上1右2下3左，由 ax 记录
                int edge = (int) ax;
                float spd = 3f;
                if (edge == 0) { x += spd * dir; y = 40; }
                else if (edge == 1) { x = W - h.size() / 2f; y += spd * dir; }
                else if (edge == 2) { x += spd * dir; y = H; }
                else { x = h.size() / 2f; y += spd * dir; }
                if (edge % 2 == 0 && (x < 10 || x > W - 10)) dir = -dir;
                if (edge % 2 == 1 && (y < 50 || y > H - 10)) dir = -dir;
                h.setPos(x, y);
                if (r.nextInt(600) == 0) switchTo("cruise", 0);
                return true;
            }
            case "corner_rest": {
                if (!perched) {
                    int c = r.nextInt(4);
                    ax = (c % 2 == 0) ? 40 + r.nextFloat() * 20 : W - 60 + r.nextFloat() * 20;
                    ay = (c < 2) ? 60 + r.nextFloat() * 20 : H - 60 + r.nextFloat() * 40;
                    perched = true;
                }
                h.setPos(ax, ay);
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return stateTimer % 3 == 0;
            }
            case "drift":
                x += vx * 0.4f;
                y += vy * 0.4f;
                if (r.nextInt(60) == 0) vy = -vy;
                h.setPos(x, y);
                clampPos();
                return true;
            case "peek": {
                // 从最近边缘探出（最深 120px）
                if (!perched) { ax = nearestEdge(); perched = true; }
                float depth = (float) (120 * Math.abs(Math.sin(ang)));
                ang += 0.05f;
                if (ax == 0) h.setPos(depth, y);
                else if (ax == 1) h.setPos(W - depth, y);
                else if (ax == 2) h.setPos(x, 40 + depth);
                else h.setPos(x, H - depth);
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            }
            case "spiral": {
                if (!perched) { ax = x; ay = y; perched = true; }
                ang += 0.12f;
                h.setPos(ax + 28 * (float) Math.cos(ang), ay + 28 * (float) Math.sin(ang));
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            }
            case "window_perch": {
                // 趴到"窗口框"：屏幕上半中部一个虚拟标题栏位置站岗（手机没有真窗口，取主屏中上）
                if (!perched) {
                    perchX = W / 2 - h.size() / 2;
                    perchY = 90;
                    perched = true;
                    ang = 0;
                }
                ang += 0.18f;
                h.setPos(perchX + (float) Math.sin(ang) * 2f,
                        perchY + (float) Math.sin(ang * 1.6f) * 1.5f);
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return stateTimer % 2 == 0;
            }
            case "text_crawl": {
                // 文字爬行：某条带高度左右横移
                if (!perched) {
                    baseY = 120 + r.nextInt(Math.max(1, H / 2 - 120));
                    dir = r.nextBoolean() ? 1 : -1;
                    perched = true;
                }
                x += 1.5f * dir;
                if (x < 10 || x > W - h.size() - 10) dir = -dir;
                h.setPos(x, baseY);
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            }
            case "idle_fidget": {
                // 小动作：平滑地晃一晃（同样是正弦，不要每 tick 随机）
                if (!perched) { ax = x; ay = y; perched = true; ang = 0; }
                ang += 0.12f;
                h.setPos(ax + (float) Math.sin(ang) * 9,
                        ay + (float) Math.sin(ang * 2f) * 4);
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            }
            case "loop": {
                // 绕 50x30 椭圆一整圈
                ang += 0.09f;
                h.setPos(ax + 50 * (float) Math.cos(ang), ay + 30 * (float) Math.sin(ang));
                if (ang >= Math.PI * 2) switchTo("cruise", 0);
                return true;
            }
            case "dive_climb": {
                // 2 倍速下冲到底再拉回原高度
                if (ang == 0) { baseY = y; }
                y += 8f;
                x += vx * 2;
                if (y >= H) {
                    y = H;
                    ang = 1;
                }
                if (ang == 1) {
                    y -= 6f;
                    if (y <= baseY) switchTo("cruise", 0);
                }
                h.setPos(x, y);
                clampPos();
                return true;
            }
            case "figure_eight": {
                ang += 0.07f;
                h.setPos(ax + 60 * (float) Math.sin(ang), ay + 30 * (float) Math.sin(2 * ang));
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            }
            case "butterfly": {
                ang += 0.05f;
                x += 2f * dir;
                if (x < 10 || x > W - h.size() - 10) dir = -dir;
                h.setPos(x, ay + 20 * (float) Math.sin(ang));
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            }
            case "sleepy": {
                // 缓慢下沉 + 左右微摆
                y += 0.8f;
                x += 0.5f * (float) Math.sin(ang);
                ang += 0.03f;
                if (y > H) y = H;
                h.setPos(x, y);
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            }
            case "dizzy": {
                if (!perched) { ax = x; ay = y; perched = true; }
                ang += 0.3f;
                h.setPos(ax + 12 * (float) Math.cos(ang), ay + 12 * (float) Math.sin(ang));
                stateTimer--;
                if (stateTimer <= 0) switchTo("cruise", 0);
                return true;
            }
            default:
                switchTo("cruise", 0);
                return true;
        }
    }

    private int nearestEdge() {
        float x = h.px(), y = h.py();
        int W = h.screenW(), H = h.screenH();
        float d0 = y, d1 = W - x, d2 = H - y, d3 = x;
        if (d0 <= d1 && d0 <= d2 && d0 <= d3) return 0;
        if (d1 <= d2 && d1 <= d3) return 1;
        if (d2 <= d3) return 2;
        return 3;
    }
}
