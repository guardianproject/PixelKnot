package info.guardianproject.pixelknot.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import info.guardianproject.pixelknot.R;

public class PhotoViewHolder extends RecyclerView.ViewHolder {
    final View mRootView;
    final ImageView mPhoto;

    public PhotoViewHolder(View itemView) {
        super(itemView);
        this.mRootView = itemView;
        this.mPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
    }
}
