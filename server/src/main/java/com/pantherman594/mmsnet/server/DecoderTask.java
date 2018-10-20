package com.pantherman594.mmsnet.server;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;

public class DecoderTask extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] objects) {
        Bitmap image = (Bitmap) objects[0];

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        image.recycle();

        String data = new String(bytes);



        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}
