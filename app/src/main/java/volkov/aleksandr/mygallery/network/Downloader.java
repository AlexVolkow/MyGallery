package volkov.aleksandr.mygallery.network;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;

/**
 * Created by alexa on 11.04.2018.
 */

public class Downloader {
    private static final String LOG_TAG = makeLogTag(Downloader.class);

    private static final File DOWNLOAD_DIR =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    private DownloadManager downloadManager;
    private Context context;

    public Downloader(Context context) {
        this.context = context;
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void download(Uri url, String filename) {
        if (!DOWNLOAD_DIR.exists()) {
            if (!DOWNLOAD_DIR.mkdirs()) {
                Toast.makeText(context, "Can't create Download directory", Toast.LENGTH_SHORT).show();
            }
        }

        DownloadManager.Request request = new DownloadManager.Request(url);
        request.setTitle(filename + " downloading")
                .setDescription("File is being downloaded...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename)
                .allowScanningByMediaScanner();

        long id = downloadManager.enqueue(request);
        Toast.makeText(context, "Start downloading " + filename, Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG, "File " + filename + " is being downloaded (id = " + id + ")");
    }

}
