package com.pantherman594.mmsnet.server;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony;

import java.io.IOException;
import java.io.InputStream;

public class ClientTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
        while (!isCancelled()) {
            ContentResolver resolver = (ContentResolver) objects[0];
            final String[] projection = new String[]{"_id", "ct_t"};

            Cursor query = resolver.query(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, projection, null, null, null);

            if (query.moveToFirst()) {
                do {
                    String contentType = query.getString(query.getColumnIndex(Telephony.Mms.CONTENT_TYPE));

                    if ("application/vnd.wap.multipart.related".equals(contentType)) { // It's an MMS
                        String mmsId = query.getString(query.getColumnIndex(Telephony.Mms._ID));
                        String selectionPart = "mid=" + mmsId;

                        String senderNumber = getAddressNumber(resolver, mmsId);

                        Client client = Client.getClient(senderNumber);

                        Uri mmsUri = Uri.parse("content://mms/part");
                        Cursor cPart = resolver.query(mmsUri, null, selectionPart, null, null);

                        if (cPart.moveToFirst()) {
                            do {
                                String partId = cPart.getString(cPart.getColumnIndex("_id"));
                                String type = cPart.getString(cPart.getColumnIndex("ct"));
                                if ("image/jpeg".equals(type)) {
                                    Bitmap bitmap = getMmsImage(resolver, partId);

                                    client.processImage(bitmap);

                                }
                            } while (cPart.moveToNext());
                        }
                    }
                } while (query.moveToNext());
            }

            query.close();
        }

        return null;
    }


    private Bitmap getMmsImage(ContentResolver resolver, String _id) {
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = resolver.openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException ignored) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
        }
        return bitmap;
    }


    private String getAddressNumber(ContentResolver resolver, String id) {
        String selectionAdd = "msg_id=" + id;
        String uriStr = String.format("content://mms/%s/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = resolver.query(uriAddress, null,
            selectionAdd, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    try {
                        Long.parseLong(number.replace("-", ""));
                        name = number;
                    } catch (NumberFormatException nfe) {
                        if (name == null) {
                            name = number;
                        }
                    }
                }
            } while (cAdd.moveToNext());
        }
        cAdd.close();
        return name;
    }
}
