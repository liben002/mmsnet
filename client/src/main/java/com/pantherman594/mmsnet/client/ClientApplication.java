package com.pantherman594.mmsnet.client;

import android.app.Application;

public class ClientApplication extends Application {
    private static ClientApplication instance;

    public ClientApplication() {
        super();
        instance = this;
    }

    public static ClientApplication getInstance() {
        return instance;
    }

    public CommunicateTask createCommunicateTask(String serverAddress) {
        return new CommunicateTask(serverAddress);
    }
}
