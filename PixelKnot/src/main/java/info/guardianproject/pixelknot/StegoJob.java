package info.guardianproject.pixelknot;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import info.guardianproject.f5android.stego.StegoProcessThread;

public class StegoJob {
    private static final boolean LOGGING = false;
    private static final String LOGTAG = "StegoJob";

    private String mId;
    private Date mCreationDate;
    protected IStegoThreadHandler mThreadHandler;
    private ArrayList<StegoJobProcess> mProcesses;
    private int mMaxProgressTicks = 0;
    private int mCurrentProgressTick = 0;
    private int mCurrentProcess = 0;
    private StegoProcessThread mThread;

    // Status
    public enum ProcessingStatus {
        PROCESSING,
        ERROR,
        EMBEDDED_SUCCESSFULLY,
        EXTRACTED_SUCCESSFULLY
    };
    private ProcessingStatus mProcessingStatus = ProcessingStatus.PROCESSING;

    public StegoJob(IStegoThreadHandler threadHandler) {
        mThreadHandler = threadHandler;
        mProcesses = new ArrayList<>();
        mId = UUID.randomUUID().toString();
        mCreationDate = new Date();
    }

    public String getId() {
        return mId;
    }

    public Date getCreationDate() {
        return mCreationDate;
    }

    private class StegoJobProcess {
        private final Runnable mRunnable;
        private final int mNumberOfProgressTicks;

        public StegoJobProcess(Runnable runnable, int numberOfProgressTicks) {
            mRunnable = runnable;
            mNumberOfProgressTicks = numberOfProgressTicks;
        }
    }

    protected void addProcess(Runnable runnable, int numberOfProgressTicks) {
        mProcesses.add(new StegoJobProcess(runnable, numberOfProgressTicks));
        mMaxProgressTicks += numberOfProgressTicks;
    }

    public int getMaxProgress() {
        return mMaxProgressTicks;
    }

    protected void Run() {
        mThread = new StegoProcessThread() {
            @Override
            public void run() {
                super.run();
                setProcessingStatus(ProcessingStatus.PROCESSING);

                // Figure out where we are progress-wise
                mCurrentProgressTick = 0;
                for (int i = 0; i < mCurrentProcess; i++) {
                    mCurrentProgressTick += mProcesses.get(i).mNumberOfProgressTicks;
                }
                while (mCurrentProcess < mProcesses.size() && !isInterrupted()) {
                    Runnable r = mProcesses.get(mCurrentProcess).mRunnable;
                    r.run();
                    if (!isInterrupted())
                        mCurrentProcess++;
                }
                if (isInterrupted()) {
                    setProcessingStatus(ProcessingStatus.ERROR);
                }
                mThreadHandler.onJobDone(StegoJob.this);
            }
        };
        mThreadHandler.onJobCreated(this);
    }

    public StegoProcessThread getThread() {
        return mThread;
    }

    public StegoJob.ProcessingStatus getProcessingStatus() {
        return mProcessingStatus;
    }

    protected void abortJob() {
        // Interrupt the thread so no more processing is done on this thread
        try {
            getThread().requestInterrupt();
        } catch (Exception ex) {}
    }

    protected void setProcessingStatus(ProcessingStatus processingStatus) {
        mProcessingStatus = processingStatus;
        if (mProcessingStatus == ProcessingStatus.ERROR) {
            mCurrentProcess = 0; // reset
            mCurrentProgressTick = 0;
        }
    }

    protected void onProgressTick() {
        mCurrentProgressTick++;
    }

    public int getProgressPercent() {
        if (mMaxProgressTicks == 0)
            return 0;
        return (int)(100f * (float)mCurrentProgressTick / (float)mMaxProgressTicks);
    }

    public void cleanup() {
    }
}
