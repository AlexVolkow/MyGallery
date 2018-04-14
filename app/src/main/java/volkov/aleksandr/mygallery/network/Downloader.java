package volkov.aleksandr.mygallery.network;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;

/**
 * Class for downloading files from the network.Clients may
 * request that a URI be downloaded to a particular destination file. The download manager will
 * conduct the download in the background. The files are downloaded in {@link Environment#DIRECTORY_DOWNLOADS}
 * <p>
 * Note that the application must have the {@link android.Manifest.permission#INTERNET}
 * permission to use this class.
 */
public class Downloader {
    private static final String LOG_TAG = makeLogTag(Downloader.class);

    private static final File DOWNLOAD_DIR =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    private DownloadManager downloadManager;
    private static Map<Long, String> files = new ConcurrentHashMap<>();
    private Context context;

    public Downloader(Context context) {
        this.context = context;

        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        BroadcastReceiver receiver = createReceiver();
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    /**
     * Creates {@link BroadcastReceiver} for information on the download completion.
     * Notifies the user with a pop-up message {@link Toast}
     *
     * @return required {@link BroadcastReceiver}
     */
    private BroadcastReceiver createReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            if (files.containsKey(downloadId)) {
                                String fileName = files.get(downloadId);
                                Toast.makeText(context, "Download complete " + fileName,
                                        Toast.LENGTH_SHORT).show();
                                Log.i(LOG_TAG, "File " + fileName + " is downloaded");
                            }
                        }
                    }
                    c.close();
                }
            }
        };
    }

    /**
     * Adds a request to download files to the queue.
     * For the downloading of the file meets the {@link DownloadManager}
     * The files are downloaded in {@link Environment#DIRECTORY_DOWNLOADS}
     *
     * @param url      file url
     * @param filename name of file
     */
    public void download(Uri url, String filename) {
        if (!DOWNLOAD_DIR.exists()) {
            if (!DOWNLOAD_DIR.mkdirs()) {
                Toast.makeText(context, "Can't create Download directory", Toast.LENGTH_SHORT).show();
            }
        }

        DownloadManager.Request request = new DownloadManager.Request(url);
        request.setTitle(filename)
                .setDescription("File is being downloaded...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename)
                .allowScanningByMediaScanner();

        long id = downloadManager.enqueue(request);
        files.put(id, filename);

        Toast.makeText(context, "Start downloading " + filename, Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG, "File " + filename + " is being downloaded (id = " + id + ")");
    }
}
