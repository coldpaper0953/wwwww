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
            String[] asks = {"你觉得我可爱吗？诚实点！", "午饭吃的什么？别又是外卖…", "如果蚊子会许愿，你猜我许什么？",
                    "你手机里这么多 App，最喜欢哪个？", "说！今天有没有想我（一点点也算）"};
            return "💬 " + asks[rnd.nextInt(asks.length)];
        }
        // 讲故事 1%，2 小时冷却
        if (rnd.nextInt(100) < 1 && now - DataStore.getLong("evStory", 0) > 7200000L) {
            DataStore.putLong("evStory", now);
            String[] stories = {"我飞进过程序员的咖啡杯，差点被当 bug 修复…", "昨天我躲在耳机里听了一下午歌，白嫖！",
                    "我见过凌晨四点的手机屏幕，比你亮。", "有一次差点被电蚊拍追杀，我学会了蛇皮走位。"};
            return "📖 " + stories[rnd.nextInt(stories.length)];
        }
        // 心情波动 3%，40 分钟冷却
        if (rnd.nextInt(100) < 3 && now - DataStore.getLong("evMood", 0) > 2400000L) {
            DataStore.putLong("evMood", now);
            String[] moods = {"（突然有点emo）你说蚊子有朋友吗…", "今天莫名开心，想给你表演后空翻！",
                    "哼，说不上来，就是有点小情绪。", "（原地转圈）开心！没理由的开心！"};
            return moods[rnd.nextInt(moods.length)];
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
        String[][] pool = {
                {"你发现我半夜偷偷在你手机充电口旁边取暖", "假装没看见", "给我盖个小被子"},
                {"我在你屏幕上跳舞被卡组队邀请打断", "让它继续跳", "加入它一起跳"},
                {"我叼来一颗不知道哪来的糖放在你键盘上", "收下并道谢", "让它自己吃"},
                {"下雨天我淋湿了翅膀躲在状态栏里", "用纸巾给它擦擦", "让它自己晾干"},
                {"我宣布今天是我的生日（真的吗）", "半信半疑地庆祝", "戳穿并揉搓它"},
        };
        String[] t = pool[rnd.nextInt(pool.length)];
        return new String[]{t[0], t[1], t[2]};
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

    public static final String[] TUTORIAL = {
            "嗡嗡～我飞到你手机上啦！",
            "点我一下＝戳戳（第一下我会说嗯？）",
            "连点三下会把我拍扁…我会复活的！",
            "按住 0.65 秒＝温柔摸头，长按 1.5 秒＝打开设置",
            "两根手指捏我＝放大缩小",
            "设置页里有手账/献血/专注/成就，慢慢玩～",
            "那就…多多关照啦！嗡嗡～",
    };

    // ================= 饲养指南 =================

    public static String guideText() {
        return "【基础】点=戳（嗯？）；连点三下=拍扁（2 秒复活）；按住 0.65s=摸头 +好感；长按 1.5s=打开设置；拖动=搬家；甩出=坠落+眩晕；双指捏合=缩放。\n"
                + "【状态】cruise 巡航/dash 冲刺/zigzag 之字/沿边巡逻/角落休息/探头/绕圈/翻筋斗/俯冲/8字/蝴蝶飘/打盹(22-7点)/眩晕等 16+ 种。\n"
                + "【好感】每日上限 50；500/1500/3000＝泛泛之交→渐生情愫→亲密无间→灵魂契约；摸头/喂血/待办/习惯/专注都能涨。\n"
                + "【饲养】饱食度随时间下降；血池自动回；献血三段式叮咬；15% 挑食；18% 暴击金光；>90 硬喂会吃撑打嗝。\n"
                + "【提醒】喝水/久坐/DDL 提前 30 分钟预警/整点报时；会议 App 自动勿扰。\n"
                + "【AI】会主动搭话；有 10 轮记忆；感知你在用什么 App。\n"
                + "【其他】手账（待办/习惯/随手记）、专注监督器、小剧场二选一、小游戏、今日运势、成就、每日天气。";
    }
}
