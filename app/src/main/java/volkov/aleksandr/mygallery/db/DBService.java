package volkov.aleksandr.mygallery.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import volkov.aleksandr.mygallery.model.ImageResource;

import static volkov.aleksandr.mygallery.db.ImageResourceContract.ImageResourceEntry;
/**
 * Created by Alexandr Volkov on 11.04.2018.
 */

public class DBService {
    private DBHelper dbHelper;

    public DBService(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void addImageResource(ImageResource imageResource) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ImageResourceEntry.COLUMN_NAME, imageResource.getName());
        values.put(ImageResourceEntry.COLUMN_PUBLIC_URL, imageResource.getPublicUrl());
        values.put(ImageResourceEntry.COLUMN_CREATED, imageResource.getCreated().getMillis());
        values.put(ImageResourceEntry.COLUMN_MODIFIED, imageResource.getModified().getMillis());
        values.put(ImageResourceEntry.COLUMN_PREVIEW, imageResource.getPreview());
        values.put(ImageResourceEntry.COLUMN_SIZE, imageResource.getSize());

        db.insert(ImageResourceEntry.TABLE_NAME, null, values);
    }

    public List<ImageResource> getAllResources() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sortOrder =
                ImageResourceEntry.COLUMN_MODIFIED + " DESC";

        Cursor c = db.query(ImageResourceEntry.TABLE_NAME, null, null,
                null, null, null, sortOrder);

        List<ImageResource> res = new ArrayList<>();

        if (c.moveToFirst()) {
            int nameColIdx = c.getColumnIndex(ImageResourceEntry.COLUMN_NAME);
            int publicUrlColIdx = c.getColumnIndex(ImageResourceEntry.COLUMN_PUBLIC_URL);
            int createdColIdx = c.getColumnIndex(ImageResourceEntry.COLUMN_CREATED);
            int modifiedColIdx = c.getColumnIndex(ImageResourceEntry.COLUMN_MODIFIED);
            int previewColIdx = c.getColumnIndex(ImageResourceEntry.COLUMN_PREVIEW);
            int sizeColIdx = c.getColumnIndex(ImageResourceEntry.COLUMN_SIZE);

            do {
                DateTime created = new DateTime(c.getLong(createdColIdx));
                DateTime modified = new DateTime(c.getLong(modifiedColIdx));

                ImageResource imageResource = ImageResource.builder()
                        .name(c.getString(nameColIdx))
                        .created(created)
                        .modified(modified)
                        .preview(c.getString(previewColIdx))
                        .publicUrl(c.getString(publicUrlColIdx))
                        .size(c.getInt(sizeColIdx))
                        .build();

                res.add(imageResource);
            } while (c.moveToNext());
        }
        c.close();
        return res;
    }

}
