package co.seoulmate.android.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.github.clans.fab.FloatingActionMenu;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.seoulmate.android.app.activities.BoardDetailActivity;
import co.seoulmate.android.app.activities.DispatchActivity;
import co.seoulmate.android.app.activities.FeedDetailActivity;
import co.seoulmate.android.app.activities.NewPostCatActivity;
import co.seoulmate.android.app.activities.NotificationActivity;
import co.seoulmate.android.app.activities.ProfileActivity;
import co.seoulmate.android.app.fragments.BoardFragment;
import co.seoulmate.android.app.fragments.FeedFragment;
import co.seoulmate.android.app.fragments.LeadershipFragment;
import co.seoulmate.android.app.helpers.PreferencesHelper;
import co.seoulmate.android.app.utils.ModelUtils;
import co.seoulmate.android.app.utils.UserUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private static final String LOG = MainActivity.class.getSimpleName();
    private static final int LOGIN_REQUEST = 0;
    public static final int LOGIN_REQUEST_FACEBOOK = 3;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Toolbar toolbar;
    private FloatingActionMenu postMenu;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG, "onCreate");
        setUpToolbar();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        postMenu = (FloatingActionMenu) findViewById(R.id.floatingMenu);
        createCustomAnimation();
        final com.github.clans.fab.FloatingActionButton newBoardFab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addBoardPost);
        final com.github.clans.fab.FloatingActionButton newQuestionFab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.newQuestion);

        newBoardFab.setOnClickListener(clickListener);
        newQuestionFab.setOnClickListener(clickListener);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            onClickFab("http://seoulmateapp.co");
            return true;
        } else if ( id == R.id.action_logout) {
            if(ParseUser.getCurrentUser() != null) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {

                        startActivity(new Intent(MainActivity.this, DispatchActivity.class));
                        finish();
                    }
                });

            }
            return true;
        } else if (id == R.id.action_notification) {

            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onClickFab(String link) {

        if(link == null)
            return;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        if (canResolveIntent(browserIntent)) {
            startActivity(browserIntent);
        }

    }

    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = getPackageManager()
                .queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0 :
                    return BoardFragment.newInstance(position);
                case 1:
                    return FeedFragment.newInstance(position);
                case 2:
                    return LeadershipFragment.newInstance(position);
                default:
                    return BoardFragment.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.board);
                case 1:
                    return getString(R.string.q_a);
                case 2:
                    return getString(R.string.leaderboard);
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG, "result code : " + resultCode + " requestCode : " + requestCode);
        if(resultCode == RESULT_OK) {
            Log.d(LOG, "result ok!");
            if(requestCode == LOGIN_REQUEST_FACEBOOK) {
                Log.d(LOG,"result facebook_login!");
                getEmailFromFacebook();
                linkFacebookWithUser();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d(LOG,"result cancelled!");

        } else if (resultCode == RESULT_FIRST_USER) {
            Log.d(LOG,"result first_user!");

        }
    }

    private void getEmailFromFacebook() {

        //TODO: Add Parse User link code
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        if(ParseUser.getCurrentUser().isNew()) {
            Log.d(LOG,"new user");
            GraphRequest request = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            if (object == null) {
                                Log.d(LOG,"object is null => " + response.getError());
                                return;
                            }
                            saveUser(object);
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,first_name,last_name,name,picture,cover,email,birthday,locale,gender,link,timezone");
            request.setParameters(parameters);
            request.executeAsync();
        } else {

            Log.d(LOG,"exisiting user ");
            GraphRequest request = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            if (object == null) {
                                Log.d(LOG,"object is null => " + response.getError());
                                return;
                            }

                            saveUser(object);
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,first_name,last_name,name,picture,cover,email,birthday,locale,gender,link,timezone");
            request.setParameters(parameters);
            request.executeAsync();

        }
    }

    private void linkFacebookWithUser() {
        final ParseUser user = ParseUser.getCurrentUser();
        if (user == null)
            return;
        List<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("user_friends");
        permissions.add("email");
        permissions.add("user_birthday");
        permissions.add("user_education_history");

        if (!ParseFacebookUtils.isLinked(user)) {
            ParseFacebookUtils.linkWithReadPermissionsInBackground(user, MainActivity.this, permissions, new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (ParseFacebookUtils.isLinked(user)) {
                        Log.d(LOG, "Woohoo, user logged in with Facebook!");
                    }
                }
            });
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG, "onResume");
        AppEventsLogger.activateApp(this);
//        handleNotifications();
    }

    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ParseUser.getCurrentUser() != null) {
            Log.d(LOG, "onStart");
            getEmailFromFacebook();
            linkFacebookWithUser();
            updateUser();

        }

    }

    private void setUpToolbar() {

        ParseUser user = ParseUser.getCurrentUser();
        if(user == null) {
            finish();
            return;
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView scoreView = (TextView) findViewById(R.id.score);
        int points = user.getInt(ModelUtils.POINTS);
        scoreView.setText(String.valueOf(points));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        final RoundedImageView avatarView = (RoundedImageView) toolbar.findViewById(R.id.profilePicMain);
        avatarView.setImageResource(R.drawable.avatar_6_raster);
        ParseFile image = user.getParseFile(UserUtils.PROFILE_PIC);
        if ( image!= null &&
                image.getUrl() != null) {
            Glide.with(this)
                    .load(image.getUrl())
                    .centerCrop()
                    .placeholder(R.drawable.avatar_1_raster)
                    .into(avatarView);

        }
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileActivity(MainActivity.this, avatarView);
            }
        });
        String name = "";
        if(user != null) {
            name = user.getString(ModelUtils.FIRST_NAME);
        }
        TextView nameView  = (TextView) toolbar.findViewById(R.id.userNameMain);
        nameView.setText(name);
        nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileActivity(MainActivity.this,avatarView);
            }
        });


    }


    private void startProfileActivity(Activity activity, View toolbar) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void saveUser(JSONObject object) {
        String email = "";
        email = object.optString("email");
//        Log.d(LOG, "email : " + email + " object : " + object.toString());
        ParseUser.getCurrentUser().setEmail(email);
        ParseUser.getCurrentUser().put(ModelUtils.FIRST_NAME,
                object.optString("first_name"));
        ParseUser.getCurrentUser().put(ModelUtils.LAST_NAME,
                object.optString("last_name"));
        ParseUser.getCurrentUser().put(ModelUtils.DISPLAY_NAME,
                object.optString("name"));
        Log.d(LOG, "User id : " + object.optString("id"));
        ParseUser.getCurrentUser().put("id", object.optString("id"));
        ParseUser.getCurrentUser().put("fbId",object.optString("id"));
        ParseUser.getCurrentUser().put("facebookId",object.optString("id"));
        ParseUser.getCurrentUser().put("locale",object.optString("locale"));
        ParseUser.getCurrentUser().put("link",object.optString("link"));
        try {
            JSONObject picture = object.getJSONObject("picture");
            JSONObject data = picture.getJSONObject("data");
            String pictureUrl = data.optString("url");
            ParseUser.getCurrentUser().put("profilePicLink",pictureUrl);
        } catch (JSONException e) {
            Log.d(LOG,"no picture found for user");
        }
        ParseUser.getCurrentUser().put("gender",object.optString("gender"));
        ParseUser.getCurrentUser().put("birthday",object.optString("birthday"));

        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(LOG, "save user");
                } else {
                    Log.d(LOG, "save user exception : " + e.getMessage());

                }
            }
        });
    }

    private void createCustomAnimation() {


        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(postMenu.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(postMenu.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(postMenu.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(postMenu.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                postMenu.getMenuIconView().setImageResource(postMenu.isOpened()
                        ? R.drawable.ic_close_white_24dp : R.drawable.ic_add_white_24dp);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        postMenu.setIconToggleAnimatorSet(set);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.addBoardPost:
                    Intent newBoardIntent = new Intent(MainActivity.this, NewPostCatActivity.class);
                    newBoardIntent.putExtra(ModelUtils.BOARD,true);
                    startActivity(newBoardIntent);
                    break;
                case R.id.newQuestion:
                    Intent newQuestionIntent = new Intent(MainActivity.this, NewPostCatActivity.class);
                    newQuestionIntent.putExtra(ModelUtils.BOARD,false);
                    startActivity(newQuestionIntent);
                    break;

            }
        }
    };

    private void updateUser() {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.put(ModelUtils.USER, ParseUser.getCurrentUser());
        currentInstallation.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if(e == null)
                    Log.d(LOG,"user installation saved");
            }
        });
    }

    private void handleNotifications() {
        Log.d(LOG,"handle notifications");
        Boolean isBoard = PreferencesHelper.getBoardNotiStatus(this);
        String boardObjectId = PreferencesHelper.getBoardId(this);
        Log.d(LOG,"board noti : " + isBoard + "|" +boardObjectId);
        if(isBoard && boardObjectId != "default") {
            PreferencesHelper.writeToPreferenceBoardNotiStatus(this, false);
            Intent intent = new Intent(MainActivity.this, BoardDetailActivity.class);
            intent.putExtra(ModelUtils.OBJECT_ID, boardObjectId);
            startActivity(intent);
            return;
        }
        Boolean isQuestion = PreferencesHelper.getQuestionNotiStatus(this);
        String questionObjectId = PreferencesHelper.getQuestionId(this);
        Log.d(LOG,"feed noti : " + isBoard + "|" +questionObjectId);
        if(isQuestion && questionObjectId != "default") {
            PreferencesHelper.writeToPreferenceQuestionNotiStatus(this, false);
            Intent intent = new Intent(MainActivity.this, FeedDetailActivity.class);
            intent.putExtra(ModelUtils.OBJECT_ID, boardObjectId);
            startActivity(intent);
            return;
        }

    }
}
