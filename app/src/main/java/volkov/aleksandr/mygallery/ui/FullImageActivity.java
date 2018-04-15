package volkov.aleksandr.mygallery.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import volkov.aleksandr.mygallery.R;
import volkov.aleksandr.mygallery.model.ImageResource;
import volkov.aleksandr.mygallery.network.Downloader;
import volkov.aleksandr.mygallery.network.ResponseListener;
import volkov.aleksandr.mygallery.network.YandexDrive;
import volkov.aleksandr.mygallery.utils.AndroidHelper;
import volkov.aleksandr.mygallery.utils.DateHelper;
import volkov.aleksandr.mygallery.utils.HackyViewPager;

import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;

public class FullImageActivity extends AppCompatActivity {
    private static final String LOG_TAG = makeLogTag(FullImageActivity.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

    public static final String CURR_IDX = "curr_idx";
    public static final String IMAGE_RESOURCES = "image_resources";

    @BindView(R.id.full_image_toolbar)
    Toolbar toolbar;
    @BindView(R.id.full_image_pb)
    ProgressBar progressBar;
    @BindView(R.id.photo_pager)
    HackyViewPager photoPager;

    private SparseArray<String> downloadUrls = new SparseArray<>();
    private int currIdx;
    private int size;
    private boolean hasConnection;

    private ArrayList<ImageResource> resources;
    private YandexDrive yandexDrive;
    private Downloader downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        ButterKnife.bind(this);

        yandexDrive = new YandexDrive(getApplicationContext());
        downloader = new Downloader(getApplicationContext());

        hasConnection = true;
        if (!AndroidHelper.hasConnection(this)) {
            Toast.makeText(this, R.string.check_internet, Toast.LENGTH_SHORT)
                    .show();
            hasConnection = false;
        }

        Bundle bundle = savedInstanceState;
        if (bundle == null) {
            Intent intent = getIntent();
            if (intent != null) {
                bundle = intent.getExtras();
            }
        }
        extract(bundle);

        initPager();

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void initPager() {
        photoPager.setAdapter(new ImagePagerAdapter(resources));
        photoPager.setCurrentItem(currIdx);
        updateTitle();

        photoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currIdx = position;
                updateTitle();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private boolean checkWritePermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void updateTitle() {
        toolbar.setTitle((currIdx + 1) + " из " + size);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void openProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }


    private void extract(Bundle bundle) {
        resources = bundle.getParcelableArrayList(IMAGE_RESOURCES);
        currIdx = bundle.getInt(CURR_IDX, 0);
        size = resources.size();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURR_IDX, currIdx);
        outState.putParcelableArrayList(IMAGE_RESOURCES, resources);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length >= 2 && grantResults[0]== PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            performDownload();
        } else {
            Toast.makeText(this, R.string.not_permissions, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.show_info:
                AndroidHelper.showAlert(this, "Информация", createInfo(resources.get(currIdx)));
                return true;

            case R.id.download:
                if (checkWritePermissions()) {
                    performDownload();
                } else {
                    AndroidHelper.requestWriteStoragePermission(this);
                }
                return true;

            case R.id.action_share:
                share(resources.get(currIdx).getPublicUrl());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void performDownload() {
        ImageResource imageResource = resources.get(currIdx);
        String downloadUrl = downloadUrls.indexOfKey(currIdx) >= 0 ? downloadUrls.get(currIdx) : null;
        if (imageResource != null && downloadUrl != null) {
            downloader.download(Uri.parse(downloadUrl), imageResource.getName());
        } else {
            Toast.makeText(this, R.string.impossible_download,
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
        inflater.inflate(R.menu.full_image_context_menu, menu);
        return true;
    }

    private void downloadFailed(String msg) {
        Log.e(LOG_TAG, "error when downloading image" + msg);
        Toast.makeText(this, "Error when downloading image", Toast.LENGTH_SHORT).show();
    }

    private void loadImage(int idx, String publicUrl, PhotoView photoView) {
        openProgressBar();
        if (downloadUrls.indexOfKey(idx) >= 0) {
            loadImageToView(downloadUrls.get(idx), photoView);
        } else {
            yandexDrive.getDownloadLink(publicUrl, new ImageDownloadListener(idx, photoView, this));
        }
    }

    private void loadImageToView(String url, PhotoView photoView) {
        Picasso.with(this)
                .load(url)
                .error(android.R.drawable.stat_notify_error)
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        hideProgressBar();
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

    class ImagePagerAdapter extends PagerAdapter {
        ArrayList<ImageResource> imageList;

        ImagePagerAdapter(ArrayList<ImageResource> imageList) {
            this.imageList = imageList;
        }

        @Override
        public int getCount() {
            return (null != imageList) ? imageList.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());

            photoView.setMaximumScale(5.0F);
            photoView.setMediumScale(3.0F);
            container.addView(photoView, LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT);

            if (hasConnection) {
                loadImage(position, imageList.get(position).getPublicUrl(), photoView);
            }
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private static class ImageDownloadListener implements ResponseListener<String> {
        private WeakReference<PhotoView> photoViewReference;
        private WeakReference<FullImageActivity> activityReference;
        private int idx;

        ImageDownloadListener(int idx, PhotoView photoView, FullImageActivity activity) {
            this.activityReference = new WeakReference<>(activity);
            this.idx = idx;
            this.photoViewReference = new WeakReference<>(photoView);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            FullImageActivity activity = activityReference.get();
            if (activity != null) {
                activity.downloadFailed(error.getMessage());
            }
        }

        @Override
        public void onResponse(String response) {
            FullImageActivity activity = activityReference.get();
            if (activity != null) {
                activity.downloadUrls.put(idx, response);
                Log.i(LOG_TAG, "open image with " + response);

                PhotoView photoView = photoViewReference.get();
                if (photoView != null) {
                    activity.loadImageToView(response, photoView);
                }
            }
        }
    }
}
