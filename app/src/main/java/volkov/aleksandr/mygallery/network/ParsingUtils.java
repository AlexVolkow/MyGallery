package volkov.aleksandr.mygallery.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import volkov.aleksandr.mygallery.model.ImageResource;

import static volkov.aleksandr.mygallery.db.ImageResourceContract.ImageResourceEntry;
import static volkov.aleksandr.mygallery.utils.DateHelper.parseTime;

/**
 * Utilities class for parsing response from server.
 */
public class ParsingUtils {
    private ParsingUtils() {

    }

    /**
     * Parses an object of type ResourceList
     * {@see https://tech.yandex.ru/disk/api/reference/response-objects-docpage/#resourcelist}
     *
     * @param response json from server
     * @return list of {@link ImageResource}
     * @throws JSONException if missing required fields or if response isn't a public folder
     */
    public static List<ImageResource> parseImageResourceList(JSONObject response) throws JSONException {
        if (!response.has("_embedded")) {
            throw new JSONException("embedded = null(isn't a public folder)");
        }

        JSONObject embedded = response.getJSONObject("_embedded");
        JSONArray items = embedded.getJSONArray("items");
        List<ImageResource> imageList = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject image = items.getJSONObject(i);
            imageList.add(parseImageResource(image));
        }
        return imageList;
    }

    /**
     * Parses an object of type Resource
     * {@see https://tech.yandex.ru/disk/api/reference/response-objects-docpage/#resource}
     *
     * @param response json from server
     * @return information about the requested resource in the form of {@link ImageResource}
     * @throws JSONException if missing required fields
     */
    public static ImageResource parseImageResource(JSONObject response) throws JSONException {
        ImageResource.Builder imageBuilder = ImageResource.builder();
        imageBuilder.publicUrl(response.getString(ImageResourceEntry.COLUMN_PUBLIC_URL))
                .name(response.getString(ImageResourceEntry.COLUMN_NAME))
                .created(parseTime(response.getString(ImageResourceEntry.COLUMN_CREATED)))
                .modified(parseTime(response.getString(ImageResourceEntry.COLUMN_MODIFIED)))
                .preview(response.getString(ImageResourceEntry.COLUMN_PREVIEW))
                .size(response.getInt(ImageResourceEntry.COLUMN_SIZE));
        return imageBuilder.build();
    }

    /**
     * Parses an object of type Link
     * {@see https://tech.yandex.ru/disk/api/reference/response-objects-docpage/#resource}
     *
     * @param response json from server
     * @return string representation of url for resource
     * @throws JSONException if missing required fields
     */
    public static String parseDownloadUrl(JSONObject response) throws JSONException {
        return response.getString("href");
    }

    /**
     * Parses an object of type Error
     * {@see https://tech.yandex.ru/disk/api/reference/response-objects-docpage/#error}
     *
     * @param response json from server
     * @return string representation of error
     * @throws JSONException if missing required fields
     */
    private static String parseError(JSONObject response) throws JSONException {
        if (response.has("error")) {
            String error = response.getString("error");
            String description = response.getString("description");
            return error + ": " + description;
        }
        return null;
    }

    /**
     * Wrap over {@link JsonParser} which handles the error information
     *
     * @param parser   {@link JsonParser}
     * @param response json from server
     * @throws JSONException if response is error message
     */
    public static <T> T errorWrapper(JsonParser<T> parser, JSONObject response) throws JSONException {
        String error = parseError(response);
        if (error != null) {
            throw new JSONException(error);
        }
        return parser.parse(response);
    }

    public interface JsonParser<T> {
        T parse(JSONObject json) throws JSONException;
    }
}
