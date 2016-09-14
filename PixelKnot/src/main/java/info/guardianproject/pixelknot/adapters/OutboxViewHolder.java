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
    View mRootView;
    RoundedImageView mPhoto;
    TextView mProgressText;
    TextView mStatusText;
    CircularProgress mProgress;
    View mLayoutDone;
    View mLayoutError;
    TextView mTimestamp;

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
