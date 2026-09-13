package com.weng.weng;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 记忆本：原始记忆（情景|回复）自动追加；攒到阈值（默认 150 条，可调）后
 * 用副 API（用户配置的第二套接口，未配置则用主 API）把原始记忆压缩成"整理后的长期记忆"。
 * 整理后的记忆注入 AI 提示词，原始记忆清空重新攒。
 */
public class Memory {

    // ================= 原始记忆（rawLog: [{t, ev, reply}]） =================

    public static synchronized List<JSONObject> raw() {
        return DataStore.arr("memRaw");
    }

    /** AI 回复成功后自动记录 */
    public static synchronized void append(String event, String reply) {
        try {
            List<JSONObject> l = raw();
            JSONObject o = new JSONObject();
            o.put("t", new SimpleDateFormat("MM-dd HH:mm", Locale.US).format(new Date()));
            o.put("ev", event == null ? "" : event);
            o.put("reply", reply == null ? "" : reply);
            l.add(o);
            DataStore.saveArr("memRaw", l);
        } catch (Exception ignored) {
        }
    }

    public static int rawCount() {
        return raw().size();
    }

    /** 自动整理阈值（默认 150，可调 30-1000） */
    public static int threshold() {
        return DataStore.getInt("memThreshold", 150);
    }

    public static void setThreshold(int n) {
        DataStore.putInt("memThreshold", Math.max(30, Math.min(1000, n)));
    }

    /** 整理后保留条数（默认 12 条长期记忆，可调 3-50） */
    public static int keep() {
        return DataStore.getInt("memKeep", 12);
    }

    public static void setKeep(int n) {
        DataStore.putInt("memKeep", Math.max(3, Math.min(50, n)));
    }

    // ================= 整理后的长期记忆（memLong: [字符串]） =================

    public static synchronized List<String> longTerm() {
        List<String> r = new ArrayList<String>();
        try {
            org.json.JSONArray a = new org.json.JSONArray(DataStore.sp().getString("memLong", "[]"));
            for (int i = 0; i < a.length(); i++) r.add(a.getString(i));
        } catch (Exception ignored) {
        }
        return r;
    }

    /** 整理结果落库（替换长期记忆，清空原始记忆） */
    public static synchronized void applyDigest(String digestText) {
        List<String> longs = new ArrayList<String>();
        // 整理结果按行拆成条目
        for (String s : digestText.split("\n")) {
            String t = s.trim();
            if (t.isEmpty()) continue;
            // 去常见编号前缀
            t = t.replaceFirst("^\\d+[\\.、)]\\s*", "").replaceFirst("^[-•*]\\s*", "");
            if (t.length() > 2) longs.add(t);
        }
        while (longs.size() > keep()) longs.remove(0);
        org.json.JSONArray a = new org.json.JSONArray();
        for (String s : longs) a.put(s);
        DataStore.sp().edit().putString("memLong", a.toString())
                .putString("memRaw", "[]").apply();
    }

    public static void clearAll() {
        DataStore.sp().edit().putString("memRaw", "[]").putString("memLong", "[]").apply();
    }

    /** 注入 AI 提示词的长期记忆块 */
    public static String promptBlock() {
        List<String> l = longTerm();
        if (l.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("\n【长期记忆】\n");
        for (String s : l) sb.append("· ").append(s).append('\n');
        return sb.toString();
    }

    // ================= 用户身份提示词 =================

    public static String userPersona() {
        return DataStore.sp().getString("userPersona", "");
    }

    public static void setUserPersona(String s) {
        DataStore.sp().edit().putString("userPersona", s == null ? "" : s.trim()).apply();
    }

    // ================= 副 API（整理专用；未配置则用主 API） =================

    public static String subBase() { return DataStore.sp().getString("subBase", ""); }
    public static String subKey() { return DataStore.sp().getString("subKey", ""); }
    public static String subModel() { return DataStore.sp().getString("subModel", ""); }

    public static void setSub(String base, String key, String model) {
        DataStore.sp().edit()
                .putString("subBase", base == null ? "" : base.trim())
                .putString("subKey", key == null ? "" : key.trim())
                .putString("subModel", model == null ? "" : model.trim())
                .apply();
    }

    /** 是否达到整理条件 */
    public static boolean shouldDigest() {
        return rawCount() >= threshold();
    }

    public interface DigestCb {
        void onResult(boolean ok, String digest, String error);
    }

    /**
     * 用副 API 整理原始记忆：把全部原始记忆发给一个总结模型，
     * 产出"用户画像 + 关键事实"条目。成功后 applyDigest()。
     * subBase/Key/Model 任一为空 → 用 PetService 主 API。
     */
    public static void digest(PetService pet, DigestCb cb) {
        List<JSONObject> raw = raw();
        if (raw.isEmpty()) {
            cb.onResult(false, null, "还没有原始记忆");
            return;
        }
        final String base = subBase().isEmpty() ? pet.apiBase : subBase();
        final String key = subKey().isEmpty() ? pet.apiKey : subKey();
        final String model = subModel().isEmpty() ? pet.apiModel : subModel();
        new Thread(() -> {
            String err = null, digest = null;
            try {
                StringBuilder sb = new StringBuilder();
                for (JSONObject o : raw) {
                    sb.append('[').append(o.optString("t", "")).append("] 情景：")
                            .append(o.optString("ev", "")).append(" 回复：")
                            .append(o.optString("reply", "")).append('\n');
                }
                String sys = "你是记忆整理器。把一段宠物蚊子与用户的互动流水整理成不超过 " + keep()
                        + " 条长期记忆条目，每条一行，格式：用户[事实/喜好/重要事件]。只输出条目，不要编号不要客套。";
                JSONObject body = new JSONObject();
                body.put("model", model);
                body.put("temperature", 0.3);
                body.put("messages", new org.json.JSONArray()
                        .put(new JSONObject().put("role", "system").put("content", sys))
                        .put(new JSONObject().put("role", "user").put("content", sb.toString())));
                HttpURLConnection c = (HttpURLConnection) new URL(PetService.normalizeEndpointStatic(base)).openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("Authorization", "Bearer " + key);
                c.setDoOutput(true);
                c.setConnectTimeout(20000);
                c.setReadTimeout(90000);
                c.getOutputStream().write(body.toString().getBytes(StandardCharsets.UTF_8));
                InputStream is = c.getResponseCode() < 400 ? c.getInputStream() : c.getErrorStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[65536];
                int n;
                while (is != null && (n = is.read(buf)) > 0) bos.write(buf, 0, n);
                if (is != null) is.close();
                if (c.getResponseCode() == 200) {
                    JSONObject j = new JSONObject(bos.toString("UTF-8"));
                    digest = j.getJSONArray("choices").getJSONObject(0)
                            .getJSONObject("message").getString("content").trim();
                    if (digest.isEmpty()) err = "整理结果为空";
                } else {
                    err = "HTTP " + c.getResponseCode();
                }
            } catch (Exception e) {
                err = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            }
            final String fD = digest, fE = err;
            if (fD != null) applyDigest(fD);
            cb.onResult(fD != null, fD, fE);
        }).start();
    }
}
