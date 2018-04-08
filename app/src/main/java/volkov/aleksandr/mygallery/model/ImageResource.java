package volkov.aleksandr.mygallery.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by alexa on 08.04.2018.
 */

public class ImageResource implements Parcelable {
    private String publicKey;
    private String name;
    private Date created;
    private Date modified;
    private String preview;
    private int size;

    private ImageResource() {
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }

    public Date getCreated() {
        return created;
    }

    public Date getModified() {
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
        public Builder publicKey(String publicKey) {
            ImageResource.this.publicKey = publicKey;
            return this;
        }

        public Builder name(String name) {
            ImageResource.this.name = name;
            return this;
        }

        public Builder created(Date created) {
            ImageResource.this.created = created;
            return this;
        }

        public Builder modified(Date modified) {
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
        if (publicKey != null ? !publicKey.equals(that.publicKey) : that.publicKey != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        if (modified != null ? !modified.equals(that.modified) : that.modified != null)
            return false;
        return preview != null ? preview.equals(that.preview) : that.preview == null;
    }

    @Override
    public int hashCode() {
        int result = publicKey != null ? publicKey.hashCode() : 0;
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
                "publicKey='" + publicKey + '\'' +
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
        dest.writeString(publicKey);
        dest.writeString(name);
        dest.writeSerializable(created);
        dest.writeSerializable(modified);
        dest.writeString(preview);
        dest.writeInt(size);
    }

    private ImageResource(Parcel parcel) {
        publicKey = parcel.readString();
        name = parcel.readString();
        created = (Date) parcel.readSerializable();
        modified = (Date) parcel.readSerializable();
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
