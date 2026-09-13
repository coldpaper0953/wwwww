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

    /** 情绪四维快照文本（注入 AI 提示词用） */
    public String describe() {
        return "开心" + (int) happy + "/生气" + (int) angry + "/孤独" + (int) lonely + "/兴奋" + (int) excited
                + "，主导情绪：" + (dominant() == null ? "平静" : dominant());
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

    /** 最近 10 条心情记录（时间+主导情绪），供走势图显示 */
    public static void record(String mood) {
        if (mood == null) mood = "平静";
        List<JSONObject> l = DataStore.arr("moodLog");
        JSONObject o = new JSONObject();
        try {
            o.put("t", new SimpleDateFormat("MM-dd HH:mm", Locale.US).format(new Date()));
            o.put("mood", mood);
        } catch (Exception ignored) {
        }
        l.add(o);
        while (l.size() > 10) l.remove(0);
        DataStore.saveArr("moodLog", l);
    }

    /** 走势图：10 条记录的表情点阵 + 当前主导情绪 */
    public static String chart() {
        List<JSONObject> l = DataStore.arr("moodLog");
        if (l.isEmpty()) return "（还没有心情记录）";
        StringBuilder sb = new StringBuilder();
        String cur = "平静";
        for (JSONObject o : l) {
            String m = o.optString("mood", "平静");
            cur = m;
            String mark = "😐";
            if (m.equals("开心")) mark = "😊";
            else if (m.equals("生气")) mark = "😠";
            else if (m.equals("孤独")) mark = "😞";
            else if (m.equals("兴奋")) mark = "🤩";
            sb.append(mark).append(' ');
        }
        return sb.toString().trim() + "  当前：" + cur;
    }

    /** 全量重建列表（DataStore.arr 的便捷复制） */
    public static List<JSONObject> emptyList() { return new ArrayList<JSONObject>(); }
}
