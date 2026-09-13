package com.weng.weng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 统一台词库：全部宠物台词（本地文案）都在这里，默认种子 + 用户编辑覆盖。
 * 教程步骤也是一组台词（tutorial），用户可改。
 */
public class Quotes {

    public static final Map<String, Object[]> DEF = new HashMap<String, Object[]>();

    static {
        // 基础交互
        DEF.put("tap", new Object[]{"嗯？", "别闹…", "干嘛？", "戳啥呢"});
        DEF.put("throw", new Object[]{"哎呀！", "你干什么！", "喂——"});
        DEF.put("revive", new Object[]{"哼，我会复活的…你等着"});
        DEF.put("sleep", new Object[]{"Zzz…", "好困…", "抱着挺舒服…"});
        DEF.put("fallback", new Object[]{"嗡～信号不太好，等会儿再聊", "（信号弱）先自己玩会儿…", "嗡嗡…听不清，再说一遍？"});
        DEF.put("work_on", new Object[]{"好嘞，进入工作状态！"});
        DEF.put("work_off", new Object[]{"下班啦！"});
        DEF.put("glide", new Object[]{"woooo～惯性滑翔！", "飞喽～～"});
        DEF.put("bounce", new Object[]{"哎哟撞墙了…", "反弹～"});
        // 时段问候（timePeriod 的第二列）
        DEF.put("period_dawn", new Object[]{"都凌晨了还不睡？！"});          // 凌晨
        DEF.put("period_morning", new Object[]{"早起的蚊子有血吸～早！"});   // 清晨
        DEF.put("period_am", new Object[]{"上午好！今天打算干点什么？"});    // 上午
        DEF.put("period_noon", new Object[]{"中午啦，记得吃饭别糊弄～"});    // 中午
        DEF.put("period_pm", new Object[]{"下午好，起来活动活动～"});        // 下午
        DEF.put("period_dusk", new Object[]{"傍晚了，今天过得咋样？"});      // 傍晚
        DEF.put("period_night", new Object[]{"晚上好～放松一下吧"});         // 晚上
        DEF.put("period_late", new Object[]{"都深夜了，早点睡！"});          // 深夜
        // 随机事件
        DEF.put("ev_ask", new Object[]{"你觉得我可爱吗？诚实点！", "午饭吃的什么？别又是外卖…",
                "如果蚊子会许愿，你猜我许什么？", "你手机里这么多 App，最喜欢哪个？", "说！今天有没有想我（一点点也算）"});
        DEF.put("ev_story", new Object[]{"我飞进过程序员的咖啡杯，差点被当 bug 修复…", "昨天我躲在耳机里听了一下午歌，白嫖！",
                "我见过凌晨四点的手机屏幕，比你亮。", "有一次差点被电蚊拍追杀，我学会了蛇皮走位。"});
        DEF.put("ev_mood", new Object[]{"（突然有点emo）你说蚊子有朋友吗…", "今天莫名开心，想给你表演后空翻！",
                "哼，说不上来，就是有点小情绪。", "（原地转圈）开心！没理由的开心！"});
        // 小剧场场景（scene|A|B 一条三段）
        DEF.put("theater", new Object[]{
                "你发现我半夜偷偷在你手机充电口旁边取暖|假装没看见|给我盖个小被子",
                "我在你屏幕上跳舞被卡组队邀请打断|让它继续跳|加入它一起跳",
                "我叼来一颗不知道哪来的糖放在你键盘上|收下并道谢|让它自己吃",
                "下雨天我淋湿了翅膀躲在状态栏里|用纸巾给它擦擦|让它自己晾干",
                "我宣布今天是我的生日（真的吗）|半信半疑地庆祝|戳穿并揉搓它"});
        // 假报错（标题|内容）
        DEF.put("fake_error", new Object[]{
                "系统错误|Mosquito.dll 内存溢出：检测到生物组织。点击确定释放蚊子。",
                "鼠标驱动异常|光标正在被吸血。建议立即拍打屏幕。",
                "磁盘空间不足|C:\\蚊子卵 文件夹占用 500GB。是否清理？",
                "网络连接中断|检测到蚊子翅膀震动干扰 WiFi 信号。",
                "杀毒软件警告|发现蚊群正在繁殖。建议物理清除。",
                "系统更新|Windows 防蚊补丁 KB666666 安装失败。",
                "蓝屏预警|蚊子密度超过阈值。系统将于 3 秒后蓝屏。",
                "摄像头占用|蚊子正在使用你的摄像头直播它的飞行。"});
        // App 感知吐槽（类别|台词）
        DEF.put("appsense", new Object[]{
                "办公|又在弄表格文档？记得随手保存！", "办公|工作工作，你的老板知道你这么努力吗～",
                "视频|老板！这里有人摸鱼看视频！", "视频|看完这集就去干活哦～",
                "聊天|又在偷偷跟谁聊天呢？", "聊天|聊什么呢聊这么开心～",
                "游戏|作业/工作写完了吗就打游戏？", "游戏|带我一个！我当飞行单位！",
                "音乐|🎵 跟着节奏动起来～",
                "购物浏览|又剁手了？蚊子我吃土就行", "购物浏览|逛逛逛，钱包还好吗～"});
        // 教程（可编辑！）
        DEF.put("tutorial", new Object[]{
                "嗡嗡～我飞到你手机上啦！",
                "点我一下＝戳戳（第一下我会说嗯？）",
                "连点三下会把我拍扁…我会复活的！",
                "按住 0.65 秒＝温柔摸头，长按 1.5 秒＝打开设置",
                "两根手指捏我＝放大缩小",
                "设置页里有手账/献血/专注/成就，慢慢玩～",
                "那就…多多关照啦！嗡嗡～"});
        // 成就解锁（可改）
        DEF.put("achievement", new Object[]{"🏆 解锁成就：{name}", "恭喜！{name} 达成～"});
        // 里程碑
        DEF.put("milestone", new Object[]{"✨ 好感 {n} 里程碑达成！称号：{t}", "跨越 {n}！我们的羁绊变深了…"});
        // 饿/撑
        DEF.put("hungry", new Object[]{"😩 饿扁了…快献血啦…", "🍽️ 有点饿了，献血吗？"});
        DEF.put("stuffed", new Object[]{"呃……嗝……撑死了……"});
        // 主动搭话话题（Topic 池）
        DEF.put("topics", new Object[]{
                "求摸摸：蹭到用户手边讨摸摸", "催喝水：提醒用户今天喝水了没",
                "炫耀：吹嘘自己刚才一个俯冲躲过了什么", "编一条蚊子冷知识讲给用户听",
                "问用户午饭打算吃什么", "抱怨手机屏幕太亮晃眼睛",
                "夸用户今天看起来状态不错", "好奇地问问用户在忙什么",
                "宣布自己要开始绕圈圈锻炼了", "问用户喜不喜欢下雨天的味道",
                "模仿手机通知声吓用户一跳", "感叹一下今天飞了多少圈",
                "提议用户起来伸个懒腰", "想听听用户今天遇到的开心事",
                "抱怨自己差点被风扇吹跑", "问用户觉得蚊子算不算最可爱的宠物",
                "宣布要给用户表演一个后空翻（虽然不会）"});
    }

    // ================= 读写 =================

    private static Map<String, List<String>> cache = null;

    /** 用户覆盖层：{key: [lines]} */
    public static List<String> get(String key) {
        ensure();
        List<String> l = cache.get(key);
        if (l == null || l.isEmpty()) {
            Object[] d = DEF.get(key);
            if (d == null) return new ArrayList<String>();
            List<String> r = new ArrayList<String>();
            for (Object o : d) r.add(String.valueOf(o));
            return r;
        }
        return l;
    }

    public static String pick(String key, Random rnd) {
        List<String> l = get(key);
        if (l.isEmpty()) return "嗡？";
        return l.get(rnd.nextInt(l.size()));
    }

    /** 带占位符替换：{n} {t} {name} */
    public static String pick(String key, Random rnd, String n, String t, String name) {
        String s = pick(key, rnd);
        if (n != null) s = s.replace("{n}", n);
        if (t != null) s = s.replace("{t}", t);
        if (name != null) s = s.replace("{name}", name);
        return s;
    }

    public static void save(String key, List<String> lines) {
        ensure();
        cache.put(key, lines);
        persist();
    }

    public static void reset() {
        cache = new HashMap<String, List<String>>();
        DataStore.sp().edit().putString("quotesAll", "{}").apply();
    }

    private static synchronized void ensure() {
        if (cache != null) return;
        cache = new HashMap<String, List<String>>();
        try {
            JSONObject o = new JSONObject(DataStore.sp().getString("quotesAll", "{}"));
            java.util.Iterator<String> it = o.keys();
            while (it.hasNext()) {
                String k = it.next();
                JSONArray a = o.getJSONArray(k);
                List<String> l = new ArrayList<String>();
                for (int i = 0; i < a.length(); i++) {
                    String s = a.getString(i).trim();
                    if (!s.isEmpty()) l.add(s);
                }
                if (!l.isEmpty()) cache.put(k, l);
            }
        } catch (Exception ignored) {
        }
    }

    private static void persist() {
        try {
            JSONObject o = new JSONObject();
            for (Map.Entry<String, List<String>> e : cache.entrySet()) {
                JSONArray a = new JSONArray();
                for (String s : e.getValue()) a.put(s);
                o.put(e.getKey(), a);
            }
            DataStore.sp().edit().putString("quotesAll", o.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    /** 全部可编辑组（给编辑页列出；用户空覆盖＝回落默认） */
    public static Map<String, List<String>> all() {
        ensure();
        Map<String, List<String>> r = new HashMap<String, List<String>>();
        for (String k : DEF.keySet()) {
            List<String> user = cache.get(k);
            r.put(k, (user != null && !user.isEmpty()) ? user : get(k));
        }
        return r;
    }

    /** 编辑页显示名 */
    public static String label(String key) {
        if (key.equals("tap")) return "戳击";
        if (key.equals("throw")) return "被扔出";
        if (key.equals("revive")) return "复活";
        if (key.equals("sleep")) return "睡觉/勿扰";
        if (key.equals("fallback")) return "网络异常兜底";
        if (key.equals("work_on")) return "进入工作";
        if (key.equals("work_off")) return "退出工作";
        if (key.equals("glide")) return "惯性滑翔";
        if (key.equals("bounce")) return "撞墙";
        if (key.startsWith("period_")) return "时段问候：" + key.replace("period_", "");
        if (key.equals("ev_ask")) return "随机提问";
        if (key.equals("ev_story")) return "讲故事";
        if (key.equals("ev_mood")) return "心情波动";
        if (key.equals("theater")) return "小剧场（场景|选项A|选项B）";
        if (key.equals("fake_error")) return "伪造报错（标题|内容）";
        if (key.equals("appsense")) return "App吐槽（类别|台词）";
        if (key.equals("tutorial")) return "新手教程（每行一步）";
        if (key.equals("achievement")) return "成就解锁（{name}占位）";
        if (key.equals("milestone")) return "好感里程碑（{n}{t}占位）";
        if (key.equals("hungry")) return "肚子饿";
        if (key.equals("stuffed")) return "吃撑打嗝";
        if (key.equals("topics")) return "主动搭话话题库";
        return key;
    }
}
