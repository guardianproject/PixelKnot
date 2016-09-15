package info.guardianproject.pixelknot;


import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShareChooserDialog extends BottomSheetDialogFragment {
    private RecyclerView mRecyclerView;
    private ArrayList<ResolveInfo> mTargets;
    private int mParentWidth;

    public final static Set<String> gSafeApps;
    static {
        // package names from appbrain.com
        HashSet<String> activities = new HashSet<String>();
        activities.add("com.twitter.android"); //, ActivityNames.TWITTER);
        activities.add("com.facebook.katana"); //, ActivityNames.FACEBOOK);
        activities.add("com.google.android.gm"); //, ActivityNames.GMAIL);
        activities.add("com.android.bluetooth"); //, ActivityNames.BLUETOOTH);
        activities.add("com.yahoo.mobile.client.android.flickr"); //, ActivityNames.FLICKR);
        activities.add("com.dropbox.android"); //, ActivityNames.DROPBOX);
        activities.add("com.bumptech.bumpga"); //, ActivityNames.BUMP);
        activities.add("com.google.android.apps.docs"); //, ActivityNames.DRIVE);
        activities.add("com.google.android.apps.plus"); //, ActivityNames.GOOGLE_PLUS);
        activities.add("com.instagram.android"); //, ActivityNames.INSTAGRAM);
        activities.add("com.tumblr"); //, ActivityNames.TUMBLR);
        activities.add("org.wordpress.android"); //, ActivityNames.WORDPRESS);
        //activities.add("com.skype.raider"); //, ActivityNames.SKYPE);
        activities.add("com.google.android.email"); //, ActivityNames.EMAIL);
        activities.add("com.htc.android.mail"); //, ActivityNames.EMAIL);
        activities.add("com.android.email"); //, ActivityNames.EMAIL);
        gSafeApps = Collections.unmodifiableSet(activities);
    }

    public interface ShareChooserDialogListener {
        void onItemSelected(ResolveInfo ri);
    }
    private ShareChooserDialogListener mListener;

    public ShareChooserDialog() {
        super();
    }

    public void setListener(ShareChooserDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = Uri.parse(getArguments().getString("uri"));
        mParentWidth = getArguments().getInt("width", 640);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/jpeg");

        mTargets = new ArrayList<ResolveInfo>();
        List<ResolveInfo> resInfo = App.getInstance().getPackageManager().queryIntentActivities(intent, 0);
        if (resInfo != null && resInfo.size() > 0) {
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (!packageName.equals(App.getInstance().getPackageName())) // Remove ourselves
                {
                    mTargets.add(resolveInfo);
                }
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogINterface) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(
                        mParentWidth - UIHelpers.dpToPx(0, getContext()), // Set margins here!
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.share_chooser, container, false);
        setupView(v);
        return v;
    }

    private void setupView(View v) {
        mRecyclerView = (RecyclerView)v.findViewById(R.id.recycler_view_apps);

        // Figure out how manby columns that fit in the given parent width. Give them 100dp.
        int appWidth = UIHelpers.dpToPx(80, getContext());
        final int nCols = (mParentWidth - /* padding */ UIHelpers.dpToPx(8, getContext())) / appWidth;
        GridLayoutManager glm = new GridLayoutManager(getContext(), nCols);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mRecyclerView.getAdapter() != null) {
                    if (mRecyclerView.getAdapter().getItemViewType(position) == 1) {
                        return nCols;
                    }
                    return 1;
                }
                return 0;
            }
        });
        mRecyclerView.setLayoutManager(glm);


        class VH extends RecyclerView.ViewHolder {
            public final ImageView icon;
            public final TextView label;

            public VH(View itemView) {
                super(itemView);
                icon = (ImageView) itemView.findViewById(R.id.ivShare);
                label = (TextView) itemView.findViewById(R.id.tvShare);
            }
        }
        mRecyclerView.setAdapter(new RecyclerView.Adapter<VH>() {

            private ArrayList<Object> mIntents;

            RecyclerView.Adapter init(List<ResolveInfo> targetedShareIntents) {
                mIntents = new ArrayList<Object>();
                ArrayList<ResolveInfo> safe = new ArrayList<ResolveInfo>();
                ArrayList<ResolveInfo> unsafe = new ArrayList<ResolveInfo>();
                for (ResolveInfo ri : targetedShareIntents) {
                    if (gSafeApps.contains(ri.activityInfo.packageName)) {
                        safe.add(ri);
                    } else {
                        unsafe.add(ri);
                    }
                }
                if (safe.size() > 0) {
                    mIntents.add(getString(R.string.chooser_share_safe));
                    mIntents.addAll(safe);
                }
                if (unsafe.size() > 0) {
                    mIntents.add(getString(R.string.chooser_share_unsafe));
                    mIntents.addAll(unsafe);
                }
                return this;
            }

            @Override
            public int getItemViewType(int position) {
                if (mIntents.get(position) instanceof String)
                    return 1;
                return 0;
            }

            @Override
            public VH onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate((viewType == 1) ? R.layout.share_header_item : R.layout.share_item, parent, false);
                return new VH(view);
            }

            @Override
            public void onBindViewHolder(VH holder, int position) {
                if (getItemViewType(position) == 1) {
                    holder.label.setText((CharSequence)mIntents.get(position));
                    return;
                }
                final ResolveInfo ri = (ResolveInfo) mIntents.get(position);
                holder.icon.setImageDrawable(ri.loadIcon(App.getInstance().getPackageManager()));
                holder.label.setText(ri.loadLabel(App.getInstance().getPackageManager()));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null)
                            mListener.onItemSelected(ri);
                        dismiss();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mIntents.size();
            }
        }.init(mTargets));

    }

    public static void createChooser(CoordinatorLayout rootView, final AppCompatActivity parent, final int requestCode, final Uri uri) {
        ShareChooserDialog d = new ShareChooserDialog();
        d.setListener(new ShareChooserDialog.ShareChooserDialogListener() {
            @Override
            public void onItemSelected(final ResolveInfo ri) {
                if (gSafeApps.contains(ri.activityInfo.packageName) || App.getInstance().getSettings().skipUnsafeShareInfo()) {
                    startActivityForResolveInfo(ri);
                } else {
                    // Show info dialog
                    final AlertDialog.Builder alert = new AlertDialog.Builder(parent).setTitle(R.string.unsafe_share_title).setMessage(R.string.unsafe_share);
                    final View view = LayoutInflater.from(parent).inflate(R.layout.dialog_unsafe_share, null, false);
                    alert.setView(view);
                    alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CheckBox cb = (CheckBox) view.findViewById(R.id.cbDontShowAgain);
                            if (cb.isChecked()) {
                                App.getInstance().getSettings().setSkipUnsafeShareInfo(true);
                            }
                            startActivityForResolveInfo(ri);
                        }
                    });
                    alert.show();
                }
            }

            private void startActivityForResolveInfo(ResolveInfo ri) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setType("image/jpeg");
                ComponentName name = new ComponentName(ri.activityInfo.applicationInfo.packageName,
                        ri.activityInfo.name);
                intent.setComponent(name);
                parent.startActivityForResult(intent, requestCode);
            }
        });
        Bundle args = new Bundle();
        args.putString("uri", uri.toString());
        args.putInt("width", rootView.getWidth());
        d.setArguments(args);
        d.show(parent.getSupportFragmentManager(), "Share");
    }
}