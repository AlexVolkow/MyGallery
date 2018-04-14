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
 * Created by Alexandr Volkov on 08.04.2018.
 */

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_TEXT = 1;
    public static final int MIDDLE_IMAGE_PREVIEW = 2;
    public static final int SMALL_IMAGE_PREVIEW = 3;
    public static final float TEXT_SIZE = 14f;

    /**
     * Position in adapter for time stamps.
     */
    private SparseArray<DateTime> timestamp = new SparseArray<>();

    /**
     * Supporting information about offsets in data set.
     */
    private SparseIntArray offsets = new SparseIntArray();

    /**
     * Information about image size.
     */
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
            return scales.get(position);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TEXT) {
            return createTextHolder(parent);
        }
        if (viewType == MIDDLE_IMAGE_PREVIEW) {
            return createImageHolder(parent, MIDDLE_IMAGE_PREVIEW);
        }
        return createImageHolder(parent, SMALL_IMAGE_PREVIEW);
    }

    private RecyclerView.ViewHolder createTextHolder(ViewGroup parent) {
        TextView textView = new TextView(parent.getContext());
        textView.setTextSize(TEXT_SIZE);
        textView.setPadding(20, 60, 20, 60);
        textView.setTextColor(parent.getContext().getResources().getColor(android.R.color.black));
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        return new TextViewHolder(textView);
    }

    private RecyclerView.ViewHolder createImageHolder(ViewGroup parent, int size) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setPadding(2, 2, 2, 2);

        int width = mImageWidth / size;
        int height = mImageHeight / size;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        imageView.setLayoutParams(lp);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_TEXT:
                bindTextHolder(holder, position);
                break;
            case SMALL_IMAGE_PREVIEW:
            case MIDDLE_IMAGE_PREVIEW:
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
                .placeholder(R.color.placeholderColor)
                .noFade()
                .resize(mImageWidth / scale, mImageHeight / scale)
                .centerCrop()
                .error(R.drawable.ic_cat)
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

    public void clear() {
        setImageResources(Collections.emptyList());
    }

    /**
     * Method create time stamps for current data set.
     * Each time stamp corresponds to a certain day of the year.
     *
     * @param imageResources list of {@link ImageResource} sorted by {@link ImageResource#getModified()}
     */
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

    /**
     * Method update information about image size for range [from, to).
     * The algorithm tries to alternate rows of three images with rows of two images.
     * There are optimizations for landscape mode.
     *
     * @param from start of range
     * @param to   end of range
     */
    private void updateScale(int from, int to) {
        // it's optimization for landscape mode
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            for (int i = from; i < to; i++) {
                scales.put(i, MIDDLE_IMAGE_PREVIEW);
            }
            return;
        }

        int len = to - from;

        // it's optimization for three images
        if (len == SMALL_IMAGE_PREVIEW) {
            for (int j = from; j < to; j++) {
                scales.put(j, SMALL_IMAGE_PREVIEW);
            }
            return;
        }

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
