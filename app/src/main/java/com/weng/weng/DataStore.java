package com.weng.weng;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** 全局存档：全部持久化收敛到这里（同一 SharedPreferences "weng"，与历史版本兼容） */
public class DataStore {

    private static SharedPreferences P;

    public static void init(Context c) {
        if (P == null) P = c.getApplicationContext().getSharedPreferences("weng", Context.MODE_PRIVATE);
    }

    public static SharedPreferences sp() { return P; }

    // ================= 好感度 / 称号 / 里程碑 =================

    public static final int[] TITLE_LINES = {500, 1500, 3000};
    public static final String[] TITLES = {"泛泛之交", "渐生情愫", "亲密无间", "灵魂契约"};

    public static String titleFor(int aff) {
        int idx = 0;
        for (int i = 0; i < TITLE_LINES.length; i++) {
            if (aff >= TITLE_LINES[i]) idx = i + 1;
        }
        return TITLES[idx];
    }

    private static String today() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    public static int getAff() { return P.getInt("aff", 0); }

    public static void setAffRaw(int v) { P.edit().putInt("aff", Math.max(0, v)).apply(); }

    /** 每日 50 点好感上限；返回 [实际增加, 是否被截断] */
    public static int[] addAff(int n) {
        if (n <= 0) {
            setAffRaw(getAff() + n);
            return new int[]{n, 0};
        }
        String t = today();
        int day = P.getInt("dailyAff", 0);
        if (!t.equals(P.getString("dailyDate", ""))) day = 0;
        int applied = Math.min(n, Math.max(0, 50 - day));
        setAffRaw(getAff() + applied);
        P.edit().putInt("dailyAff", day + applied)
                .putString("dailyDate", t)
                .putInt("affMax", Math.max(P.getInt("affMax", 0), getAff()))
                .apply();
        return new int[]{applied, applied < n ? 1 : 0};
    }

    /** 刚跨过的好感里程碑档（500/1500/3000），没有则 0 */
    public static int milestoneCrossed(int prev, int cur) {
        for (int line : TITLE_LINES) {
            if (prev < line && cur >= line) return line;
        }
        return 0;
    }

    public static void setPendingTheater(String tag) {
        P.edit().putString("pendingTheater", tag).apply();
    }

    public static String getPendingTheater() {
        String t = P.getString("pendingTheater", "");
        if (!t.isEmpty()) P.edit().putString("pendingTheater", "").apply();
        return t;
    }

    // ================= 饲养：饱食度 / 血池 / 献血称号 =================

    public static float getSatiety() { return P.getFloat("satiety", 70f); }

    public static void setSatiety(float v) { P.edit().putFloat("satiety", Math.max(0f, Math.min(100f, v))).apply(); }

    public static float getBlood() { return P.getFloat("blood", 30f); }

    public static void setBlood(float v) { P.edit().putFloat("blood", Math.max(0f, Math.min(100f, v))).apply(); }

    public static float getBloodTotal() { return P.getFloat("bloodTotal", 0f); }

    public static void addBloodTotal(float v) {
        float old = getBloodTotal();
        P.edit().putFloat("bloodTotal", old + v).apply();
    }

    /** 累计献血五档称号：0/250/600/1500/3000 */
    public static String bloodTitle() {
        float t = getBloodTotal();
        if (t >= 3000) return "血祭之王 👑";
        if (t >= 1500) return "献血达人 🏅";
        if (t >= 600) return "贫血战士 ⚔️";
        if (t >= 250) return "轻度供血者 💉";
        return "血源新手 🩸";
    }

    public static long getLong(String k, long def) { return P.getLong(k, def); }

    public static void putLong(String k, long v) { P.edit().putLong(k, v).apply(); }

    public static void setStuffedUntil(long until) { P.edit().putLong("stuffedUntil", until).apply(); }

    public static long getStuffedUntil() { return P.getLong("stuffedUntil", 0L); }

    // ================= 通用 JSON 数组（待办/习惯/记忆/回忆录/成就...） =================

    public static List<JSONObject> arr(String key) {
        List<JSONObject> r = new ArrayList<JSONObject>();
        try {
            JSONArray a = new JSONArray(P.getString(key, "[]"));
            for (int i = 0; i < a.length(); i++) r.add(a.getJSONObject(i));
        } catch (Exception ignored) {
        }
        return r;
    }

    public static void saveArr(String key, List<JSONObject> l) {
        JSONArray a = new JSONArray();
        for (JSONObject o : l) a.put(o);
        P.edit().putString(key, a.toString()).apply();
    }

    public static JSONObject obj(String key) {
        try {
            return new JSONObject(P.getString(key, "{}"));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static void saveObj(String key, JSONObject o) {
        P.edit().putString(key, o.toString()).apply();
    }

    // ================= 设置项（带默认值） =================

    public static int getInt(String k, int def) { return P.getInt(k, def); }

    public static void putInt(String k, int v) { P.edit().putInt(k, v).apply(); }

    public static boolean getBool(String k, boolean def) { return P.getBoolean(k, def); }

    public static void putBool(String k, boolean v) { P.edit().putBoolean(k, v).apply(); }

    public static float getFloat(String k, float def) { return P.getFloat(k, def); }

    public static void putFloat(String k, float v) { P.edit().putFloat(k, v).apply(); }
}
