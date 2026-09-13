package com.weng.weng;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Emoji → 开源矢量图标（Material Design Icons, Apache 2.0）替换工具。
 * Ico.s(ctx, "💬 聊天") 返回 Spannable，文本里的 emoji 变成行内 VectorDrawable。
 */
public class Ico {

    /** emoji → drawable 资源名（ic_Xxx.xml，由 _tools/gen_icons.py 生成自 @mdi/js） */
    public static final Map<String, String> MAP = new HashMap<String, String>();

    static {
        MAP.put("🏆", "ic_TrophyAward");
        MAP.put("💬", "ic_Chat");
        MAP.put("🩸", "ic_BloodBag");
        MAP.put("💧", "ic_Water");
        MAP.put("🍅", "ic_Food");
        MAP.put("📅", "ic_Calendar");
        MAP.put("🧠", "ic_Brain");
        MAP.put("✏", "ic_Pencil");
        MAP.put("✏️", "ic_Pencil");
        MAP.put("⚠", "ic_Alert");
        MAP.put("⚠️", "ic_Alert");
        MAP.put("📲", "ic_Cellphone");
        MAP.put("📖", "ic_BookOpenPageVariant");
        MAP.put("✨", "ic_AutoFix");
        MAP.put("💖", "ic_Heart");
        MAP.put("❤", "ic_Heart");
        MAP.put("❤️", "ic_Heart");
        MAP.put("✖", "ic_Close");
        MAP.put("✖️", "ic_Close");
        MAP.put("🗣", "ic_AccountVoice");
        MAP.put("🗣️", "ic_AccountVoice");
        MAP.put("🌙", "ic_WeatherNight");
        MAP.put("📚", "ic_BookMultiple");
        MAP.put("🦟", "ic_Bug");
        MAP.put("🦟️", "ic_Bug");
        MAP.put("👁", "ic_Eye");
        MAP.put("👁️", "ic_Eye");
        MAP.put("🕐", "ic_ClockOutline");
        MAP.put("🔮", "ic_CrystalBall");
        MAP.put("⭐", "ic_Star");
        MAP.put("🎭", "ic_DramaMasks");
        MAP.put("⚙", "ic_Cog");
        MAP.put("⚙️", "ic_Cog");
        MAP.put("🎮", "ic_GamepadVariant");
        MAP.put("🎉", "ic_PartyPopper");
        MAP.put("📝", "ic_NotebookEdit");
        MAP.put("📝️", "ic_NotebookEdit");
        MAP.put("📡", "ic_SatelliteUplink");
        MAP.put("👑", "ic_Crown");
        MAP.put("🏅", "ic_Medal");
        MAP.put("🎖", "ic_Medal");
        MAP.put("⚔", "ic_SwordCross");
        MAP.put("⚔️", "ic_SwordCross");
        MAP.put("💉", "ic_Needle");
        MAP.put("😐", "ic_EmoticonNeutral");
        MAP.put("😊", "ic_EmoticonHappy");
        MAP.put("😠", "ic_EmoticonAngry");
        MAP.put("😞", "ic_EmoticonSad");
        MAP.put("🤩", "ic_EmoticonExcited");
        MAP.put("🎯", "ic_Target");
        MAP.put("🧩", "ic_Puzzle");
        MAP.put("🧮", "ic_Calculator");
        MAP.put("🌀", "ic_WeatherTornado");
        MAP.put("🙋", "ic_HandOkay");
        MAP.put("🔀", "ic_ShuffleVariant");
        MAP.put("🔀️", "ic_ShuffleVariant");
        MAP.put("📃", "ic_FileDocument");
        MAP.put("🗑", "ic_TrashCan");
        MAP.put("🗑️", "ic_TrashCan");
        MAP.put("☀", "ic_WeatherSunny");
        MAP.put("☀️", "ic_WeatherSunny");
        MAP.put("☔", "ic_Umbrella");
        MAP.put("🤫", "ic_Sleep");
        MAP.put("🚨", "ic_AlarmLight");
        MAP.put("🏃", "ic_Run");
        MAP.put("🏃️", "ic_Run");
        MAP.put("🔔", "ic_Bell");
        MAP.put("✅", "ic_CheckBold");
        MAP.put("👀", "ic_Eye");
        MAP.put("🗒", "ic_Notebook");
        MAP.put("🗒️", "ic_Notebook");
        MAP.put("☑", "ic_CheckboxMarked");
        MAP.put("☑️", "ic_CheckboxMarked");
        MAP.put("☐", "ic_CheckboxBlank");
        MAP.put("🌊", "ic_Waves");
        MAP.put("🪧", "ic_SignText");
        MAP.put("🎵", "ic_Music");
        MAP.put("😩", "ic_EmoticonDead");
        MAP.put("🍽", "ic_SilverwareForkKnife");
        MAP.put("🍽️", "ic_SilverwareForkKnife");
        MAP.put("↩", "ic_Restore");
        MAP.put("↩️", "ic_Restore");
        MAP.put("🐝", "ic_Bee");
        MAP.put("🔒", "ic_Lock");
        MAP.put("🔒️", "ic_Lock");
        MAP.put("🔌", "ic_PowerPlug");
        MAP.put("🔌️", "ic_PowerPlug");
        MAP.put("💾", "ic_ContentSave");
        MAP.put("🔁", "ic_Repeat");
        MAP.put("🖐", "ic_HandBackRight");
        MAP.put("🖐️", "ic_HandBackRight");
        MAP.put("🔄", "ic_Autorenew");
        MAP.put("🔄️", "ic_Autorenew");
        MAP.put("🔍", "ic_Magnify");
        MAP.put("🔍️", "ic_Magnify");
        MAP.put("📊", "ic_ChartBar");
        MAP.put("🕊", "ic_Bird");
        MAP.put("🕊️", "ic_Bird");
    }

    /** emoji 串（按长度降序拼正则，防部分匹配） */
    private static final Pattern EMOJI = buildPattern();

    private static Pattern buildPattern() {
        StringBuilder sb = new StringBuilder();
        // 变体选择符 FE0F 跟随处理：把 "emoji(\uFE0F)?" 作为整体
        java.util.List<String> keys = new java.util.ArrayList<String>(MAP.keySet());
        java.util.Collections.sort(keys, (a, b) -> b.length() - a.length());
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            // 去掉 key 里已有的 FE0F 再加可选组
            String base = k.replace("\uFE0F", "");
            sb.append(Pattern.quote(base)).append("\\uFE0F?");
            if (i < keys.size() - 1) sb.append('|');
        }
        return Pattern.compile(sb.toString());
    }

    /** tint 颜色：iconTint 参数，null=黑（默认） */
    public static CharSequence s(Context ctx, String text) {
        return s(ctx, text, null);
    }

    public static CharSequence s(Context ctx, String text, Integer tint) {
        if (text == null) return "";
        if (!EMOJI.matcher(text).find()) return text;
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        Matcher m = EMOJI.matcher(text);
        while (m.find()) {
            String hit = m.group(0);
            String res = MAP.get(hit);
            if (res == null) res = MAP.get(hit.replace("\uFE0F", ""));
            if (res == null) continue;
            int id = ctx.getResources().getIdentifier(res, "drawable", ctx.getPackageName());
            if (id == 0) continue;
            Drawable d = ctx.getResources().getDrawable(id, null);
            if (d == null) continue;
            if (tint != null) d.setColorFilter(tint, PorterDuff.Mode.SRC_IN);
            int sz = (int) (d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : 36);
            // 18dp 图标与 13sp 文字基线对齐：用 CenterImageSpan
            d.setBounds(0, 0, sz, sz);
            CenterSpan span = new CenterSpan(d);
            sb.setSpan(span, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }

    /** 垂直居中的 ImageSpan（默认底对齐会沉底；无参构造兼容 API<29） */
    static class CenterSpan extends DynamicDrawableSpan {
        private final Drawable d;

        CenterSpan(Drawable d) {
            this.d = d;
        }

        @Override
        public Drawable getDrawable() {
            return d;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                          float x, int top, int y, int bottom, Paint paint) {
            canvas.save();
            int lineH = bottom - top;
            int iconH = d.getBounds().bottom;
            int transY = top + (lineH - iconH) / 2;
            canvas.translate(x, transY);
            d.draw(canvas);
            canvas.restore();
        }
    }
}
