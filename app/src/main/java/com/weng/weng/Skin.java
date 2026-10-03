package com.weng.weng;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * 宠物形象自定义。
 *
 * 用户从系统相册挑图后，图片被复制进应用私有目录 files/skin/&lt;动作&gt;_&lt;帧号&gt;.png；
 * 加载帧图时先看这里有没有自定义图，没有再回落到内置 drawable。
 * 一个动作可以一次选多张（最多 5 张），按顺序当动画帧；选 1 张就是静态图。
 */
public class Skin {

    public static final String CRUISE = "cruise";
    public static final String HAPPY = "happy";
    public static final String SAD = "sad";
    public static final String WORK = "work";
    public static final String JUMP = "jump";
    public static final String DEAD = "dead";

    /** 可自定义的动作（顺序与设置页展示一致） */
    public static final String[] KEYS = {CRUISE, HAPPY, SAD, WORK, JUMP, DEAD};
    public static final String[] LABELS = {
            "飞行姿态（平常的样子）", "开心", "难过 / 生气", "工作", "跳跃", "被拍扁"};

    /** 每个动作最多几帧（内置形象都是 5 帧） */
    public static final int MAX_FRAMES = 5;

    /** 图片最长边上限：宠物本体只有 96dp，太大会白白吃内存 */
    private static final int MAX_EDGE = 384;

    public static File dir(Context c) {
        File d = new File(c.getFilesDir(), "skin");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File file(Context c, String key, int idx) {
        return new File(dir(c), key + "_" + idx + ".png");
    }

    public static boolean has(Context c, String key, int idx) {
        return idx >= 0 && idx < MAX_FRAMES && file(c, key, idx).exists();
    }

    /** 该动作已自定义几帧（连续计数） */
    public static int count(Context c, String key) {
        int n = 0;
        while (n < MAX_FRAMES && has(c, key, n)) n++;
        return n;
    }

    /**
     * 解码结果缓存：key = 动作#帧号#文件大小#修改时间。
     *
     * 热重载（reloadFrames）会把 6 个动作最多 30 帧全部重新加载一遍，
     * 如果每帧都重新解码一次 PNG，设置页里改一张图要卡几百毫秒；
     * 有缓存后只有真正被换掉的那几帧需要重新解码。
     *
     * 存 Bitmap 而不是 Drawable：同一个 Bitmap 可以包成多个独立 Drawable 实例，
     * 免得主宠和捣蛋鬼共用同一个 Drawable 时滤镜（金光/暴击变色）互相串台。
     */
    private static final java.util.HashMap<String, Bitmap> CACHE = new java.util.HashMap<String, Bitmap>();

    /** 自定义目录里已经没有这张图了（换图/恢复默认），把缓存清掉 */
    public static void clearCache() {
        synchronized (CACHE) { CACHE.clear(); }
    }

    /** 取该帧：有自定义用自定义，否则用内置兜底（fallback 可能为 null） */
    public static Drawable load(Context c, String key, int idx, Drawable fallback) {
        if (idx < 0 || idx >= MAX_FRAMES) return fallback;
        File f = file(c, key, idx);
        if (!f.exists()) return fallback;
        try {
            String ck = key + "#" + idx + "#" + f.length() + "#" + f.lastModified();
            Bitmap bm;
            synchronized (CACHE) { bm = CACHE.get(ck); }
            if (bm == null || bm.isRecycled()) {
                bm = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bm == null) return fallback;
                synchronized (CACHE) { CACHE.put(ck, bm); }
            }
            return new BitmapDrawable(c.getResources(), bm);
        } catch (Exception e) {
            return fallback;
        }
    }

    /** 把用户选的一组图按顺序存成 0..n-1 帧，返回实际存下的帧数 */
    public static int save(Context c, String key, List<Uri> uris) {
        if (uris == null || uris.isEmpty()) return 0;
        clear(c, key);
        int saved = 0;
        for (int i = 0; i < uris.size() && i < MAX_FRAMES; i++) {
            Uri u = uris.get(i);
            InputStream is = null;
            FileOutputStream fos = null;
            Bitmap bm = null, out = null;
            try {
                // ① 先只读尺寸（inJustDecodeBounds 不分配像素内存），据此算采样率。
                //    以前是直接 decodeStream 全尺寸解码、再缩 —— 用户选一张 8000×6000 的
                //    相册原图会瞬间申请近 200MB，直接是 OutOfMemoryError 崩掉。
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                is = c.getContentResolver().openInputStream(u);
                if (is == null) continue;
                BitmapFactory.decodeStream(is, null, bounds);
                closeQuietly(is);
                is = null;
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) continue;

                int sample = 1;
                int longest = Math.max(bounds.outWidth, bounds.outHeight);
                while (longest / (sample * 2) >= MAX_EDGE) sample *= 2;   // 2 的幂，解码器原生支持

                // ② 按采样率解码，峰值内存降到约 1/4 以下
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = sample;
                is = c.getContentResolver().openInputStream(u);
                if (is == null) continue;
                bm = BitmapFactory.decodeStream(is, null, opt);
                if (bm == null) continue;

                out = shrink(bm);        // 再精确缩到 MAX_EDGE
                fos = new FileOutputStream(file(c, key, i));
                out.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                saved++;
            } catch (Throwable ignored) {
                // 这里必须是 Throwable：OutOfMemoryError 属于 Error，catch(Exception) 抓不住，
                // 以前一张超大图就能把整个 App 带走。
            } finally {
                closeQuietly(fos);
                closeQuietly(is);
                if (out != null && out != bm && !out.isRecycled()) out.recycle();
                if (bm != null && !bm.isRecycled()) bm.recycle();
            }
        }
        return saved;
    }

    private static void closeQuietly(java.io.Closeable x) {
        try { if (x != null) x.close(); } catch (Throwable ignored) {}
    }

    /** 缩到最长边 MAX_EDGE；回收交给调用方，避免同一张 Bitmap 被回收两次 */
    private static Bitmap shrink(Bitmap bm) {
        int max = Math.max(bm.getWidth(), bm.getHeight());
        if (max <= MAX_EDGE) return bm;
        float s = MAX_EDGE / (float) max;
        int w = Math.max(1, (int) (bm.getWidth() * s));
        int h = Math.max(1, (int) (bm.getHeight() * s));
        return Bitmap.createScaledBitmap(bm, w, h, true);
    }

    /** 恢复该动作的默认形象 */
    public static void clear(Context c, String key) {
        for (int i = 0; i < MAX_FRAMES; i++) {
            File f = file(c, key, i);
            if (f.exists()) f.delete();
        }
        clearCache();
    }

    public static void clearAll(Context c) {
        for (String k : KEYS) clear(c, k);
    }

    public static boolean anyCustom(Context c) {
        for (String k : KEYS) {
            if (count(c, k) > 0) return true;
        }
        return false;
    }
}
