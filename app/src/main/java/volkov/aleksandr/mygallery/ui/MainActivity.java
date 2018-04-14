package volkov.aleksandr.mygallery.ui;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import volkov.aleksandr.mygallery.R;
import volkov.aleksandr.mygallery.db.DBService;
import volkov.aleksandr.mygallery.model.ImageResource;
import volkov.aleksandr.mygallery.network.ResponseListener;
import volkov.aleksandr.mygallery.network.YandexDrive;
import volkov.aleksandr.mygallery.utils.AndroidHelper;

import static volkov.aleksandr.mygallery.utils.LogHelper.makeLogTag;

public class MainActivity extends AppCompatActivity implements ResponseListener<List<ImageResource>> {
    private static final String LOG_TAG = makeLogTag(MainActivity.class);
    public static final int LIMIT = 120;
    public static final String IMAGE_RESOURCES = "image_resources";


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.main_toolbar)
    Toolbar mainToolbar;
    @BindView(R.id.main_activity_pb)
    ProgressBar progressBar;
    @BindView(R.id.tv_no_internet)
    TextView noInternet;

    private YandexDrive yandexDrive;
    private DBService dbService;
    private ImageAdapter adapter;
    private List<ImageResource> resources;

    private int mImageWidth;
    private int mPreviewSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mainToolbar);
        initDisplayParams();

        dbService = new DBService(this);
        yandexDrive = new YandexDrive(this);

        if (!AndroidHelper.hasConnection(this)) {
            noInternet.setVisibility(View.VISIBLE);
            return;
        } else {
            noInternet.setVisibility(View.GONE);
        }

        openProgressBar();
        if (savedInstanceState != null) {
            resources = savedInstanceState.getParcelableArrayList(IMAGE_RESOURCES);
            initRecyclerView(resources);
            hideProgressBar();
        } else {
            initRecyclerView(Collections.emptyList());
            LoadDataTask loadTask = new LoadDataTask(this);
            loadTask.execute();
        }
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void openProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void initDisplayParams() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mImageWidth = displayMetrics.widthPixels;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mImageWidth /= 2;
        }
        mPreviewSize = mImageWidth / 2;
    }

    private void initRecyclerView(List<ImageResource> resources) {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 12);
        int orientation = getResources().getConfiguration().orientation;
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == ImageAdapter.VIEW_TYPE_TEXT) {
                    return 12;
                }
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    return 3;
                }
                if (adapter.getScales().get(position) == ImageAdapter.SMALL_IMAGE_PREVIEW) {
                    return 4;
                } else {
                    return 6;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ImageAdapter(resources, mImageWidth, orientation);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(LOG_TAG, "error when download from yandex.drive " + error.getMessage());
        Toast.makeText(this, "Ошибка при загрузке данных с Яндекс.Диск, попробуйте позже",
                Toast.LENGTH_SHORT).show();
        hideProgressBar();
    }

    @Override
    public void onResponse(List<ImageResource> response) {
        Log.i(LOG_TAG, "successfully downloaded " + response.size() + " image resources");

        resources = response;

        LoadToDBTask loadToDBTask = new LoadToDBTask(this, response);
        loadToDBTask.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(IMAGE_RESOURCES, (ArrayList<ImageResource>) resources);
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, List<ImageResource>> {
        private WeakReference<MainActivity> activityReference;

        LoadDataTask(MainActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            MainActivity activity = activityReference.get();
            if (activity != null) {
                activity.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<ImageResource> doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (activity != null) {
                return activity.dbService.getAllResources();
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        protected void onPostExecute(List<ImageResource> imageResources) {
            MainActivity activity = activityReference.get();
            if (activity != null) {
                activity.resources = imageResources;
                if (imageResources.isEmpty()) {
                    performDownloading(activity);
                } else {
                    activity.adapter.setImageResources(imageResources);
                    activity.hideProgressBar();
                }
            }
        }
    }

    private static void performDownloading(MainActivity activity) {
        activity.yandexDrive.getPublicFolder(YandexDrive.FOLDER_URL, LIMIT, activity.mPreviewSize, activity);
    }

    private static class LoadToDBTask extends AsyncTask<Void, Void, Void> {
        private List<ImageResource> resources;
        private WeakReference<MainActivity> activityReference;

        public LoadToDBTask(MainActivity activity, List<ImageResource> resources) {
            this.resources = resources;
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (activity != null) {
                for (ImageResource resource : resources) {
                    activity.dbService.addImageResource(resource);
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MainActivity activity = activityReference.get();
            if (activity != null) {
                activity.adapter.setImageResources(resources);
                activity.hideProgressBar();
            }
        }
    }
}
