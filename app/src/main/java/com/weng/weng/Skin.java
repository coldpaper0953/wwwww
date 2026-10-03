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

    /** 取该帧：有自定义用自定义，否则用内置兜底（fallback 可能为 null） */
    public static Drawable load(Context c, String key, int idx, Drawable fallback) {
        if (!has(c, key, idx)) return fallback;
        try {
            Bitmap bm = BitmapFactory.decodeFile(file(c, key, idx).getAbsolutePath());
            if (bm == null) return fallback;
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
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = c.getContentResolver().openInputStream(uris.get(i));
                if (is == null) continue;
                Bitmap bm = BitmapFactory.decodeStream(is);
                if (bm == null) continue;
                bm = shrink(bm);
                fos = new FileOutputStream(file(c, key, i));
                bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                saved++;
            } catch (Exception ignored) {
            } finally {
                try { if (is != null) is.close(); } catch (Exception ignored) {}
                try { if (fos != null) fos.close(); } catch (Exception ignored) {}
            }
        }
        return saved;
    }

    private static Bitmap shrink(Bitmap bm) {
        int max = Math.max(bm.getWidth(), bm.getHeight());
        if (max <= MAX_EDGE) return bm;
        float s = MAX_EDGE / (float) max;
        int w = Math.max(1, (int) (bm.getWidth() * s));
        int h = Math.max(1, (int) (bm.getHeight() * s));
        Bitmap out = Bitmap.createScaledBitmap(bm, w, h, true);
        if (out != bm) bm.recycle();
        return out;
    }

    /** 恢复该动作的默认形象 */
    public static void clear(Context c, String key) {
        for (int i = 0; i < MAX_FRAMES; i++) {
            File f = file(c, key, i);
            if (f.exists()) f.delete();
        }
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
