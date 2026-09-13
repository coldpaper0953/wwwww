package com.weng.weng;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return false;
            }
        });
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
    }

    class Bridge {
        @JavascriptInterface
        public void apiPost(final String path, final String jsonBody, final String cb) {
            new Thread(() -> {
                String result;
                boolean ok = false;
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL("https://api.tokenrouter.com" + path).openConnection();
                    c.setRequestMethod("POST");
                    c.setRequestProperty("Content-Type", "application/json");
                    c.setRequestProperty("Authorization", "Bearer sk-mRrMtL6KmIvcTmq4tJvM67KGLij62xcfJfn0DuIeYhP4bD8b");
                    c.setDoOutput(true);
                    c.setConnectTimeout(20000);
                    c.setReadTimeout(90000);
                    c.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    int code = c.getResponseCode();
                    InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
                    byte[] buf = new byte[65536];
                    StringBuilder sb = new StringBuilder();
                    int n;
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                    while (is != null && (n = is.read(buf)) > 0) bos.write(buf, 0, n);
                    if (is != null) is.close();
                    result = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                    ok = code >= 200 && code < 300;
                } catch (Exception e) {
                    result = e.getMessage() == null ? "network error" : e.getMessage();
                }
                final boolean fok = ok;
                final String fresult = result;
                web.post(() -> {
                    String js = "window['" + cb + "'](" + fok + "," + JSONObject.quote(fresult) + ")";
                    web.evaluateJavascript(js, null);
                });
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
        // 不退出，桌面宠物业没有"返回"概念；回桌面即可
        moveTaskToBack(true);
    }
}
