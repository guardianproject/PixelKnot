package info.guardianproject.pixelknot.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.StegoEncryptionJob;
import info.guardianproject.pixelknot.views.CircularProgress;
import info.guardianproject.pixelknot.views.RoundedImageView;

public class OutboxViewHolder extends RecyclerView.ViewHolder {
    StegoEncryptionJob mJob;
    final View mRootView;
    final RoundedImageView mPhoto;
    final TextView mProgressText;
    final TextView mStatusText;
    final CircularProgress mProgress;
    final View mLayoutDone;
    final View mLayoutError;
    private final TextView mTimestamp;

    public OutboxViewHolder(View itemView) {
        super(itemView);
        this.mRootView = itemView;
        this.mPhoto = (RoundedImageView) itemView.findViewById(R.id.ivPhoto);
        this.mProgressText = (TextView) itemView.findViewById(R.id.tvProgress);
        this.mProgress = (CircularProgress) itemView.findViewById(R.id.progress);
        this.mStatusText = (TextView) itemView.findViewById(R.id.tvStatus);
        this.mLayoutDone = itemView.findViewById(R.id.layout_done);
        this.mLayoutError = itemView.findViewById(R.id.layout_error);
        this.mTimestamp = (TextView)itemView.findViewById(R.id.tvTimestamp);
    }

    public RoundedImageView getPhotoView() {
        return mPhoto;
    }

    public StegoEncryptionJob getJob() { return mJob; }

    public TextView getStatusTextView() {
        return mStatusText;
    }

    public TextView getTimestampTextView() { return mTimestamp; }
}
