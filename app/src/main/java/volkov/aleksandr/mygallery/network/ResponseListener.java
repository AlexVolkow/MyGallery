package volkov.aleksandr.mygallery.network;

import com.android.volley.Response;

/**
 * Interface for callback from server. Extends {@link Response.Listener} and {@link Response.ErrorListener}
 */
public interface ResponseListener<T> extends Response.Listener<T>, Response.ErrorListener {
}
