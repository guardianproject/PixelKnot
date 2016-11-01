package info.guardianproject.pixelknot;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import info.guardianproject.f5android.stego.StegoProcessThread;

class StegoProcessor {
    private static final boolean LOGGING = false;
    private static final String LOGTAG = "StegoProcessor";

    private final ArrayList<StegoProcessThread> threads;
    private boolean mStop = false;

    private static final String LOG = StegoProcessor.class.getName();

    public StegoProcessor(Context ctx) {
        threads = new ArrayList<>();
        Thread mThisThread = new Thread() {
            @Override
            public void run() {
                super.run();
                Thread.currentThread().setName(LOGTAG);
                while (!mStop) {
                    try {
                        StegoProcessThread t = null;
                        synchronized (threads) {
                            if (threads.size() > 0)
                                t = threads.remove(0);
                        }
                        if (t != null) {
                            if (LOGGING)
                                Log.d(LOGTAG, "Starting new thread " + t.getId());
                            t.start();
                            t.join();
                        } else {
                            if (LOGGING)
                                Log.d(LOGTAG, "Nothing to do, sleeping awhile");
                            sleep(2000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mThisThread.start();
    }

    public void addThread(StegoProcessThread thread) {
        synchronized (threads) {
            threads.add(thread);
        }
    }

    public void destroy() {
        mStop = true;
    }

}

