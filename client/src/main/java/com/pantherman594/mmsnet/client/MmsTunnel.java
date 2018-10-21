package com.pantherman594.mmsnet.client;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

public class MmsTunnel {
    private static MmsTunnel instance;
    private String phoneNumber;
    private Server server;
    private Deque<String> outgoingQueue = new ArrayDeque<>();

    public MmsTunnel(String phoneNumber) {
        instance = this;
        this.phoneNumber = phoneNumber;
        this.server = new Server(phoneNumber);
    }

    public static MmsTunnel getInstance() {
        return instance;
    }

    public Deque<String> getOutgoingQueue() {
        return outgoingQueue;
    }

    public int write(ByteBuffer src) {
        byte[] data = src.array();
        int w = (int) Math.round(Math.sqrt(data.length / 4d + 1));
        int h = (int) Math.ceil((data.length / 4d + 1) / w);

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        //Bitmap bmp = BitmapFactory.decodeByteArray(src.array(), 0, src.array().length);
        Canvas c = new Canvas(bmp);
        for (int i = 0, len = data.length; i < len / 4d + 1; i++) {
            Paint paint = new Paint();

            if (i == 0) {
                byte[] length = ByteBuffer.allocate(4).putInt(len).array();
                paint.setARGB(length[0], length[1], length[2], length[3]);
                c.drawPoint(0, 0, paint);
            } else {
                int j = i - 1;
                int a = j * 4 < len ? data[j * 4] : 0;
                int r = j * 4 + 1 < len ? data[j * 4 + 1] : 0;
                int g = j * 4 + 2 < len ? data[j * 4 + 2] : 0;
                int b = j * 4 + 3 < len ? data[j * 4 + 3] : 0;
                paint.setARGB(a, r, g, b);
                c.drawPoint(i % w, i / h, paint);
            }
        }
        c.save();
        Log.i("YYYYYYYYY", bmp.toString());

        //MMSManager.sendMmsImage(ClientApplication.getInstance().getApplicationContext(), phoneNumber, bmp);
        MMSManager.sendMmsImage(ClientApplication.getInstance().getApplicationContext(), phoneNumber, Base64.encodeToString(data, Base64.DEFAULT));
        return src.array().length;
    }

    public int read(ByteBuffer dest) {
        Log.i("XXXXXXX", new String(dest.array()));

        String next;
        if (!outgoingQueue.isEmpty()) {
            next = outgoingQueue.pop();
            Log.i("XXXXXXXX>>>", next);
            dest.put(next.getBytes());
        } else {
            return -1;
        }

        Log.i("XXXXXXX2", new String(dest.array()));

        return next.getBytes().length;
    }
}
