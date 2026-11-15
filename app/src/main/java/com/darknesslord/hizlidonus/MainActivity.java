package com.darknesslord.hizlidonus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {

    private static final String HOME = "https://appassets.androidplatform.net/assets/index.html";
    private WebView web;
    private WebViewAssetLoader assetLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#0c1013"));
        getWindow().setNavigationBarColor(Color.parseColor("#0c1013"));

        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0c1013"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setSupportMultipleWindows(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("appassets.androidplatform.net".equals(uri.getHost())) return false;
                openExternal(uri);
                return true;
            }
        });

        setContentView(web);
        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(HOME);
    }

    private void openExternal(Uri uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        Intent intent;
        if (scheme.equals("sms") || scheme.equals("smsto")) {
            // sms:+905321234567?body=... (opak URI, getQueryParameter kullanılamaz)
            String ssp = uri.getEncodedSchemeSpecificPart();
            if (ssp == null) ssp = "";
            String number = ssp;
            String body = null;
            int q = ssp.indexOf('?');
            if (q >= 0) {
                number = ssp.substring(0, q);
                for (String part : ssp.substring(q + 1).split("&")) {
                    if (part.startsWith("body=")) body = Uri.decode(part.substring(5));
                }
            }
            intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.decode(number)));
            if (body != null && !body.isEmpty()) intent.putExtra("sms_body", body);
        } else {
            intent = new Intent(Intent.ACTION_VIEW, uri);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            try {
                if (scheme.startsWith("sms")) {
                    Intent fallback = new Intent(Intent.ACTION_VIEW, uri);
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(fallback);
                    return;
                }
            } catch (Exception ignored) { }
            Toast.makeText(this, "Bu bağlantıyı açacak uygulama bulunamadı", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
