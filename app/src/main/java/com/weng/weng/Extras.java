package com.weng.weng;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** 随机事件 / 小剧场 / 小游戏 / 今日运势 / 成就 / 教程 / 饲养指南文案 */
public class Extras {

    // ================= 随机事件（带冷却） =================

    /** 每 3 分钟轮询一次；返回事件文案 or null。概率与冷却对齐电脑版 */
    public static String randomEvent(Random rnd) {
        long now = System.currentTimeMillis();
        // 突然提问 5%，30 分钟冷却
        if (rnd.nextInt(100) < 5 && now - DataStore.getLong("evAsk", 0) > 1800000L) {
            DataStore.putLong("evAsk", now);
            return "💬 " + Quotes.pick("ev_ask", rnd);
        }
        // 讲故事 1%，2 小时冷却
        if (rnd.nextInt(100) < 1 && now - DataStore.getLong("evStory", 0) > 7200000L) {
            DataStore.putLong("evStory", now);
            return "📖 " + Quotes.pick("ev_story", rnd);
        }
        // 心情波动 3%，40 分钟冷却
        if (rnd.nextInt(100) < 3 && now - DataStore.getLong("evMood", 0) > 2400000L) {
            DataStore.putLong("evMood", now);
            return Quotes.pick("ev_mood", rnd);
        }
        return null;
    }

    // ================= 小游戏 =================

    /** 抽一个小游戏（带冷却 1 小时），返回 [标题, 题面] 或 null */
    public static String[] miniGame(Random rnd) {
        long now = System.currentTimeMillis();
        if (rnd.nextInt(100) >= 2) return null;
        if (now - DataStore.getLong("evGame", 0) < 3600000L) return null;
        DataStore.putLong("evGame", now);
        int k = rnd.nextInt(3);
        if (k == 0) {
            int target = 1 + rnd.nextInt(100);
            DataStore.putInt("gameAnswer", target);
            return new String[]{"🎯 猜数字", "我心里想了个 1-100 的数，在设置页跟我说答案！（" + target + "）"};
        }
        if (k == 1) {
            DataStore.putInt("gameAnswer", 0);   // 谜底无数字
            return new String[]{"🧩 谜语", "什么东西越洗越脏？（去设置页聊天里告诉我答案：水）"};
        }
        int a = 10 + rnd.nextInt(80), b = 10 + rnd.nextInt(80);
        DataStore.putInt("gameAnswer", a * b);
        return new String[]{"🧮 心算挑战", a + " × " + b + " 等于几？去设置页告诉我！（" + (a * b) + "）"};
    }

    // ================= 今日运势（日期种子，同一天固定） =================

    public static String fortune() {
        String day = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        int seed = 0;
        for (char c : day.toCharArray()) seed = seed * 31 + c;
        Random r = new Random(seed);
        int stars = 1 + r.nextInt(5);
        String[] luck = {"大凶", "小凶", "平", "小吉", "大吉"};
        StringBuilder sb = new StringBuilder("🔮 今日运势：" + luck[stars - 1] + " ");
        for (int i = 0; i < stars; i++) sb.append('⭐');
        String[] tips = {"宜摸蚊子头，好运 +1", "宜喝水八杯，水逆退散", "忌久坐，起来蹦跶两下", "宜夸我今天可爱", "宜专注一小时，财运亨通", "忌熬夜，蚊子都要睡了"};
        sb.append('\n').append(tips[r.nextInt(tips.length)]);
        return sb.toString();
    }

    // ================= AI 小剧场（本地生成二选一；AI 版在 aiChat 里按 askTheater 触发） =================

    /** 随机触发小剧场（2%，1 小时冷却）：返回 [场景, 选项A, 选项B] 或 null */
    public static String[] theater(Random rnd) {
        long now = System.currentTimeMillis();
        if (rnd.nextInt(100) >= 2) return null;
        if (now - DataStore.getLong("evTheater", 0) < 3600000L) return null;
        DataStore.putLong("evTheater", now);
        List<String> ths = Quotes.get("theater");
        String[] t = ths.get(rnd.nextInt(ths.size())).split("\\|");
        return t.length == 3 ? t : null;
    }

    /** 小剧场结算：A 倾向 +5~15，B 随机 -10~+10；记入回忆录（最近 100 条） */
    public static String theaterResult(int choiceIdx, String scene, String choice) {
        Random rnd = new Random();
        int delta;
        if (choiceIdx == 0) delta = 5 + rnd.nextInt(11);
        else delta = -10 + rnd.nextInt(21);
        int prev = DataStore.getAff();
        DataStore.addAff(delta);
        int ms = DataStore.milestoneCrossed(prev, DataStore.getAff());
        // 回忆录
        List<JSONObject> l = DataStore.arr("theaterLog");
        JSONObject o = new JSONObject();
        try {
            o.put("t", new SimpleDateFormat("MM-dd HH:mm", Locale.US).format(new Date()));
            o.put("scene", scene).put("choice", choice).put("delta", delta);
        } catch (Exception ignored) {
        }
        l.add(o);
        while (l.size() > 100) l.remove(0);
        DataStore.saveArr("theaterLog", l);
        String feel = delta >= 10 ? "开心到原地转圈！" : delta >= 5 ? "偷偷美滋滋～" : delta >= 0 ? "还行吧，勉强原谅你" : delta >= -5 ? "哼，有点小失落" : "气到贴边飞行！";
        return "🎭 好感 " + (delta >= 0 ? "+" : "") + delta + "　" + feel + (ms > 0 ? "\n✨ 顺带跨过 " + ms + " 里程碑！" : "");
    }

    // ================= 成就 =================

    public static final String[][] ACHIEVEMENTS = {
            {"灵魂契约 💖", "好感度达到 3000"},
            {"自律达人 📅", "任意习惯连续打卡 7 天"},
            {"血祭之王 🩸", "累计献血 3000"},
            {"专注大师 🍅", "累计专注 20 场"},
            {"水润少年 💧", "单日喝满 8 杯水"},
            {"话痨之友 💬", "和蚊子说过 50 句话"},
    };

    public static String checkAchievements() {
        StringBuilder sb = new StringBuilder();
        int aff = DataStore.getAff();
        if (aff >= 3000 && !DataStore.getBool("ach_soul", false)) {
            DataStore.putBool("ach_soul", true);
            sb.append("🏆 解锁成就：灵魂契约 💖\n");
        }
        if (DataStore.getBloodTotal() >= 3000 && !DataStore.getBool("ach_blood", false)) {
            DataStore.putBool("ach_blood", true);
            sb.append("🏆 解锁成就：血祭之王 🩸\n");
        }
        if (DataStore.getInt("focusTotalN", 0) >= 20 && !DataStore.getBool("ach_focus", false)) {
            DataStore.putBool("ach_focus", true);
            sb.append("🏆 解锁成就：专注大师 🍅\n");
        }
        if (Planner.waterToday() >= 8 && !DataStore.getBool("ach_water_" + todayKey(), false)) {
            DataStore.putBool("ach_water_" + todayKey(), true);
            if (!DataStore.getBool("ach_water", false)) {
                DataStore.putBool("ach_water", true);
                sb.append("🏆 解锁成就：水润少年 💧\n");
            }
        }
        for (org.json.JSONObject h : Planner.habits()) {
            java.util.List<String> dates = new java.util.ArrayList<String>();
            try {
                org.json.JSONArray a = h.getJSONArray("dates");
                for (int i = 0; i < a.length(); i++) dates.add(a.getString(i));
            } catch (Exception ignored) {
            }
            if (Planner.streakOf(dates) >= 7 && !DataStore.getBool("ach_streak", false)) {
                DataStore.putBool("ach_streak", true);
                sb.append("🏆 解锁成就：自律达人 📅\n");
            }
        }
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    private static String todayKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    // ================= 教程（7 步，气泡序列） =================

    // ================= 饲养指南 =================

    public static String guideText() {
        return "【基础】点=戳（嗯？）；连点三下=拍扁（2 秒复活）；按住 0.65s=摸头 +好感；长按 1.5s=打开设置；拖动=搬家；甩出=抛物线扔出去（甩得越快飞得越远，左右墙会弹，落地晕一会）；双指捏合=缩放。\n"
                + "【状态】cruise 巡航/dash 冲刺/zigzag 之字/沿边巡逻/角落休息/探头/绕圈/翻筋斗/俯冲/8字/蝴蝶飘/打盹(22-7点)/眩晕等 16+ 种。飞到屏幕下方会慢悠悠靠到底边站住、再连跳 1~10 下、然后飞走；勿扰模式则是慢慢沉到屏幕底部静静站着。\n"
                + "【好感】每日上限 50；500/1500/3000＝泛泛之交→渐生情愫→亲密无间→灵魂契约；摸头/喂血/待办/习惯/专注都能涨。\n"
                + "【饲养】饱食度按真实时间流逝下降（白天约 12.6/小时，夜里 22-7 点掉 1.6 倍；关掉 App 的时间也照算），设置页有进度条可视化。点「献血」可以无限点、没有冷却也没有资源限制；每口随机吸 15~40 血，吸多少血涨多少饱食度，18% 暴击双倍。但蚊子有约 20% 概率耍脾气拒绝（挑食/不饿），拒绝就什么也不发生。顶到 100 会满足地打个嗝，累计献血会解锁五档称号。\n"
                + "【提醒】喝水/久坐/DDL 提前 30 分钟预警/整点报时；会议 App 自动勿扰。\n"
                + "【气泡】撞墙、滑翔这类碎碎念不会打断重要消息（AI 回复/提醒/成就等会优先显示完整）。\n"
                + "【AI】会主动搭话，节奏自己调：基准间隔 n 分钟 + 动态抖动 ±%（默认 10 分钟 ±50%），每天主动条数可设上限也可不限；有 10 轮记忆；感知你在用什么 App。\n"
                + "【其他】手账（待办/习惯/随手记）、专注监督器、小剧场二选一、小游戏、今日运势、成就、每日天气。";
    }
}
