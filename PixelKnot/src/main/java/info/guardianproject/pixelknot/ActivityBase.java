package info.guardianproject.pixelknot;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * A common base class for activities, to support language handling
 */
public class ActivityBase extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.getInstance().setCurrentLanguageInContext(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        App.getInstance().setCurrentLanguageInConfig(newConfig);
        super.onConfigurationChanged(newConfig);
    }
}
