package com.pantherman594.mmsnet.client;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MmsVpnService extends VpnService implements Handler.Callback {

private static final String TAG = MmsVpnService.class.getSimpleName();
    public static final String ACTION_CONNECT = "com.pantherman594.mmsnet.client.START";
    public static final String ACTION_DISCONNECT = "com.pantherman594.mmsnet.client.STOP";
    private static class Connection extends Pair<Thread, ParcelFileDescriptor> {
        public Connection(Thread thread, ParcelFileDescriptor pfd) {
            super(thread, pfd);
        }
    }
    private final AtomicReference<Thread> mConnectingThread = new AtomicReference<>();
    private final AtomicReference<Connection> mConnection = new AtomicReference<>();
    private AtomicInteger mNextConnectionId = new AtomicInteger(1);
    private PendingIntent mConfigureIntent;

    private String receiverNumber;

    private boolean isCancelled = false;

    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void onCreate() {
        // The handler is only used to show messages.
        // Create the intent to "configure" the connection (just start ToyVpnClient).
        mConfigureIntent = PendingIntent.getActivity(this, 0, new Intent(this, MmsVpnService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            receiverNumber = intent.getStringExtra("number");
        }
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            disconnect();
            return START_NOT_STICKY;
        } else {
            connect();
            return START_STICKY;
        }
    }
    @Override
    public void onDestroy() {
        disconnect();
    }

    @Override
    public boolean handleMessage(Message message) {
        return true;
    }

    private void connect() {
        // Kick off a connection.
        startConnection(new VpnConnection(
                this, receiverNumber));
    }
    private void startConnection(final VpnConnection connection) {
        // Replace any existing connecting thread with the  new one.
        final Thread thread = new Thread(connection, "MmsVpnThread");
        setConnectingThread(thread);
        // Handler to mark as connected once onEstablish is called.
        connection.setConfigureIntent(mConfigureIntent);
        connection.setOnEstablishListener(new VpnConnection.OnEstablishListener() {
            public void onEstablish(ParcelFileDescriptor tunInterface) {
                mConnectingThread.compareAndSet(thread, null);
                setConnection(new Connection(thread, tunInterface));
            }
        });
        thread.start();
    }
    private void setConnectingThread(final Thread thread) {
        final Thread oldThread = mConnectingThread.getAndSet(thread);
        if (oldThread != null) {
            oldThread.interrupt();
        }
    }
    private void setConnection(final Connection connection) {
        final Connection oldConnection = mConnection.getAndSet(connection);
        if (oldConnection != null) {
            try {
                oldConnection.first.interrupt();
                oldConnection.second.close();
            } catch (IOException e) {
                Log.e(TAG, "Closing VPN interface", e);
            }
        }
    }
    private void disconnect() {
        setConnectingThread(null);
        setConnection(null);
        stopForeground(true);
        isCancelled = true;
    }
}