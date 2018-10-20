package com.pantherman594.mmsnet.server;

import android.app.Application;
import android.webkit.WebView;

public class ServerApplication extends Application {
    private static ServerApplication instance;

    public ServerApplication() {
        super();
        instance = this;
    }

    public static ServerApplication getInstance() {
        return instance;
    }

    public static WebView createWebview() {
        return new WebView(instance);
    }
}
