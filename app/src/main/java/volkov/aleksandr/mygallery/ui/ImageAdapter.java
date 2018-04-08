package volkov.aleksandr.mygallery.ui;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import volkov.aleksandr.mygallery.model.ImageResource;

/**
 * Created by alexa on 08.04.2018.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<ImageResource> imageResources;
    private int mImageWidth;
    private int mImageHeight;

    public ImageAdapter(List<ImageResource> imageResources, int imageWidth, int imageHeight) {
        this.imageResources = imageResources;
        this.mImageHeight = imageHeight;
        this.mImageWidth = imageWidth;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mImageWidth, mImageHeight);
        imageView.setLayoutParams(params);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageResource imageResource = imageResources.get(position);
        Picasso.with(holder.imageView.getContext())
                .load(imageResource.getPreview())
                .resize(mImageWidth, mImageHeight)
                .centerCrop()
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageResources.size();
    }

    public void setImageResources(List<ImageResource> imageResources) {
        this.imageResources = imageResources;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }

    private int calculatePreviewSize(Bitmap source) {
        final int photoWidth = source.getWidth();
        final int photoHeight = source.getHeight();
        int scaleFactor = 1;

        if (photoWidth > mImageWidth || photoHeight > mImageHeight) {
            int halfPhotoWidth = photoWidth / 2;
            int halfPhotoHeight = photoHeight / 2;
            while (halfPhotoWidth / scaleFactor > mImageWidth ||
                    halfPhotoHeight / scaleFactor > mImageHeight) {
                scaleFactor *= 2;
            }
        }
        return scaleFactor;
    }
}
