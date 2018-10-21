package com.pantherman594.mmsnet.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.File;

public class MMSManager {
    private static Settings settings;
    private static boolean go = true;

    static {
        settings = new Settings();
        settings.setUseSystemSending(true);
    }

    public static void sendMmsImage(Context context, String receiverNumber, Bitmap image) {
        File imgFile = new File("/sdcard/Download/mmsn.png");
        if (imgFile.exists()){
            image = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        Log.i("SSSSSSEND", "r " + receiverNumber + " i " + image);
        Transaction transaction = new Transaction(context, settings);
        Message message = new Message("", receiverNumber);
        message.setImage(image);

        transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
    }

}
