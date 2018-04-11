package volkov.aleksandr.mygallery.network;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import volkov.aleksandr.mygallery.model.ImageResource;

import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;
import static volkov.aleksandr.mygallery.db.ImageResourceContract.ImageResourceEntry;

/**
 * Created by alexa on 08.04.2018.
 */

public class ParsingUtils {
    private static final String LOG_TAG = makeLogTag(ParsingUtils.class);

    public static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTimeNoMillis();

    private ParsingUtils() {

    }

    public static List<ImageResource> parseImageResourceList(JSONObject response) throws JSONException {
        JSONObject embedded = response.getJSONObject("_embedded");
        if (embedded == null) {
            throw new JSONException("embedded = null(isn't a public folder)");
        }
        JSONArray items = embedded.getJSONArray("items");
        List<ImageResource> imageList = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject image = items.getJSONObject(i);
            imageList.add(parseImageResource(image));
        }
        return imageList;
    }

    public static ImageResource parseImageResource(JSONObject image) throws JSONException {
        ImageResource.Builder imageBuilder = ImageResource.builder();
        imageBuilder.publicUrl(image.getString(ImageResourceEntry.COLUMN_PUBLIC_URL))
                .name(image.getString(ImageResourceEntry.COLUMN_NAME))
                .created(parseTime(image.getString(ImageResourceEntry.COLUMN_CREATED)))
                .modified(parseTime(image.getString(ImageResourceEntry.COLUMN_MODIFIED)))
                .preview(image.getString(ImageResourceEntry.COLUMN_PREVIEW))
                .size(image.getInt(ImageResourceEntry.COLUMN_SIZE));
        return imageBuilder.build();
    }


    public static String parseDownloadUrl(JSONObject json) throws JSONException {
        return json.getString("href");
    }

    private static DateTime parseTime(String time) {
        return DATE_FORMAT.parseDateTime(time);
    }
}
