package volkov.aleksandr.mygallery.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.List;

import volkov.aleksandr.mygallery.model.ImageResource;

import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;

/**
 * Created by alexa on 08.04.2018.
 */

public class Disk {
    private static final String LOG_TAG = makeLogTag(Disk.class);

    public static final String CATS_URL = "https://yadi.sk/d/L-oc0Bqa3UD4oL";

    private static final String PUBLIC_RESOURCE_URL =
            "cloud-api.yandex.net/v1/disk/public/resources";
    private static final String DOWNLOAD_RESOURCE_URL = PUBLIC_RESOURCE_URL + "/download";

    private static RequestQueue queue;

    public Disk(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public void getPublicFolder(final String publicKey, int limit,
                                final ResponseListener<List<ImageResource>> listener) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .encodedAuthority(PUBLIC_RESOURCE_URL)
                .appendQueryParameter("public_key", encodeUrl(publicKey))
                .appendQueryParameter("limit", String.valueOf(limit))
                .appendQueryParameter("preview_size", "L")
                .appendQueryParameter("preview_crop", "true")
                .appendQueryParameter("sort", "media_type=image");

        Uri uri = builder.build();
        addRequest(Request.Method.GET, uri, ParsingUtils::parseImageResourceList, listener);
        Log.i(LOG_TAG, "request for downloading folder info " + publicKey);
    }

    public void getDownloadLink(final String publicUrl, final ResponseListener<String> listener) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .encodedAuthority(DOWNLOAD_RESOURCE_URL)
                .appendQueryParameter("public_key", encodeUrl(publicUrl));

        Uri uri = builder.build();
        addRequest(Request.Method.GET, uri, ParsingUtils::parseDownloadUrl, listener);
        Log.i(LOG_TAG, "request for downloading file " + publicUrl);
    }

    private <T> void addRequest(int method, Uri uri, JsonParser<T> parser, ResponseListener<T> listener) {
        WeakReference<ResponseListener<T>> listenerReference = new WeakReference<>(listener);
        JsonObjectRequest request = new JsonObjectRequest(method, uri.toString(),
                null,
                response -> {
                    try {
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

    private interface JsonParser<T> {
        T parse(JSONObject json) throws JSONException;
    }
}
