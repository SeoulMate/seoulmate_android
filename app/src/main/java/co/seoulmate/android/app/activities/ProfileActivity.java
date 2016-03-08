package co.seoulmate.android.app.activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import co.seoulmate.android.app.R;
import co.seoulmate.android.app.utils.ModelUtils;
import co.seoulmate.android.app.utils.UserUtils;

public class ProfileActivity extends AppCompatActivity {

    private static final String LOG = ProfileActivity.class.getSimpleName();

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.send_message_later), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = getIntent().getStringExtra(ModelUtils.USER_ID);
        if( userId != null) {
            getUser(userId);
        } else {
            getCurrentUser(userId);
        }
    }

    private void getUser(String objectId) {


        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.getInBackground(objectId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e == null) {
                    setView(user);
                } else {
                    Log.d(LOG,"user not found - e :" + e.getMessage());

                }
            }
        });

    }


    private void getCurrentUser(String objectId) {

        setView(ParseUser.getCurrentUser());
    }

    private void setView(ParseUser user) {

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        if(user.getString(ModelUtils.DISPLAY_NAME) != null)
            collapsingToolbarLayout.setTitle(user.getString(ModelUtils.DISPLAY_NAME));
        else
            collapsingToolbarLayout.setTitle(user.getString(ModelUtils.FIRST_NAME));

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.SeoulMate_CollapsingToolBar_Title);
        ImageView cover = (ImageView)findViewById(R.id.profileCover);
        ImageView profilePic = (ImageView) findViewById(R.id.profilePic);
        TextView points = (TextView) findViewById(R.id.points);
        points.setText(String.valueOf(user.getInt("points")));

        ParseFile image = user.getParseFile(UserUtils.PROFILE_PIC);
        if ( image!= null && image.getUrl() != null) {

            Picasso.with(this)
                    .load(image.getUrl())
                    .placeholder(R.drawable.avatar_1_raster)
                    .error(R.drawable.avatar_9_raster)
                    .into(profilePic);

        } else {
            profilePic.setImageResource(R.drawable.avatar_2_raster);
        }

        if (user.getString("cover") != null) {

            Picasso.with(this)
                    .load(user.getString("cover"))
                    .placeholder(R.drawable.media)
                    .error(R.drawable.media)
                    .into(cover);
        } else {

            getCoverPhoto(user);
        }



    }
    private void getCoverPhoto(ParseUser user) {

        GraphRequest request =  GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        if (object == null) {
                            Log.d(LOG, "object is null => " + response.getError());
                            return;
                        }
                        saveUser(object);
                        Log.v(LOG, response.toString());
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "cover");
        request.setParameters(parameters);
        request.executeAsync();

/*

        String URL = "https://graph.facebook.com/" + user.getString("fbId")+"?fields=cover&access_token=" +AccessToken.getCurrentAccessToken().getToken();
        Log.d(LOG,URL);

        Bundle parameters = new Bundle();
        parameters.putString("fields", "cover");

       GraphRequest coverRequest =  new GraphRequest(
                AccessToken.getCurrentAccessToken(),
               user.getString("fbId"),
               parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        Log.d(LOG, response.toString());
                    }
                }
        );
//        coverRequest.setParameters(parameters);

        coverRequest.executeAsync();
*/


    }

    private void saveUser(JSONObject object) {

        try {
            JSONObject cover = object.getJSONObject("cover");
            String coverUrl = cover.optString("source");
            ParseUser.getCurrentUser().put("cover",coverUrl);
        } catch (JSONException e) {
            Log.d(LOG,"no picture found for user");
        }

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
}
