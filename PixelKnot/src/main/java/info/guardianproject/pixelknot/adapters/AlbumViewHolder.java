package info.guardianproject.pixelknot.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import info.guardianproject.pixelknot.R;

public class AlbumViewHolder extends RecyclerView.ViewHolder {
    View mRootView;
    TextView mAlbumName;
    TextView mAlbumCount;
    ImageView mAlbumThumbnail;

    public AlbumViewHolder(View itemView) {
        super(itemView);
        this.mRootView = itemView;
        this.mAlbumName = (TextView) itemView.findViewById(R.id.tvAlbumName);
        this.mAlbumCount = (TextView) itemView.findViewById(R.id.tvAlbumCount);
        this.mAlbumThumbnail = (ImageView) itemView.findViewById(R.id.ivAlbumThumbnail);
    }
}
