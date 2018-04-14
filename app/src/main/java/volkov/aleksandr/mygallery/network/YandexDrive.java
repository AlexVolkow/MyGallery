package volkov.aleksandr.mygallery.network;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import volkov.aleksandr.mygallery.model.ImageResource;

import static java.lang.Math.abs;
import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;

/**
 * Class for working with Yandex.Disk api.
 * {@see https://tech.yandex.ru/disk/api/concepts/about-docpage}
 */

public class YandexDrive {
    private static final String LOG_TAG = makeLogTag(YandexDrive.class);

    /**
     * Public folder with cats.
     */
    public static final String FOLDER_URL = "https://yadi.sk/d/L-oc0Bqa3UD4oL";

    /**
     * Basic query to obtain information about the resource
     */
    private static final String PUBLIC_RESOURCE_URL = "cloud-api.yandex.net/v1/disk/public/resources";

    /**
     * Base part of the file url request
     */
    private static final String DOWNLOAD_RESOURCE_URL = PUBLIC_RESOURCE_URL + "/download";

    /**
     * Request queue
     */
    private static RequestQueue queue;

    /**
     * Server response cache
     */
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

    /**
     * The method adds a request to the queue to get information about the public package.
     * Runs asynchronously, calls callback after execution
     *
     * @param publicKey public key of folder
     * @param limit limit on the number of downloaded resources from the folder
     * @param previewSize size of preview
     * @param listener callback
     */
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

    /**
     * The method adds a file url request to the queue.
     * Runs asynchronously, calls callback after execution
     *
     * @param publicUrl public key resource
     * @param listener callback
     */
    public void getDownloadLink(final String publicUrl, final ResponseListener<String> listener) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .encodedAuthority(DOWNLOAD_RESOURCE_URL)
                .appendQueryParameter("public_key", encodeUrl(publicUrl));

        Uri uri = builder.build();
        addRequest(Request.Method.GET, uri, ParsingUtils::parseDownloadUrl, listener, true);
        Log.i(LOG_TAG, "request for downloading file " + publicUrl);
    }

    /**
     * Generic method for adding request. If there is such a request in the cache,
     * it will return the answer and will not add the request to the queue.
     *
     * @param method http method of request
     * @param uri query url
     * @param parser response parser
     * @param listener callback
     * @param needCache flag that indicates whether the result should be cached
     */
    private <T> void addRequest(int method, Uri uri, ParsingUtils.JsonParser<T> parser,
                                ResponseListener<T> listener, boolean needCache) {
        JSONObject cached = jsonCache.get(uri);
        if (cached != null) {
            try {
                listener.onResponse(ParsingUtils.errorWrapper(parser, cached));
            } catch (JSONException e) {
                listener.onErrorResponse(new VolleyError(e.getMessage()));
                Log.e(LOG_TAG, "error " + e.getMessage() +
                        " occurred while parsing json for uri " + uri);
            }
            return;
        }
        WeakReference<ResponseListener<T>> listenerReference = new WeakReference<>(listener);
        JsonObjectRequest request = new JsonObjectRequest(method, uri.toString(),
                null,
                response -> {
                    ResponseListener<T> responseListener = listenerReference.get();
                    try {
                        if (jsonCache != null && needCache) {
                            jsonCache.put(uri, response);
                        }
                        if (responseListener != null) {
                            responseListener.onResponse(ParsingUtils.errorWrapper(parser, response));
                        }
                    } catch (JSONException e) {
                        responseListener.onErrorResponse(new VolleyError(e.getMessage()));
                        Log.e(LOG_TAG, "error " + e.getMessage() +
                                " occurred while parsing json for uri " + uri);
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

    private static List<Pair<Integer, String>> quality;

    static {
        quality = new ArrayList<>();
        quality.add(Pair.create(300, "M"));
        quality.add(Pair.create(500, "L"));
        quality.add(Pair.create(800, "XL"));
        quality.add(Pair.create(1024, "XXL"));
        quality.add(Pair.create(1280, "XXXL"));
    }

    /**
     * Method to get the preview size.
     *
     * @param previewSize preview size
     * @return string representation of preview quality
     */
    private static String getPreviewSize(int previewSize) {
        Pair<Integer, String> q = Collections.min(quality,
                (o1, o2) -> abs(o1.first - previewSize) - abs(o2.first - previewSize));
        return q.second;
    }
}
