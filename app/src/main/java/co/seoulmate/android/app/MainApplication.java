package co.seoulmate.android.app;

import android.app.Activity;
import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

import co.seoulmate.android.app.R;

import co.seoulmate.android.app.model.ActivityBoard;
import co.seoulmate.android.app.model.Board;
import co.seoulmate.android.app.model.Feed;
import co.seoulmate.android.app.model.Notification;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by hassanabid on 3/1/16.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register ParseObject subclasses
//        ParseObject.registerSubclass(University.class);
//        ParseObject.registerSubclass(MyUniversity.class);
//        ParseObject.registerSubclass(Korean_Language.class);
//        ParseObject.registerSubclass(Post.class);
//        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(Feed.class);
        ParseObject.registerSubclass(Board.class);
        ParseObject.registerSubclass(co.seoulmate.android.app.model.Activity.class);
        ParseObject.registerSubclass(co.seoulmate.android.app.model.ActivityBoard.class);
//        ParseObject.registerSubclass(Feedback.class);
        ParseObject.registerSubclass(Notification.class);
//        ParseObject.registerSubclass(KoreanPhoto.class);
//        ParseObject.registerSubclass(ActivityKorean.class);
//        ParseObject.registerSubclass(MessageStack.class);
        ParseObject.registerSubclass(ActivityBoard.class);


        Parse.enableLocalDatastore(this);
        Parse.initialize(this, BuildConfig.PARSE_APPLICATION_ID, BuildConfig.PARSE_CLIENT_KEY);
        ParseFacebookUtils.initialize(this,MainActivity.LOGIN_REQUEST_FACEBOOK);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}
