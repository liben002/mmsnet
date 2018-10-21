package com.pantherman594.mmsnet.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.pantherman594.mmsnet.server.localvpn.Packet;

public class Client {
    private static Map<String, Client> clients = new HashMap<>();

    public static Client getClient(String clientNumber) {
        if (!clients.containsKey(clientNumber)) {
            Client newClient = new Client(clientNumber);
            clients.put(clientNumber, newClient);
        }

        return clients.get(clientNumber);
    }

    private String clientNumber;

    private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
    private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;

    private Selector tcpSelector;

    private TCPOutput tcpOutput;

    public Client(String clientNumber) {
        this.clientNumber = clientNumber;

        deviceToNetworkTCPQueue = new ConcurrentLinkedQueue<>();
        networkToDeviceQueue = new ConcurrentLinkedQueue<>();

        try {
            tcpSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tcpOutput = new TCPOutput(deviceToNetworkTCPQueue, networkToDeviceQueue, tcpSelector, clientNumber);
        tcpOutput.run();
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
                int[] imageArray = new int[image.getWidth() * image.getHeight()];
                image.getPixels(imageArray, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                int size = imageArray[0];
                for (int y = 0, h = image.getHeight(); y < h; y++) {
                    for (int x = 0, w = image.getWidth(); x < w; x++) {
                        if (y == 0 && x == 0) continue;
                        if ((y * w) + x >= size) break;

                        byte[] length = ByteBuffer.allocate(4).putInt(imageArray[(y * w) + x]).array();
                        for (int i = 0; i < 4; i++) {
                            stream.write(length[i]);
                        }
                    }
                }

                byte[] bytes = stream.toByteArray();
                image.recycle();

                ByteBuffer buf = ByteBuffer.wrap(bytes);
                try {
                    Packet packet = new Packet(buf);
                    deviceToNetworkTCPQueue.add(packet);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }


                //final String prefix = "";//ServerApplication.getInstance().getApplicationContext().getString(R.string.prefix);

                //String data = new String(bytes);
                //if (!data.startsWith(prefix)) return null;

                //data = data.substring(prefix.length());

                //try {
                //    Packet packet = new Packet(data);

                //    Packet.PacketCallback callback = new Packet.PacketCallback() {
                //        @Override
                //        public void run(String result) {
                //            if (result == null || result.equals("")) return;
                //            // Set the prefix for the response
                //            result = prefix + result;

                //            byte[] responseBytes = result.getBytes();

                //            Bitmap bmp = BitmapFactory.decodeByteArray(responseBytes, 0, responseBytes.length);

                //            MMSManager.sendMmsImage(context, receiverNumber, bmp);
                //        }
                //    };

                //    new Packet.PacketTask(callback).execute(packet);
                //} catch (IOException ignored) {}

                return null;
            }
        }.execute("");

    }
}
