package com.pantherman594.mmsnet.server;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Packet {
    private static Set<String> methods = new HashSet<>();

    static {
        for (Method method : Method.values()) {
            methods.add(method.toString());
        }
    }

    private URL url;
    private String method;
    private Map<String, String> headers;

    public Packet(String rawRequest) throws IOException {
        String[] data = rawRequest.split("\n");

        String[] head = data[0].split(" ");

        String method = head[0].toUpperCase();
        if (methods.contains(method)) this.method = method;
        String url = head[1];
        String prefix = "http://";

        if (head.length > 2) {
            prefix = head[2].split("/")[0].toLowerCase() + "://";
        }

        this.headers = new HashMap<>();
        for (int i = 1, len = data.length; i < len; i++) {
            String line = data[i];
            String[] lineData = line.split(":", 2);

            String key = lineData[0];
            String value = lineData[1];

            if (key.equalsIgnoreCase("Host")) {
                this.url = new URL(prefix + value + url);
            } else {
                headers.put(key, value);
            }
        }
    }

    public URL getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    static class PacketTask extends AsyncTask<Packet, String, String> {
        private PacketCallback callback;

        PacketTask(PacketCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Packet... packets) {
            Packet packet = packets[0];
            StringBuilder result = new StringBuilder();

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) packet.getUrl().openConnection();
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod(packet.getMethod());

                for (String key : packet.getHeaders().keySet()) {
                    connection.setRequestProperty(key, packet.getHeaders().get(key));
                }

                connection.connect();

                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.run(s);
        }
    }

    interface PacketCallback {
        void run(String result);
    }

    enum Method {
        GET,
        POST,
        HEAD,
        OPTIONS,
        PUT,
        DELETE,
        TRACE
    }

}
