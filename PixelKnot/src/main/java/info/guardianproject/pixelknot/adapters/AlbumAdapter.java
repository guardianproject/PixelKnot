package info.guardianproject.pixelknot.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import info.guardianproject.pixelknot.R;


public class AlbumAdapter extends RecyclerView.Adapter<AlbumViewHolder> {
    public interface AlbumAdapterListener {
        void onAlbumSelected(String album);
        void onPickExternalSelected();
    }

    private Context mContext;
    private ArrayList<AlbumInfo> mAlbums;
    private boolean mShowPickExternal;
    private AlbumAdapterListener mListener;

    public AlbumAdapter(Context context, boolean showPickExternal) {
        super();
        mContext = context;
        mAlbums = new ArrayList<AlbumInfo>();
        mShowPickExternal = showPickExternal;
        getAlbums();
    }

    public void setListener(AlbumAdapterListener listener) {
        mListener = listener;
    }

    private void getThumbnailAndCountForAlbum(AlbumInfo album) {
        album.thumbnail = null;
        album.count = 0;
        try {
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            String searchParams = "bucket_display_name = \"" + album.albumName + "\"";

            Cursor photoCursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, searchParams, null, orderBy + " DESC");
            if (photoCursor.getCount() > 0) {
                album.count = photoCursor.getCount();
                photoCursor.moveToNext();
                int colIndexUri = photoCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                album.thumbnail = photoCursor.getString(colIndexUri);
            }
            photoCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAlbums() {

        mAlbums.clear();

        String[] PROJECTION_BUCKET = {MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.DATA};

        String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
        String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cur = mContext.getContentResolver().query(images, PROJECTION_BUCKET,
                BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);

        if (cur.moveToFirst()) {
            String bucket;
            String date;
            String data;
            long bucketId;
            int bucketColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
            int bucketIdColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            do {
                // Get the field values
                bucket = cur.getString(bucketColumn);
                date = cur.getString(dateColumn);
                data = cur.getString(dataColumn);
                bucketId = cur.getInt(bucketIdColumn);

                if (bucket != null && bucket.length() > 0) {
                    AlbumInfo album = new AlbumInfo();
                    album.albumName = bucket;
                    getThumbnailAndCountForAlbum(album);
                    mAlbums.add(album);
                }
            } while (cur.moveToNext());
        }
        cur.close();
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowPickExternal) {
            if (position == 0)
                return 1;
            else
                position--;
        }
        return 0;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(viewType == 0 ? R.layout.album_item : R.layout.album_external_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        if (mShowPickExternal) {
            if (position == 0) {
                holder.mRootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            mListener.onPickExternalSelected();
                        }
                    }
                });
                return;
            } else {
                position--; //Offset by this item
            }
        }
        holder.mRootView.setOnClickListener(new ItemClickListener(position));
        AlbumInfo album = mAlbums.get(position);
        holder.mAlbumName.setText(album.albumName);
        holder.mAlbumCount.setText(String.format("(%d)", album.count));
        holder.mAlbumThumbnail.setImageURI(Uri.parse(album.thumbnail));
    }

    @Override
    public int getItemCount() {
        return (mShowPickExternal ? 1 : 0) + mAlbums.size();
    }

    private class AlbumInfo {
        public AlbumInfo() {
        }
        public String albumName;
        public String thumbnail;
        public int count;
    }

    private class ItemClickListener implements View.OnClickListener {
        private int mPosition;

        public ItemClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View view) {
            AlbumInfo album = mAlbums.get(mPosition);
            if (mListener != null) {
                mListener.onAlbumSelected(album.albumName);
            }
        }
    }
}

