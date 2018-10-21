package com.pantherman594.mmsnet.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Server {
    private String clientNumber;

    public Server(String serverAddress) {
        this.clientNumber = clientNumber;
    }

    public String getNumber() {
        return clientNumber;
    }

    @SuppressLint("StaticFieldLeak")
    public void processImage(final Bitmap image) {
        final String receiverNumber = this.getNumber();
        final Context context = ClientApplication.getInstance().getApplicationContext();

        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... strings) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                image.recycle();

                final String prefix = "";//ClientApplication.getInstance().getApplicationContext().getString(R.string.prefix);

                String data = new String(bytes);
                if (!data.startsWith(prefix)) return null;

                data = data.substring(prefix.length());

                MmsTunnel.getInstance().getOutgoingQueue().push(data);
                return null;
            }
        }.execute("");

    }
}
