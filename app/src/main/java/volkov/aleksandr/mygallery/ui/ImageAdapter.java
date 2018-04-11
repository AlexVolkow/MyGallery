package volkov.aleksandr.mygallery.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import volkov.aleksandr.mygallery.R;
import volkov.aleksandr.mygallery.model.ImageResource;
import volkov.aleksandr.mygallery.utils.DateHelper;

/**
 * Created by alexa on 08.04.2018.
 */

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_IMAGE = 2;
    public static final int VIEW_TYPE_TEXT = 1;

    public static final int MIDDLE_IMAGE_PREVIEW = 2;
    public static final int SMALL_IMAGE_PREVIEW = 3;

    private SparseArray<DateTime> timestamp = new SparseArray<>();
    private SparseIntArray offsets = new SparseIntArray();
    private SparseIntArray scales = new SparseIntArray();

    private List<ImageResource> imageResources;

    private int mImageWidth;
    private int mImageHeight;
    private int orientation;

    public ImageAdapter(List<ImageResource> imageResources, int imageWidth, int orientation) {
        this.imageResources = imageResources;
        this.orientation = orientation;
        this.mImageWidth = imageWidth;
        this.mImageHeight = mImageWidth * 4 / 3;
        init(imageResources);
    }

    private void init(List<ImageResource> imageResources) {
        Collections.sort(imageResources, (o1, o2) ->
                DateHelper.DATE_COMPARATOR.compare(o2.getModified(), o1.getModified()));
        createTimeStamps(imageResources);
    }

    @Override
    public int getItemViewType(int position) {
        if (timestamp.indexOfKey(position) >= 0) {
            return VIEW_TYPE_TEXT;
        } else {
            return VIEW_TYPE_IMAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TEXT) {
            return createTextHolder(parent);
        }
        return createImageHolder(parent);
    }

    private RecyclerView.ViewHolder createTextHolder(ViewGroup parent) {
        TextView textView = new TextView(parent.getContext());
        textView.setTextSize(14f);
        textView.setPadding(20, 60 ,20, 60);
        textView.setTextColor(parent.getContext().getResources().getColor(android.R.color.black));
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        return new TextViewHolder(textView);
    }

    private RecyclerView.ViewHolder createImageHolder(ViewGroup parent) {
        ImageView imageView = new ImageView(parent.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(1, 1, 1, 1);
        imageView.setLayoutParams(lp);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_TEXT:
                bindTextHolder(holder, position);
                break;
            case VIEW_TYPE_IMAGE:
                bindImageHolder(holder, position);
                break;
        }
    }

    private void bindTextHolder(RecyclerView.ViewHolder holder, int position) {
        TextViewHolder textHolder = (TextViewHolder) holder;
        textHolder.textView.setText(DateHelper.LONG_DATE.print(timestamp.get(position)));
    }

    private void bindImageHolder(RecyclerView.ViewHolder holder, int position) {
        ImageViewHolder imageHolder = (ImageViewHolder) holder;
        int pos = offsets.get(position);
        ImageResource imageResource = imageResources.get(pos);

        imageHolder.imageView.setOnClickListener(v -> openFullImage(v.getContext(), pos));

        int scale = scales.get(position);
        Picasso.with(imageHolder.imageView.getContext())
                .load(imageResource.getPreview())
                .placeholder(R.drawable.ic_looper)
                .resize(mImageWidth / scale, mImageHeight / scale)
                .centerCrop()
                .error(android.R.drawable.stat_notify_error)
                .into(imageHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageResources.size() + timestamp.size();
    }

    public void setImageResources(List<ImageResource> imageResources) {
        this.imageResources = imageResources;
        init(imageResources);
        notifyDataSetChanged();
    }

    private void createTimeStamps(List<ImageResource> imageResources) {
        timestamp.clear();
        offsets.clear();
        scales.clear();
        int offset = 0;
        for (int i = 0; i < imageResources.size(); i++) {
            timestamp.put(i + offset, imageResources.get(i).getModified());
            offset++;
            int j = i;
            while (j < imageResources.size() &&
                    DateHelper.DATE_COMPARATOR.compare(imageResources.get(i).getModified(),
                            imageResources.get(j).getModified()) == 0) {
                offsets.put(j + offset, j);
                j++;
            }
            updateScale(i + offset, j + offset);
            i = j - 1;
        }
    }

    private void updateScale(int from, int to) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            for (int i = from; i < to; i++) {
                scales.put(i, MIDDLE_IMAGE_PREVIEW);
            }
            return;
        }

        int len = to - from;
        boolean turn = true;
        for (int i = 0; i < len; i++) {
            int rest = len - i;
            if (rest == 1) {
                scales.put(from + i, MIDDLE_IMAGE_PREVIEW);
            } else if (rest >= 2) {
                int imagePreviewSize = MIDDLE_IMAGE_PREVIEW;
                if (!turn && rest >= 3) {
                    imagePreviewSize = SMALL_IMAGE_PREVIEW;
                }
                for (int j = from + i; j < from + i + imagePreviewSize; j++) {
                    scales.put(j, imagePreviewSize);
                }
                i += imagePreviewSize - 1;
                turn = !turn;
            }
        }
    }

    private void openFullImage(Context context, int idx) {
        Intent intent = new Intent();
        intent.setClass(context, FullImageActivity.class);
        intent.putExtra(FullImageActivity.IMAGE, imageResources.get(idx));
        intent.putExtra(FullImageActivity.CURR_IDX, idx);
        intent.putExtra(FullImageActivity.SIZE, imageResources.size());
        context.startActivity(intent);
    }

    public SparseIntArray getScales() {
        return scales;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public TextViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView;
        }
    }
}
