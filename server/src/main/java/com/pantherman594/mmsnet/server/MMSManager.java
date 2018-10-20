package com.pantherman594.mmsnet.server;

import android.content.Context;
import android.graphics.Bitmap;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

public class MMSManager {
    private static Settings settings;

    static {
        settings = new Settings();
        settings.setUseSystemSending(true);
    }

    public static void sendMmsImage(Context context, String receiverNumber, Bitmap image) {
        Transaction transaction = new Transaction(context, settings);
        Message message = new Message("", receiverNumber);
        message.setImage(image);

        transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
    }

}
