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
        MAP.put("🏆", "ic_trophyaward");
        MAP.put("💬", "ic_chat");
        MAP.put("🩸", "ic_bloodbag");
        MAP.put("💧", "ic_water");
        MAP.put("🍅", "ic_food");
        MAP.put("📅", "ic_calendar");
        MAP.put("🧠", "ic_brain");
        MAP.put("✏", "ic_pencil");
        MAP.put("✏️", "ic_pencil");
        MAP.put("⚠", "ic_alert");
        MAP.put("⚠️", "ic_alert");
        MAP.put("📲", "ic_cellphone");
        MAP.put("📖", "ic_bookopenpagevariant");
        MAP.put("✨", "ic_autofix");
        MAP.put("💖", "ic_heart");
        MAP.put("❤", "ic_heart");
        MAP.put("❤️", "ic_heart");
        MAP.put("✖", "ic_close");
        MAP.put("✖️", "ic_close");
        MAP.put("🗣", "ic_accountvoice");
        MAP.put("🗣️", "ic_accountvoice");
        MAP.put("🌙", "ic_weathernight");
        MAP.put("📚", "ic_bookmultiple");
        MAP.put("🦟", "ic_bug");
        MAP.put("🦟️", "ic_bug");
        MAP.put("👁", "ic_eye");
        MAP.put("👁️", "ic_eye");
        MAP.put("🕐", "ic_clockoutline");
        MAP.put("🔮", "ic_crystalball");
        MAP.put("⭐", "ic_star");
        MAP.put("🎭", "ic_dramamasks");
        MAP.put("⚙", "ic_cog");
        MAP.put("⚙️", "ic_cog");
        MAP.put("🎮", "ic_gamepadvariant");
        MAP.put("🎉", "ic_partypopper");
        MAP.put("📝", "ic_notebookedit");
        MAP.put("📝️", "ic_notebookedit");
        MAP.put("📡", "ic_satelliteuplink");
        MAP.put("👑", "ic_crown");
        MAP.put("🏅", "ic_medal");
        MAP.put("🎖", "ic_medal");
        MAP.put("⚔", "ic_swordcross");
        MAP.put("⚔️", "ic_swordcross");
        MAP.put("💉", "ic_needle");
        MAP.put("😐", "ic_emoticonneutral");
        MAP.put("😊", "ic_emoticonhappy");
        MAP.put("😠", "ic_emoticonangry");
        MAP.put("😞", "ic_emoticonsad");
        MAP.put("🤩", "ic_emoticonexcited");
        MAP.put("🎯", "ic_target");
        MAP.put("🧩", "ic_puzzle");
        MAP.put("🧮", "ic_calculator");
        MAP.put("🌀", "ic_weathertornado");
        MAP.put("🙋", "ic_handokay");
        MAP.put("🔀", "ic_shufflevariant");
        MAP.put("🔀️", "ic_shufflevariant");
        MAP.put("📃", "ic_filedocument");
        MAP.put("🗑", "ic_trashcan");
        MAP.put("🗑️", "ic_trashcan");
        MAP.put("☀", "ic_weathersunny");
        MAP.put("☀️", "ic_weathersunny");
        MAP.put("☔", "ic_umbrella");
        MAP.put("🤫", "ic_sleep");
        MAP.put("🚨", "ic_alarmlight");
        MAP.put("🏃", "ic_run");
        MAP.put("🏃️", "ic_run");
        MAP.put("🔔", "ic_bell");
        MAP.put("✅", "ic_checkbold");
        MAP.put("👀", "ic_eye");
        MAP.put("🗒", "ic_notebook");
        MAP.put("🗒️", "ic_notebook");
        MAP.put("☑", "ic_checkboxmarked");
        MAP.put("☑️", "ic_checkboxmarked");
        MAP.put("☐", "ic_checkboxblank");
        MAP.put("🌊", "ic_waves");
        MAP.put("🪧", "ic_signtext");
        MAP.put("🎵", "ic_music");
        MAP.put("😩", "ic_emoticondead");
        MAP.put("🍽", "ic_silverwareforkknife");
        MAP.put("🍽️", "ic_silverwareforkknife");
        MAP.put("↩", "ic_restore");
        MAP.put("↩️", "ic_restore");
        MAP.put("🐝", "ic_bee");
        MAP.put("🔒", "ic_lock");
        MAP.put("🔒️", "ic_lock");
        MAP.put("🔌", "ic_powerplug");
        MAP.put("🔌️", "ic_powerplug");
        MAP.put("💾", "ic_contentsave");
        MAP.put("🔁", "ic_repeat");
        MAP.put("🖐", "ic_handbackright");
        MAP.put("🖐️", "ic_handbackright");
        MAP.put("🔄", "ic_autorenew");
        MAP.put("🔄️", "ic_autorenew");
        MAP.put("🔍", "ic_magnify");
        MAP.put("🔍️", "ic_magnify");
        MAP.put("📊", "ic_chartbar");
        MAP.put("🕊", "ic_bird");
        MAP.put("🕊️", "ic_bird");
        MAP.put("⏰", "ic_clockoutline");
        MAP.put("⏰️", "ic_clockoutline");
        MAP.put("▶", "ic_play");
        MAP.put("▶️", "ic_play");
        MAP.put("⏸", "ic_pause");
        MAP.put("⏸️", "ic_pause");
        MAP.put("⏹", "ic_stop");
        MAP.put("⏹️", "ic_stop");
        MAP.put("✓", "ic_checkbold");
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
