package com.pantherman594.mmsnet.server;

import android.app.Application;
import android.webkit.WebView;

public class ServerApplication extends Application {
    private static ServerApplication instance;
    private ClientTask clientTask;

    public ServerApplication() {
        super();
        instance = this;

        clientTask = new ClientTask();
    }

    public static ServerApplication getInstance() {
        return instance;
    }

    public ClientTask getClientTask() {
        return clientTask;
    }

    public static WebView createWebview() {
        return new WebView(instance);
    }
}
