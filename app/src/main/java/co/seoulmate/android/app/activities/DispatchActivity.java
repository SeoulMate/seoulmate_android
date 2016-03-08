package co.seoulmate.android.app.activities;

import com.facebook.appevents.AppEventsLogger;
import com.parse.ui.ParseLoginDispatchActivity;

import co.seoulmate.android.app.MainActivity;

/**
 * Created by hassanabid on 3/1/16.
 */
public class DispatchActivity extends ParseLoginDispatchActivity {

    @Override
    protected Class<?> getTargetClass() {
        return MainActivity.class;
    }


}
