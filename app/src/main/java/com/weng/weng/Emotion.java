package com.weng.weng;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** 情绪引擎：开心/生气/孤独/兴奋四维 0-100，按距上次互动的时长漂移；情绪主导切换时驱动表情动画 */
public class Emotion {

    public float happy = 50, angry = 10, lonely = 20, excited = 40;

    private static float clamp(float v) { return Math.max(0f, Math.min(100f, v)); }

    public void add(String mood, float n) {
        if (mood == null) return;
        if (mood.equals("开心")) happy = clamp(happy + n);
        else if (mood.equals("生气")) angry = clamp(angry + n);
        else if (mood.equals("孤独")) lonely = clamp(lonely + n);
        else if (mood.equals("兴奋")) excited = clamp(excited + n);
    }

    /** 距上次互动的分钟数 → 漂移：10 分钟内保持正向、10-30 渐平静、30-60 孤独积累、>60 开心跌谷底 */
    public void drift(long minutesSinceLast) {
        if (minutesSinceLast <= 10) {
            happy = clamp(happy + 2);
            lonely = clamp(lonely - 2);
        } else if (minutesSinceLast <= 30) {
            happy = clamp(happy - 0.3f);
            lonely = clamp(lonely + 0.2f);
        } else if (minutesSinceLast <= 60) {
            happy = clamp(happy - 0.8f);
            lonely = clamp(lonely + 0.6f);
            angry = clamp(angry - 0.2f);
        } else {
            happy = clamp(happy - 1.5f);
            lonely = clamp(lonely + 1.2f);
            excited = clamp(excited - 0.5f);
        }
    }

    /** 当前主导情绪（各维都低时视为平静，返回 null） */
    public String dominant() {
        float max = Math.max(happy, Math.max(angry, Math.max(lonely, excited)));
        if (happy >= 15 && happy >= max) return "开心";
        if (angry >= 15 && angry >= max) return "生气";
        if (excited >= 15 && excited >= max) return "兴奋";
        if (lonely >= 15) return "孤独";
        return null;
    }

    /** 情绪向量（注入 AI 提示词用）：当前四维 + 近期走势，纯数字 */
    public String describe() {
        StringBuilder trend = new StringBuilder("[");
        try {
            org.json.JSONArray l = new org.json.JSONArray(DataStore.sp().getString("moodVec", "[]"));
            int from = Math.max(0, l.length() - 5);
            for (int i = from; i < l.length(); i++) {
                org.json.JSONArray v = l.optJSONArray(i);
                if (v == null || v.length() < 4) continue;
                if (trend.length() > 1) trend.append(" → ");
                trend.append((int) v.optDouble(0)).append(',').append((int) v.optDouble(1)).append(',')
                        .append((int) v.optDouble(2)).append(',').append((int) v.optDouble(3));
            }
        } catch (Exception ignored) {
        }
        trend.append(']');
        return "心情向量[开心,生气,孤独,兴奋]=" + (int) happy + "," + (int) angry + "," + (int) lonely + ","
                + (int) excited + "，主导：" + (dominant() == null ? "平静" : dominant())
                + "；近期走势" + trend;
    }

    public void load() {
        JSONObject o = DataStore.obj("emotion");
        happy = (float) o.optDouble("happy", 50);
        angry = (float) o.optDouble("angry", 10);
        lonely = (float) o.optDouble("lonely", 20);
        excited = (float) o.optDouble("excited", 40);
    }

    public void save() {
        JSONObject o = new JSONObject();
        try {
            o.put("happy", happy).put("angry", angry).put("lonely", lonely).put("excited", excited);
        } catch (Exception ignored) {
        }
        DataStore.saveObj("emotion", o);
    }

    /** 心情向量存储：最近 10 条四维快照 [开心,生气,孤独,兴奋]（纯数字，不再存文本条目） */
    public void record() {
        org.json.JSONArray snap = new org.json.JSONArray();
        try {
            snap.put(Math.round(happy)).put(Math.round(angry)).put(Math.round(lonely)).put(Math.round(excited));
        } catch (Exception ignored) {
        }
        org.json.JSONArray l;
        try {
            l = new org.json.JSONArray(DataStore.sp().getString("moodVec", "[]"));
        } catch (Exception e) {
            l = new org.json.JSONArray();
        }
        l.put(snap);
        while (l.length() > 10) l.remove(0);
        DataStore.sp().edit().putString("moodVec", l.toString()).apply();
    }

    /** 旧版 moodLog（文本条目）一次性迁移成向量 */
    public static void migrateMoodLog() {
        try {
            if (!DataStore.sp().getString("moodVec", "[]").equals("[]")) return;   // 已有向量
            List<JSONObject> old = DataStore.arr("moodLog");
            if (old.isEmpty()) return;
            org.json.JSONArray v = new org.json.JSONArray();
            for (JSONObject o : old) {
                String m = o.optString("mood", "平静");
                org.json.JSONArray snap = new org.json.JSONArray();
                if (m.equals("开心"))      snap.put(70).put(5).put(15).put(30);
                else if (m.equals("生气")) snap.put(15).put(70).put(25).put(30);
                else if (m.equals("孤独")) snap.put(15).put(10).put(75).put(15);
                else if (m.equals("兴奋")) snap.put(60).put(10).put(10).put(85);
                else                       snap.put(40).put(10).put(30).put(35);
                v.put(snap);
            }
            DataStore.sp().edit().putString("moodVec", v.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    /** 四维向量 → 主导情绪（供渲染快照） */
    private static String dominantOf(float h, float a, float lo, float e) {
        float max = Math.max(h, Math.max(a, Math.max(lo, e)));
        if (h >= 15 && h >= max) return "开心";
        if (a >= 15 && a >= max) return "生气";
        if (e >= 15 && e >= max) return "兴奋";
        if (lo >= 15) return "孤独";
        return "平静";
    }

    /** 走势图：向量快照的表情点阵 + 当前四维数值 */
    public static String chart() {
        migrateMoodLog();
        StringBuilder sb = new StringBuilder();
        String cur = "平静";
        try {
            org.json.JSONArray l = new org.json.JSONArray(DataStore.sp().getString("moodVec", "[]"));
            for (int i = 0; i < l.length(); i++) {
                org.json.JSONArray v = l.optJSONArray(i);
                if (v == null || v.length() < 4) continue;
                cur = dominantOf((float) v.optDouble(0), (float) v.optDouble(1),
                        (float) v.optDouble(2), (float) v.optDouble(3));
                String mark = "😐";
                if (cur.equals("开心")) mark = "😊";
                else if (cur.equals("生气")) mark = "😠";
                else if (cur.equals("孤独")) mark = "😞";
                else if (cur.equals("兴奋")) mark = "🤩";
                sb.append(mark).append(' ');
            }
        } catch (Exception ignored) {
        }
        if (sb.length() == 0) return "（还没有心情记录）";
        String curVec = "（宠物未运行）";
        if (PetService.instance != null) {
            Emotion em = PetService.instance.emo;
            curVec = "开心" + (int) em.happy + ",生气" + (int) em.angry + ",孤独" + (int) em.lonely
                    + ",兴奋" + (int) em.excited;
        }
        return sb.toString().trim() + "  当前：" + curVec;
    }

    /** 全量重建列表（DataStore.arr 的便捷复制） */
    public static List<JSONObject> emptyList() { return new ArrayList<JSONObject>(); }
}
