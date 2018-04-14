package volkov.aleksandr.mygallery.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import volkov.aleksandr.mygallery.R;
import volkov.aleksandr.mygallery.model.ImageResource;
import volkov.aleksandr.mygallery.network.Downloader;
import volkov.aleksandr.mygallery.network.ResponseListener;
import volkov.aleksandr.mygallery.network.YandexDrive;
import volkov.aleksandr.mygallery.utils.AndroidHelper;
import volkov.aleksandr.mygallery.utils.DateHelper;

import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;

public class FullImageActivity extends AppCompatActivity implements ResponseListener<String> {
    private static final String LOG_TAG = makeLogTag(FullImageActivity.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

    public static final String IMAGE = "image";
    public static final String CURR_IDX = "curr_idx";
    public static final String SIZE = "size";
    public static final String IMAGE_URL = "downloadUrl";

    @BindView(R.id.full_image_toolbar)
    Toolbar toolbar;
    @BindView(R.id.full_image_pb)
    ProgressBar progressBar;
    @BindView(R.id.photo_view)
    PhotoView photoView;

    private ImageResource imageResource;
    private String downloadUrl;
    private int size;
    private int currIdx;
    private YandexDrive yandexDrive;
    private Downloader downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        ButterKnife.bind(this);

        progressBar.setVisibility(View.VISIBLE);

        yandexDrive = new YandexDrive(getApplicationContext());
        downloader = new Downloader(getApplicationContext());

        boolean hasConnection = true;
        if (!AndroidHelper.hasConnection(this)) {
            Toast.makeText(this, "Проверьте подключение к интернету", Toast.LENGTH_SHORT)
                    .show();
            hasConnection = false;
            progressBar.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            downloadUrl = savedInstanceState.getString(IMAGE_URL);
            imageResource = savedInstanceState.getParcelable(IMAGE);
            updateTitle(savedInstanceState);
            if (downloadUrl != null && hasConnection) {
                loadImage(downloadUrl);
            }
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                imageResource = intent.getParcelableExtra(IMAGE);
                if (hasConnection) {
                    yandexDrive.getDownloadLink(imageResource.getPublicUrl(), this);
                }
                updateTitle(intent.getExtras());
            }
        }

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }


    private void updateTitle(Bundle bundle) {
        currIdx = bundle.getInt(CURR_IDX, 0) + 1;
        size = bundle.getInt(SIZE, 1);
        toolbar.setTitle(currIdx + " из " + size);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(IMAGE_URL, downloadUrl);
        outState.putParcelable(IMAGE, imageResource);
        outState.putInt(SIZE, size);
        outState.putInt(CURR_IDX, currIdx - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.show_info:
                if (imageResource != null) {
                    AndroidHelper.showAlert(this, "Информация", createInfo(imageResource));
                }
                return true;

            case R.id.download:
                performDownload();
                return true;

            case R.id.action_share:
                if (imageResource != null) {
                    share(imageResource.getPublicUrl());
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void performDownload() {
        if (imageResource != null && downloadUrl != null) {
            downloader.download(Uri.parse(downloadUrl), imageResource.getName());
        } else {
            Toast.makeText(this, "Невозможно скачать файл, попробуйте позже",
                    Toast.LENGTH_SHORT).show();
            if (imageResource == null) {
                Log.e(LOG_TAG, "Unable to download file, imageResource is null");
            }
            if (downloadUrl == null) {
                Log.e(LOG_TAG, "download link is not ready yet " + imageResource.getName());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return true;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        downloadFailed(error.getMessage());
    }

    private void downloadFailed(String msg) {
        Log.e(LOG_TAG, "error when downloading image" + msg);
        Toast.makeText(this, "Error when downloading image", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(String response) {
        downloadUrl = response;
        Log.i(LOG_TAG, "open image with " + response);
        loadImage(response);
    }

    private void loadImage(String response) {
        Picasso.with(this)
                .load(response)
                .error(android.R.drawable.stat_notify_error)
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        downloadFailed("picasso failed");
                    }
                });
    }


    private void share(String url) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private static String createInfo(ImageResource image) {
        StringBuilder sb = new StringBuilder();
        sb.append("Имя: ").append(image.getName()).append("\n");
        double size = image.getSize() / 1024.0;
        sb.append("Размер: ").append(DECIMAL_FORMAT.format(size)).append(" kb\n");
        sb.append("Время создания: ").append("\n")
                .append(DateHelper.LONG_DATE.print(image.getCreated())).append("\n");
        sb.append("Время последнего изменения: ").append("\n")
                .append(DateHelper.LONG_DATE.print(image.getModified())).append("\n");
        return sb.toString();
    }
}
