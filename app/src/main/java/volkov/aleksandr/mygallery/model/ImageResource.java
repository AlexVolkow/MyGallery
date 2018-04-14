package volkov.aleksandr.mygallery.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

/**
 * Meta information about image.
 */
public class ImageResource implements Parcelable {
    /**
     * Public url of picture in Yandex.Disk
     */
    private String publicUrl;

    /**
     * Picture name
     */
    private String name;

    /**
     * Creation time
     */
    private DateTime created;

    /**
     * Time of last modification
     */
    private DateTime modified;

    /**
     * link to preview
     */
    private String preview;

    /**
     * Image size in bytes
     */
    private int size;

    private ImageResource() {
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getName() {
        return name;
    }

    public DateTime getCreated() {
        return created;
    }

    public DateTime getModified() {
        return modified;
    }

    public String getPreview() {
        return preview;
    }

    public int getSize() {
        return size;
    }

    public static Builder builder() {
        return new ImageResource().new Builder();
    }

    public class Builder {
        public Builder publicUrl(String publicUrl) {
            ImageResource.this.publicUrl = publicUrl;
            return this;
        }

        public Builder name(String name) {
            ImageResource.this.name = name;
            return this;
        }

        public Builder created(DateTime created) {
            ImageResource.this.created = created;
            return this;
        }

        public Builder modified(DateTime modified) {
            ImageResource.this.modified = modified;
            return this;
        }

        public Builder preview(String preview) {
            ImageResource.this.preview = preview;
            return this;
        }

        public Builder size(int size) {
            ImageResource.this.size = size;
            return this;
        }

        public ImageResource build() {
            return ImageResource.this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageResource that = (ImageResource) o;

        if (size != that.size) return false;
        if (publicUrl != null ? !publicUrl.equals(that.publicUrl) : that.publicUrl != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        if (modified != null ? !modified.equals(that.modified) : that.modified != null)
            return false;
        return preview != null ? preview.equals(that.preview) : that.preview == null;
    }

    @Override
    public int hashCode() {
        int result = publicUrl != null ? publicUrl.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (modified != null ? modified.hashCode() : 0);
        result = 31 * result + (preview != null ? preview.hashCode() : 0);
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return "ImageResource{" +
                "publicUrl='" + publicUrl + '\'' +
                ", name='" + name + '\'' +
                ", created=" + created +
                ", modified=" + modified +
                ", preview='" + preview + '\'' +
                ", size=" + size +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(publicUrl);
        dest.writeString(name);
        dest.writeSerializable(created);
        dest.writeSerializable(modified);
        dest.writeString(preview);
        dest.writeInt(size);
    }

    private ImageResource(Parcel parcel) {
        publicUrl = parcel.readString();
        name = parcel.readString();
        created = (DateTime) parcel.readSerializable();
        modified = (DateTime) parcel.readSerializable();
        preview = parcel.readString();
        size = parcel.readInt();
    }

    public static final Parcelable.Creator<ImageResource> CREATOR = new Parcelable.Creator<ImageResource>() {
        public ImageResource createFromParcel(Parcel in) {
            return new ImageResource(in);
        }

        public ImageResource[] newArray(int size) {
            return new ImageResource[size];
        }
    };
}
