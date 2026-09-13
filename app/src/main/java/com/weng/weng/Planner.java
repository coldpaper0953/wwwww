package com.weng.weng;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** 手账数据层：待办(含DDL)/习惯打卡/喝水8杯/随手记 + 提醒调度 + 专注统计 */
public class Planner {

    // ================= 待办 =================
    // JSON: {text, ddl:"HH:mm"或空, done:false, created:"MM-dd HH:mm"}

    public static List<JSONObject> todos() { return DataStore.arr("todos"); }

    public static void addTodo(String text, String ddl) {
        List<JSONObject> l = todos();
        JSONObject o = new JSONObject();
        try {
            o.put("text", text).put("ddl", ddl == null ? "" : ddl)
                    .put("done", false)
                    .put("created", new SimpleDateFormat("MM-dd HH:mm", Locale.US).format(new Date()));
        } catch (Exception ignored) {
        }
        l.add(o);
        DataStore.saveArr("todos", l);
    }

    /** 完成待办：返回 [成功?, 文案]；好感+5 血池+8 由 PetService 结算 */
    public static String completeTodo(int idx) {
        List<JSONObject> l = todos();
        if (idx < 0 || idx >= l.size()) return null;
        JSONObject o = l.get(idx);
        if (o.optBoolean("done", false)) return null;
        try {
            o.put("done", true);
        } catch (Exception ignored) {
        }
        DataStore.saveArr("todos", l);
        return o.optString("text", "");
    }

    public static void removeTodo(int idx) {
        List<JSONObject> l = todos();
        if (idx >= 0 && idx < l.size()) {
            l.remove(idx);
            DataStore.saveArr("todos", l);
        }
    }

    public static int openCount() {
        int n = 0;
        for (JSONObject o : todos()) if (!o.optBoolean("done", false)) n++;
        return n;
    }

    // ================= 习惯 =================
    // JSON: {name, dates:["yyyyMMdd",...]}

    public static List<JSONObject> habits() { return DataStore.arr("habits"); }

    public static void addHabit(String name) {
        List<JSONObject> l = habits();
        JSONObject o = new JSONObject();
        try {
            o.put("name", name).put("dates", new org.json.JSONArray());
        } catch (Exception ignored) {
        }
        l.add(o);
        DataStore.saveArr("habits", l);
    }

    public static void removeHabit(int idx) {
        List<JSONObject> l = habits();
        if (idx >= 0 && idx < l.size()) {
            l.remove(idx);
            DataStore.saveArr("habits", l);
        }
    }

    /** 打卡（幂等）：返回 [成功?, 连续天数] */
    public static Object[] checkHabit(int idx) {
        List<JSONObject> l = habits();
        if (idx < 0 || idx >= l.size()) return new Object[]{false, 0};
        JSONObject o = l.get(idx);
        String today = todayStr();
        List<String> dates = new ArrayList<String>();
        try {
            org.json.JSONArray a = o.getJSONArray("dates");
            for (int i = 0; i < a.length(); i++) dates.add(a.getString(i));
        } catch (Exception ignored) {
        }
        if (dates.contains(today)) return new Object[]{false, streakOf(dates) + 1};
        dates.add(today);
        try {
            o.put("dates", new org.json.JSONArray(dates));
        } catch (Exception ignored) {
        }
        DataStore.saveArr("habits", l);
        return new Object[]{true, streakOf(dates) + 1};
    }

    /** 连续打卡天数（含今天） */
    public static int streakOf(List<String> dates) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Calendar c = Calendar.getInstance();
        int streak = 0;
        while (true) {
            String d = f.format(c.getTime());
            if (dates.contains(d)) {
                streak++;
                c.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                return streak;
            }
        }
    }

    public static String habitCard(int idx) {
        List<JSONObject> l = habits();
        if (idx < 0 || idx >= l.size()) return "";
        JSONObject o = l.get(idx);
        List<String> dates = new ArrayList<String>();
        try {
            org.json.JSONArray a = o.getJSONArray("dates");
            for (int i = 0; i < a.length(); i++) dates.add(a.getString(i));
        } catch (Exception ignored) {
        }
        boolean today = dates.contains(todayStr());
        return o.optString("name", "") + (today ? " ✓今日已打卡" : " （连续 " + streakOf(dates) + " 天）");
    }

    // ================= 喝水 8 杯 =================

    public static int waterToday() {
        JSONObject o = DataStore.obj("water");
        if (!todayStr().equals(o.optString("date", ""))) return 0;
        return o.optInt("count", 0);
    }

    public static int drinkWater() {
        JSONObject o = DataStore.obj("water");
        int c = todayStr().equals(o.optString("date", "")) ? o.optInt("count", 0) : 0;
        c = Math.min(8, c + 1);
        try {
            o.put("date", todayStr()).put("count", c);
        } catch (Exception ignored) {
        }
        DataStore.saveObj("water", o);
        return c;
    }

    // ================= 随手记 =================

    public static String notes() { return DataStore.sp().getString("notes", ""); }

    public static void saveNotes(String s) {
        DataStore.sp().edit().putString("notes", s == null ? "" : s).apply();
    }

    // ================= 提醒（喝水/久坐/DDL/整点报时，由 PetService 每分钟轮询） =================

    /** 返回应弹的提醒文案；null=无。重复轮次/60s重催逻辑内建 */
    public static String pollReminders() {
        long now = System.currentTimeMillis();
        String today = todayStr();
        SimpleDateFormat hf = new SimpleDateFormat("HH:mm", Locale.US);
        String hm = hf.format(new Date());

        // ---- DDL 提前提醒 ----
        List<JSONObject> l = todos();
        for (JSONObject o : l) {
            if (o.optBoolean("done", false)) continue;
            String ddl = o.optString("ddl", "");
            if (ddl.length() != 5) continue;
            int[] left = minutesTo(ddl);
            if (left == null) continue;
            String key = "ddl_" + o.optString("text", "") + "_" + today;
            int early = DataStore.getInt("ddlEarly", 30);
            if (left[0] <= early && left[0] >= 0) {
                int fired = DataStore.getInt(key, 0);
                if (fired < 2) {
                    DataStore.putInt(key, fired + 1);
                    if (fired == 0) {
                        return "⏰ 待办「" + o.optString("text", "") + "」还有 " + left[0] + " 分钟截止！";
                    } else if (left[0] <= 5) {
                        return "🚨 快去交差！「" + o.optString("text", "") + "」马上到期！";
                    }
                }
            }
        }

        // ---- 喝水提醒 ----
        int waterGap = DataStore.getInt("waterGap", 45);
        int waterReps = DataStore.getInt("waterReps", 2);
        String wk = "water_" + today;
        long lastAt = DataStore.getLong("waterLast", 0);
        int reps = DataStore.getInt(wk, 0);
        if (reps < waterReps && now - lastAt > waterGap * 60000L) {
            DataStore.putLong("waterLast", now);
            DataStore.putInt(wk, reps + 1);
            if (waterToday() < 8) {
                return "💧 喝水时间到！今天才喝了 " + waterToday() + "/8 杯～";
            }
        }

        // ---- 久坐提醒 ----
        int sitGap = DataStore.getInt("sitGap", 60);
        int sitReps = DataStore.getInt("sitReps", 3);
        String sk = "sit_" + today;
        long sitLast = DataStore.getLong("sitLast", 0);
        int sreps = DataStore.getInt(sk, 0);
        if (sreps < sitReps && now - sitLast > sitGap * 60000L) {
            DataStore.putLong("sitLast", now);
            DataStore.putInt(sk, sreps + 1);
            return "🏃 站起来走动一下！坐太久对蚊子和你都不好～";
        }

        // ---- 整点报时 ----
        if (new Date().getMinutes() == 0) {
            String hk = "hourly_" + today + "_" + hm;
            if (!DataStore.getBool(hk, false)) {
                DataStore.putBool(hk, true);
                int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                return "🔔 " + String.format(Locale.US, "%02d", h) + " 点整！起来活动活动～";
            }
        }
        return null;
    }

    /** 距 ddl("HH:mm") 的分钟数 [分钟]，已过返回 [0]，格式错返回 null */
    private static int[] minutesTo(String hm) {
        try {
            String[] p = hm.split(":");
            int h = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            Calendar now = Calendar.getInstance();
            Calendar ddl = Calendar.getInstance();
            ddl.set(Calendar.HOUR_OF_DAY, h);
            ddl.set(Calendar.MINUTE, m);
            ddl.set(Calendar.SECOND, 0);
            long diff = ddl.getTimeInMillis() - now.getTimeInMillis();
            return new int[]{(int) Math.max(0, diff / 60000L)};
        } catch (Exception e) {
            return null;
        }
    }

    private static String todayStr() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    // ================= 专注监督器 =================
    // {active:bool, mode:'free'|'pomo', targetMin, startAt, pausedAt, pausedTotal, goal, fly:bool, todayCount, todayMin, totalCount, totalMin}

    public static JSONObject focus() { return DataStore.obj("focus"); }

    public static void startFocus(int minutes, boolean pomo, String goal, boolean keepFly) {
        JSONObject o = focus();
        try {
            o.put("active", true).put("mode", pomo ? "pomo" : "free")
                    .put("targetMin", minutes).put("startAt", System.currentTimeMillis())
                    .put("pausedTotal", 0L).put("pausedAt", 0L)
                    .put("goal", goal == null ? "" : goal).put("fly", keepFly);
        } catch (Exception ignored) {
        }
        DataStore.saveObj("focus", o);
    }

    public static void pauseFocus() {
        JSONObject o = focus();
        if (o.optBoolean("active", false) && o.optLong("pausedAt", 0) == 0) {
            try {
                o.put("pausedAt", System.currentTimeMillis());
            } catch (Exception ignored) {
            }
            DataStore.saveObj("focus", o);
        }
    }

    public static void resumeFocus() {
        JSONObject o = focus();
        if (o.optBoolean("active", false) && o.optLong("pausedAt", 0) > 0) {
            try {
                o.put("pausedTotal", o.optLong("pausedTotal", 0) + System.currentTimeMillis() - o.optLong("pausedAt", 0));
                o.put("pausedAt", 0L);
            } catch (Exception ignored) {
            }
            DataStore.saveObj("focus", o);
        }
    }

    public static void stopFocus() {
        JSONObject o = focus();
        try {
            o.put("active", false);
        } catch (Exception ignored) {
        }
        DataStore.saveObj("focus", o);
    }

    /** 已专注秒数（扣除暂停）；到点返回 true */
    public static boolean focusTick() {
        JSONObject o = focus();
        if (!o.optBoolean("active", false)) return false;
        long paused = o.optLong("pausedTotal", 0);
        if (o.optLong("pausedAt", 0) > 0) paused += System.currentTimeMillis() - o.optLong("pausedAt", 0);
        long sec = (System.currentTimeMillis() - o.optLong("startAt", 0) - paused) / 1000L;
        return sec >= o.optInt("targetMin", 25) * 60L;
    }

    /** 专注进度文案（表盘文字用） */
    public static String focusText() {
        JSONObject o = focus();
        if (!o.optBoolean("active", false)) return "未在专注";
        long paused = o.optLong("pausedTotal", 0);
        if (o.optLong("pausedAt", 0) > 0) paused += System.currentTimeMillis() - o.optLong("pausedAt", 0);
        long sec = Math.max(0, (System.currentTimeMillis() - o.optLong("startAt", 0) - paused) / 1000L);
        int target = o.optInt("targetMin", 25) * 60;
        long left = Math.max(0, target - sec);
        boolean pausedNow = o.optLong("pausedAt", 0) > 0;
        return (pausedNow ? "⏸ 已暂停 " : "") + String.format(Locale.US, "%02d:%02d", left / 60, left % 60);
    }

    /** 专注完成结算：今日/累计统计+好感10+血池15（返回结算文案） */
    public static String finishFocus() {
        JSONObject o = focus();
        int mins = o.optInt("targetMin", 25);
        String today = todayStr();
        String tk = "focusToday_" + today;
        DataStore.putInt(tk, DataStore.getInt(tk, 0) + 1);
        DataStore.putInt(tk + "min", DataStore.getInt(tk + "min", 0) + mins);
        DataStore.putInt("focusTotalN", DataStore.getInt("focusTotalN", 0) + 1);
        DataStore.putInt("focusTotalMin", DataStore.getInt("focusTotalMin", 0) + mins);
        stopFocus();
        return "🎉 专注完成 " + mins + " 分钟！蚊子给你撒花～";
    }
}
