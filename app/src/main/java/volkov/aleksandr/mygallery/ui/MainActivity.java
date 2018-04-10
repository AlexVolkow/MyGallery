package volkov.aleksandr.mygallery.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.volley.VolleyError;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import volkov.aleksandr.mygallery.R;
import volkov.aleksandr.mygallery.model.ImageResource;
import volkov.aleksandr.mygallery.network.Disk;
import volkov.aleksandr.mygallery.network.ResponseListener;

public class MainActivity extends AppCompatActivity implements ResponseListener<List<ImageResource>> {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Disk disk;
    private ImageAdapter adapter;

    private static int mImageWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initRecyclerView();

        disk = new Disk(getApplicationContext());
        disk.getPublicFolder(Disk.CATS_URL, 20, this);
    }

    private void initRecyclerView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mImageWidth = displayMetrics.widthPixels;

        GridLayoutManager layoutManager = new GridLayoutManager(this, 6);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == ImageAdapter.VIEW_TYPE_TEXT) {
                    return 6;
                }
                if (adapter.getScales().get(position) == ImageAdapter.SMALL_IMAGE_PREVIEW) {
                    return 2;
                } else {
                    return 3;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ImageAdapter(Collections.emptyList(), mImageWidth);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("image", error.getMessage());
    }

    @Override
    public void onResponse(List<ImageResource> response) {
        for (ImageResource imageResource : response) {
            Log.i("image", imageResource.getName() + " " + imageResource.getPublicUrl());
        }
        adapter.setImageResources(response);
    }
}
