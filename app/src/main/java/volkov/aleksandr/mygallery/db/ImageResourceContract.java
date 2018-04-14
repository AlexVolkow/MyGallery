package volkov.aleksandr.mygallery.db;

import android.provider.BaseColumns;

import volkov.aleksandr.mygallery.model.ImageResource;

/**
 * Created by Alexandr Volkov on 11.04.2018.
 */

public class ImageResourceContract {
    private ImageResourceContract() {}

    public static class ImageResourceEntry implements BaseColumns {
        public static final String TABLE_NAME = "image_resources";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PUBLIC_URL = "public_url";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_MODIFIED = "modified";
        public static final String COLUMN_PREVIEW = "preview";
        public static final String COLUMN_SIZE = "size";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + ImageResourceEntry.TABLE_NAME + " (" +
                    ImageResourceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ImageResourceEntry.COLUMN_NAME + " TEXT, " +
                    ImageResourceEntry.COLUMN_PUBLIC_URL + " TEXT, " +
                    ImageResourceEntry.COLUMN_CREATED + " INTEGER, " +
                    ImageResourceEntry.COLUMN_MODIFIED + " INTEGER, " +
                    ImageResourceEntry.COLUMN_PREVIEW + " TEXT, " +
                    ImageResourceEntry.COLUMN_SIZE + " INTEGER)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ImageResourceEntry.TABLE_NAME;
}
