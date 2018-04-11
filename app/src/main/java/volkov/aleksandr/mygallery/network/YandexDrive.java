package volkov.aleksandr.mygallery.network;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import volkov.aleksandr.mygallery.model.ImageResource;

import static java.lang.Math.abs;
import static java.lang.Math.tan;
import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;

/**
 * Created by alexa on 08.04.2018.
 */

public class YandexDrive {
    private static final String LOG_TAG = makeLogTag(YandexDrive.class);

    public static final String FOLDER_URL = "https://yadi.sk/d/L-oc0Bqa3UD4oL";

    private static final String PUBLIC_RESOURCE_URL =
            "cloud-api.yandex.net/v1/disk/public/resources";
    private static final String DOWNLOAD_RESOURCE_URL = PUBLIC_RESOURCE_URL + "/download";

    private static RequestQueue queue;
    private static LruCache<Uri, JSONObject> jsonCache;

    public YandexDrive(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        if (jsonCache == null) {
            int cacheSize = 4 * 1024 * 1024; // 4MiB
            jsonCache = new LruCache<>(cacheSize);
        }
    }

    public void getPublicFolder(final String publicKey, int limit, int previewSize,
                                final ResponseListener<List<ImageResource>> listener) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .encodedAuthority(PUBLIC_RESOURCE_URL)
                .appendQueryParameter("public_key", encodeUrl(publicKey))
                .appendQueryParameter("limit", String.valueOf(limit))
                .appendQueryParameter("preview_size", getPreviewSize(previewSize))
                .appendQueryParameter("preview_crop", "true")
                .appendQueryParameter("sort", "media_type=image");

        Uri uri = builder.build();
        addRequest(Request.Method.GET, uri, ParsingUtils::parseImageResourceList, listener, false);
        Log.i(LOG_TAG, "request for downloading folder info " + publicKey);
    }

    public void getDownloadLink(final String publicUrl, final ResponseListener<String> listener) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .encodedAuthority(DOWNLOAD_RESOURCE_URL)
                .appendQueryParameter("public_key", encodeUrl(publicUrl));

        Uri uri = builder.build();
        addRequest(Request.Method.GET, uri, ParsingUtils::parseDownloadUrl, listener, true);
        Log.i(LOG_TAG, "request for downloading file " + publicUrl);
    }

    private <T> void addRequest(int method, Uri uri, JsonParser<T> parser,
                                ResponseListener<T> listener, boolean needCache) {
        JSONObject cached = jsonCache.get(uri);
        if (cached != null) {
            try {
                listener.onResponse(parser.parse(cached));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "error occurred while parsing json for uri " + uri);
            }
            return;
        }
        WeakReference<ResponseListener<T>> listenerReference = new WeakReference<>(listener);
        JsonObjectRequest request = new JsonObjectRequest(method, uri.toString(),
                null,
                response -> {
                    try {
                        if (jsonCache != null && needCache) {
                            jsonCache.put(uri, response);
                        }
                        ResponseListener<T> responseListener = listenerReference.get();
                        if (responseListener != null) {
                            responseListener.onResponse(parser.parse(response));
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "error occurred while parsing json for uri " + uri);
                    }
                }
                , listener);
        queue.add(request);
    }

    private static String encodeUrl(String url) {
        String encodeText = url;
        try {
            encodeText = URLEncoder.encode(url, "UTF8");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return encodeText;
    }

    private static List<Pair<Integer,String>> quality;
    static {
        quality = new ArrayList<>();
        quality.add(Pair.create(150, "S"));
        quality.add(Pair.create(300, "M"));
        quality.add(Pair.create(500, "L"));
        quality.add(Pair.create(800,"XL"));
        quality.add(Pair.create(1024,"XXL"));
        quality.add(Pair.create(1280,"XXXL"));
    }

    private static String getPreviewSize(int previewSize) {
        Pair<Integer, String> q = Collections.min(quality,
                (o1, o2) -> abs(o1.first - previewSize) - abs(o2.first - previewSize));
        int idx = quality.indexOf(q);
        if (idx == 0) {
            return q.second;
        } else {
            return quality.get(idx - 1).second;
        }
    }

    private interface JsonParser<T> {
        T parse(JSONObject json) throws JSONException;
    }
}
