package info.guardianproject.pixelknot.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import info.guardianproject.pixelknot.App;
import info.guardianproject.pixelknot.ObservableArrayList;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.StegoEncryptionJob;
import info.guardianproject.pixelknot.StegoJob;
import info.guardianproject.pixelknot.UIHelpers;

public class OutboxAdapter extends RecyclerView.Adapter<OutboxViewHolder> implements ObservableArrayList.OnChangeListener {

    private final ArrayList<StegoEncryptionJob> mEncryptionJobs;

    public interface OutboxAdapterListener {
        void onOutboxItemClicked(StegoEncryptionJob job);
    }

    private final Context mContext;
    private OutboxAdapterListener mListener;

    public OutboxAdapter(Context context) {
        super();
        mContext = context;
        mEncryptionJobs = new ArrayList<>();

        // Listen to changes in underlying data
        App.getInstance().getJobs().setOnChangeListener(this);
        updateList();
    }

    private void updateList() {
        mEncryptionJobs.clear();
        for (StegoJob job : App.getInstance().getJobs()) {
            if (job instanceof StegoEncryptionJob)
                mEncryptionJobs.add((StegoEncryptionJob) job);
        }
        notifyDataSetChanged();
    }

    public void setListener(OutboxAdapterListener listener) {
        mListener = listener;
    }

    @Override
    public void onListChanged(ObservableArrayList list) {
        updateList();
    }

    @Override
    public OutboxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.outbox_item, parent, false);
        return new OutboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OutboxViewHolder holder, int position) {
        StegoEncryptionJob job = mEncryptionJobs.get(position);
        holder.itemView.clearAnimation();
        holder.mJob = job;
        holder.mRootView.setOnClickListener(new ItemClickListener(job));
        holder.mPhoto.setRounding(1f);
        int viewSize = UIHelpers.dpToPx(180, mContext);
        Picasso.with(mContext).load(job.getBitmapFile())
                .resize(viewSize, viewSize)
                .centerCrop()
                .into(holder.mPhoto);
        holder.mProgressText.setText("" + job.getProgressPercent() + "%");
        holder.mStatusText.setText(R.string.tap_to_send);
        holder.mProgress.setMax(100);
        holder.mProgress.setProgress(job.getProgressPercent());
        updateBasedOnStatus(holder, job);
        job.setOnProgressListener(new StegoEncryptionJob.OnProgressListener() {
            private OutboxViewHolder viewHolder;
            public StegoEncryptionJob.OnProgressListener init(OutboxViewHolder viewHolder) {
                this.viewHolder = viewHolder;
                return this;
            }

            @Override
            public void onProgressUpdate(final StegoEncryptionJob job, final int percent) {
                viewHolder.itemView.post(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.mProgressText.setText("" + percent + "%");
                        viewHolder.mProgress.setProgress(percent);
                        updateBasedOnStatus(viewHolder, job);
                    }
                });
            }
        }.init(holder));
    }

    private void updateBasedOnStatus(OutboxViewHolder holder, StegoEncryptionJob job) {
        if (job.getProcessingStatus() == StegoJob.ProcessingStatus.ERROR) {
            holder.mProgressText.setVisibility(View.GONE);
            holder.mLayoutDone.setVisibility(View.GONE);
            holder.mLayoutError.setVisibility(View.VISIBLE);
        } else if (job.getProcessingStatus() == StegoJob.ProcessingStatus.EMBEDDED_SUCCESSFULLY || job.getProgressPercent() == 100) {
            holder.mProgressText.setVisibility(View.GONE);
            holder.mLayoutDone.setVisibility(View.VISIBLE);
            holder.mLayoutError.setVisibility(View.GONE);
        } else {
            holder.mProgressText.setVisibility(View.VISIBLE);
            holder.mLayoutDone.setVisibility(View.GONE);
            holder.mLayoutError.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mEncryptionJobs.size();
    }

    private class ItemClickListener implements View.OnClickListener {
        private final StegoEncryptionJob mJob;

        public ItemClickListener(StegoEncryptionJob job) {
            mJob = job;
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onOutboxItemClicked(mJob);
            }
        }
    }
}

