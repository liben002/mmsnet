package com.pantherman594.mmsnet.client;

import android.app.PendingIntent;
import android.net.VpnService;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class VpnConnection implements Runnable {

    public interface OnEstablishListener {
        void onEstablish(ParcelFileDescriptor tunInterface);
    }
    /** Maximum packet size is constrained by the MTU, which is given as a signed short. */
    private static final int MAX_PACKET_SIZE = Short.MAX_VALUE;
    /** Time to wait in between losing the connection and retrying. */
    private static final long RECONNECT_WAIT_MS = TimeUnit.SECONDS.toMillis(3);
    /** Time between keepalives if there is no traffic at the moment.
     *
     * TODO: don't do this; it's much better to let the connection die and then reconnect when
     *       necessary instead of keeping the network hardware up for hours on end in between.
     **/
    private static final long KEEPALIVE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15);
    /** Time to wait without receiving any response before assuming the server is gone. */
    private static final long RECEIVE_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(20);
    /**
     * Time between polling the VPN interface for new traffic, since it's non-blocking.
     *
     * TODO: really don't do this; a blocking read on another thread is much cleaner.
     */
    private static final long IDLE_INTERVAL_MS = TimeUnit.MILLISECONDS.toMillis(100);
    /**
     * Number of periods of length {@IDLE_INTERVAL_MS} to wait before declaring the handshake a
     * complete and abject failure.
     *
     * TODO: use a higher-level protocol; hand-rolling is a fun but pointless exercise.
     */
    private static final int MAX_HANDSHAKE_ATTEMPTS = 50;
    private final VpnService mService;
    private final String serverAddress;

    private PendingIntent mConfigureIntent;
    private OnEstablishListener mOnEstablishListener;

    private CommunicateTask communicateTask;

    public VpnConnection(final VpnService service, final String serverAddress) {
        this.mService = service;
        this.serverAddress = serverAddress;
    }
    /**
     * Optionally, set an intent to configure the VPN. This is {@code null} by default.
     */
    public void setConfigureIntent(PendingIntent intent) {
        mConfigureIntent = intent;
    }
    public void setOnEstablishListener(OnEstablishListener listener) {
        mOnEstablishListener = listener;
    }
    @Override
    public void run() {
        try {
            run(serverAddress);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        //try {
        //    Log.i(getTag(), "Starting");
            // If anything needs to be obtained using the network, get it now.
            // This greatly reduces the complexity of seamless handover, which
            // tries to recreate the tunnel without shutting down everything.
            // In this demo, all we need to know is the server address.
            // We try to create the tunnel several times.
            // TODO: The better way is to work with ConnectivityManager, trying only when the
            //       network is available.
            // Here we just use a counter to keep things simple.
            //for (int attempt = 0; attempt < 10; ++attempt) {
            //    // Reset the counter if we were connected.
            //    if (run(serverAddress)) {
            //        attempt = 0;
            //    }
            //    // Sleep for a while. This also checks if we got interrupted.
            //    Thread.sleep(3000);
            //}
            //Log.i(getTag(), "Giving up");
        //} catch (IOException | InterruptedException | IllegalArgumentException e) {
        //    Log.e(getTag(), "Connection failed, exiting", e);
        //}
    }
    private void run(String server)
            throws IOException, InterruptedException, IllegalArgumentException {
        ParcelFileDescriptor iface = null;

        MmsTunnel tunnel = new MmsTunnel(server);
        communicateTask = ClientApplication.getInstance().createCommunicateTask(server);

        // Create a DatagramChannel as the VPN tunnel.
        //try (DatagramChannel tunnel2 = DatagramChannel.open()) {
            // Protect the tunnel before connecting to avoid loopback.
            //if (!mService.protect(tunnel.socket())) {
            //    throw new IllegalStateException("Cannot protect the tunnel");
            //}
            // Connect to the server.
            //tunnel.connect(server);
            // For simplicity, we use the same thread for both reading and
            // writing. Here we put the tunnel into non-blocking mode.
            //tunnel.configureBlocking(false);
            // Authenticate and configure the virtual network interface.
            iface = configure();
            // Now we are connected. Set the flag.
            // Packets to be sent are queued in this input stream.
            FileInputStream in = new FileInputStream(iface.getFileDescriptor());
            // Packets received need to be written to this output stream.
            FileOutputStream out = new FileOutputStream(iface.getFileDescriptor());
            // Allocate the buffer for a single packet.
            ByteBuffer packet = ByteBuffer.allocate(MAX_PACKET_SIZE);
            // Timeouts:
            //   - when data has not been sent in a while, send empty keepalive messages.
            //   - when data has not been received in a while, assume the connection is broken.
            long lastSendTime = System.currentTimeMillis();
            long lastReceiveTime = System.currentTimeMillis();
            // We keep forwarding packets till something goes wrong.
            while (!((MmsVpnService) mService).isCancelled()) {
                // Assume that we did not make any progress in this iteration.
                Log.i("BBBBB", "RUN");
                boolean idle = true;
                // Read the outgoing packet from the input stream.
                int length = in.read(packet.array());
                if (length > 0) {
                    // Write the outgoing packet to the tunnel.
                    packet.limit(length);
                    Log.i("BBBBB", "WRITE");
                    tunnel.write(packet);
                    packet.clear();
                    // There might be more outgoing packets.
                    idle = false;
                    lastReceiveTime = System.currentTimeMillis();
                }
                // Read the incoming packet from the tunnel.
                Log.i("BBBBB", "READ");
                length = tunnel.read(packet);
                Log.i("BBBBB", "len " + length);
                if (length > 0) {
                    // Ignore control messages, which start with zero.
                    if (packet.get(0) != 0) {
                        // Write the incoming packet to the output stream.
                        out.write(packet.array(), 0, length);
                    }
                    packet.clear();
                    // There might be more incoming packets.
                    idle = false;
                    lastSendTime = System.currentTimeMillis();
                }
                // If we are idle or waiting for the network, sleep for a
                // fraction of time to avoid busy looping.
                if (idle) {
                    Thread.sleep(IDLE_INTERVAL_MS);
                    final long timeNow = System.currentTimeMillis();
                    if (lastSendTime + KEEPALIVE_INTERVAL_MS <= timeNow) {
                        // We are receiving for a long time but not sending.
                        // Send empty control messages.
                        packet.put((byte) 0).limit(1);
                        for (int i = 0; i < 3; ++i) {
                            packet.position(0);
                            tunnel.write(packet);
                        }
                        packet.clear();
                        lastSendTime = timeNow;
                    } else if (lastReceiveTime + RECEIVE_TIMEOUT_MS <= timeNow) {
                        // We are sending for a long time but not receiving.
                        throw new IllegalStateException("Timed out");
                    }
                }
            }
        //} catch (SocketException e) {
        //    Log.e(getTag(), "Cannot use socket", e);
        //} finally {
                iface.close();
            communicateTask.cancel(false);
        //}
    }

    private ParcelFileDescriptor configure() throws IllegalArgumentException {
        // Configure a builder while parsing the parameters.
        VpnService.Builder builder = mService.new Builder();
        builder.addRoute("0.0.0.0", 0);
        builder.addAddress("10.0.0.2", 24);
        // Create a new interface using the builder and save the parameters.
        final ParcelFileDescriptor vpnInterface;
        synchronized (mService) {
            vpnInterface = builder
                    .setSession(serverAddress)
                    .setConfigureIntent(mConfigureIntent)
                    .establish();
            if (mOnEstablishListener != null) {
                mOnEstablishListener.onEstablish(vpnInterface);
            }
        }
        Log.i(getTag(), "New interface: " + vpnInterface);
        return vpnInterface;
    }

    private String getTag() {
        return "SSSSSS";
    }
}
