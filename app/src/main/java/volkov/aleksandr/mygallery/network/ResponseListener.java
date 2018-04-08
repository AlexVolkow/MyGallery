package volkov.aleksandr.mygallery.network;

import com.android.volley.Response;

/**
 * Created by AlexandrVolkov on 07.07.2017.
 */
public interface ResponseListener<T> extends Response.Listener<T>, Response.ErrorListener {
}
