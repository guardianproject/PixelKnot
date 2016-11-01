package info.guardianproject.pixelknot;

import android.content.Context;

interface IStegoThreadHandler {
    Context getContext();
    void onJobCreated(StegoJob stegoJob);
    void onJobDone(StegoJob stegoJob);
}
