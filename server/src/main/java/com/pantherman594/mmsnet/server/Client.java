package com.pantherman594.mmsnet.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private static Map<String, Client> clients = new HashMap<>();

    public static Client getClient(String clientNumber) {
        if (!clients.containsKey(clientNumber)) {
            Client newClient = new Client(clientNumber);
            clients.put(clientNumber, newClient);
        }

        return clients.get(clientNumber);
    }

    private WebView webview;
    private String clientNumber;

    @SuppressLint("SetJavaScriptEnabled")
    public Client(String clientNumber) {
        this.clientNumber = clientNumber;
        webview = ServerApplication.createWebview();
        webview.getSettings().setJavaScriptEnabled(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);
    }

    public String getNumber() {
        return clientNumber;
    }

    @SuppressLint("StaticFieldLeak")
    public void processImage(final Bitmap image) {
        final String receiverNumber = this.getNumber();
        final Context context = ServerApplication.getInstance().getApplicationContext();

        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... strings) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                image.recycle();

                final String prefix = ServerApplication.getInstance().getApplicationContext().getString(R.string.prefix);

                String data = new String(bytes);
                if (!data.startsWith(prefix)) return null;

                data = data.substring(prefix.length());

                try {
                    Packet packet = new Packet(data);

                    Packet.PacketCallback callback = new Packet.PacketCallback() {
                        @Override
                        public void run(String result) {
                            if (result == null || result.equals("")) return;
                            // Set the prefix for the response
                            result = prefix + result;

                            byte[] responseBytes = result.getBytes();

                            Bitmap bmp = BitmapFactory.decodeByteArray(responseBytes, 0, responseBytes.length);

                            MMSManager.sendMmsImage(context, receiverNumber, bmp);
                        }
                    };

                    new Packet.PacketTask(callback).execute(packet);
                } catch (IOException ignored) {}

                return null;
            }
        }.execute("");

    }
}
