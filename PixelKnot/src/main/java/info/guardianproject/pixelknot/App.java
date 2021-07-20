package info.guardianproject.pixelknot;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;

public class App extends Application implements IStegoThreadHandler {
    private static App gInstance;

    public static App getInstance() {
        return gInstance;
    }

    private FileManager mFileManager;
    private ObservableArrayList<StegoJob> mJobs;
    private StegoProcessor mStegoProcessor;
    private NotificationCompat.Builder mNotification;
    private Settings mSettings;

    @Override
    public void onCreate() {
        gInstance = this;
        super.onCreate();
        mSettings = new Settings(this);
        mFileManager = new FileManager(this);
        mJobs = new ObservableArrayList<>();
    }

    public Settings getSettings() { return mSettings; }

    public FileManager getFileManager() {
        return mFileManager;
    }

    public ObservableArrayList<StegoJob> getJobs() {
        return mJobs;
    }

    public void storeJob(StegoJob job) {
        if (!mJobs.contains(job))
            mJobs.add(job);
    }

    public boolean forgetJob(StegoJob job, boolean callCleanup) {
        if (job != null) {
            mJobs.remove(job);
            if(job.getThread() != null && job.getThread().isAlive()) {
                job.getThread().interrupt();
            }
            if (callCleanup)
                job.cleanup();
            updateNotification(false);
            return true;
        }
        return false;
    }

    public StegoJob getJobById(String jobId) {
        for (StegoJob job : mJobs) {
            if (job.getId().contentEquals(jobId))
                return job;
        }
        return null;
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onJobCreated(StegoJob job) {
        if(job.getThread() != null) {
            if(mStegoProcessor == null) {
                mStegoProcessor = new StegoProcessor(this);
            }
            mStegoProcessor.addThread(job.getThread());
        }
    }

    @Override
    public void onJobDone(StegoJob job) {
        if (job.getProcessingStatus() == StegoJob.ProcessingStatus.EMBEDDED_SUCCESSFULLY) {
            updateNotification(true);
        }
    }

    private void updateNotification(boolean jobDone) {
        int nReady = 0;
        for (int i = 0; i < mJobs.size(); i++) {
            if (mJobs.get(i).getProcessingStatus() == StegoJob.ProcessingStatus.EMBEDDED_SUCCESSFULLY) {
                nReady++;
            }
        }
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nReady == 0) {
            nm.cancel(0);
        } else {
            if (mNotification == null) {
                mNotification = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                R.mipmap.ic_launcher))
                        .setAutoCancel(true)
                        .setContentText(getString(R.string.tap_to_send));
                Intent resumeIntent = new Intent(this, SendActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                resumeIntent.putExtra("showTab", 1);
                PendingIntent pi = PendingIntent.getActivity(this, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mNotification.setContentIntent(pi);
                mNotification.setAutoCancel(true);
            }
            if (jobDone)
                mNotification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            else
                mNotification.setDefaults(0);
            mNotification.setContentTitle(nReady == 1 ? getString(R.string.notification_message_ready) : getString(R.string.notification_messages_ready, nReady));
            nm.notify(0, mNotification.build());
        }
    }

    public void setCurrentLanguageInConfig(Configuration config)
    {
/*
        String language = "fa";
        Locale loc = new Locale(language);
        if (Build.VERSION.SDK_INT >= 17)
            config.setLocale(loc);
        else
            config.locale = loc;
        Locale.setDefault(loc);
*/
    }

    public void setCurrentLanguageInContext(Context context)
    {
/*
        Configuration config = new Configuration();
        setCurrentLanguageInConfig(config);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
*/
    }

}
