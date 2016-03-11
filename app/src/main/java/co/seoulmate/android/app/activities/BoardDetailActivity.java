package co.seoulmate.android.app.activities;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.google.android.gms.analytics.HitBuilders;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.ocpsoft.prettytime.PrettyTime;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import co.seoulmate.android.app.AnalyticsTrackers;
import co.seoulmate.android.app.R;
import co.seoulmate.android.app.helpers.AConstants;
import co.seoulmate.android.app.model.Board;
import co.seoulmate.android.app.utils.ModelUtils;
import co.seoulmate.android.app.utils.UserUtils;

public class BoardDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = BoardDetailActivity.class.getSimpleName();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBoardDetail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String objectId = getIntent().getStringExtra(ModelUtils.OBJECT_ID);
        getBoardPostFromDb(objectId);
    }

    private void getBoardPostFromDb(final String objectId) {
        progressBar.setVisibility(View.VISIBLE);
        Board.createSingleLocalQuery(objectId).getInBackground(objectId, new GetCallback<Board>() {
            @Override
            public void done(final Board object, ParseException e) {
                if (e == null && object != null) {
                    setupView(object);

                } else {

                    Log.d(LOG_TAG, "exception while fetching Board post from local db - " + e.getMessage());
                    getBoardPostFromParse(objectId);
                }
            }
        });

    }

    private void getBoardPostFromParse(final String objectId) {
        progressBar.setVisibility(View.VISIBLE);
        Board.createSingleQuery(objectId).getInBackground(objectId, new GetCallback<Board>() {
            @Override
            public void done(final Board object, ParseException e) {
                if (e == null && object != null) {

                    object.pinInBackground(ModelUtils.BOARD_PIN, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            setupView(object);
                        }
                    });
                    progressBar.setVisibility(View.GONE);

                } else {

                    Log.d(LOG_TAG, "exception while fetching Board post from parse - " + e.getMessage());
                    progressBar.setVisibility(View.GONE);

                }

            }
        });

    }

    private void setupView(final Board object) {

        TextView content = (TextView) findViewById(R.id.boardDetailContent);
        ImageView boardImage = (ImageView) findViewById(R.id.boardImage);
        TextView voteCount = (TextView) findViewById(R.id.voteCount);
        ImageView voteCountImg = (ImageView) findViewById(R.id.VoteCountImg);

        TextView postBy = (TextView) findViewById(R.id.postedBy);
        net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout collapsingToolbarLayout
                = (net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout ) findViewById(R.id.toolbar_layout);

        collapsingToolbarLayout.setTitle(object.getTitle());
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.SeoulMate_CollapsingToolBar_Title);
        sendAnalytic(object.getTitle());
        RoundedImageView profilePic = (RoundedImageView) findViewById(R.id.boardDetailProfilePic);
        FloatingActionButton linkFab = (FloatingActionButton) findViewById(R.id.fab);
        progressBar = (ProgressBar) findViewById(R.id.progressBoardDetail);
        getSupportActionBar().setTitle(object.getTitle());


        if(object.getlink() == null) {
            linkFab.setVisibility(View.INVISIBLE);
        }
        linkFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onClickFab(object.getlink());
            }
        });

        content.setText(object.getContent());
        PrettyTime p = new PrettyTime();
        Date now = new Date(System.currentTimeMillis());
        long diff = now.getTime() - object.getCreatedAt().getTime();
        String dateTime = p.format(new Date(System.currentTimeMillis() - diff));

        if(object.getUser() != null) {

            postBy.setText(String.format(Locale.US, getString(R.string.posted_by_with_time),
                    object.getUser().getString(ModelUtils.FIRST_NAME), dateTime));
            ParseFile image = object.getUser().getParseFile(UserUtils.PROFILE_PIC);
            if ( image!= null &&
                    image.getUrl() != null) {
                Glide.with(this)
                        .load(image.getUrl())
                        .centerCrop()
                        .placeholder(R.drawable.avatar_1_raster)
                        .into(profilePic);

            } else {

                profilePic.setImageResource(R.drawable.avatar_1_raster);
            }
        } else {
            postBy.setText(dateTime);
            profilePic.setImageResource(R.drawable.avatar_1_raster);

        }


        ViewCompat.animate(profilePic)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setStartDelay(500)
                .scaleX(1)
                .scaleY(1)
                .start();

        if(object.getVoters() != null) {
            voteCount.setText(String.valueOf(object.getVoters().size()));
        } else {
            voteCount.setText(String.valueOf(0));
        }


        content.setMovementMethod(ScrollingMovementMethod.getInstance());

        ParseFile mainImage = object.getPhoto();
        if ( mainImage!= null &&
                mainImage.getUrl() != null) {
            Glide.with(this)
                    .load(mainImage.getUrl())
                    .centerCrop()
                    .placeholder(R.drawable.place_holder_image)
                    .into(boardImage);

        } else {

            switch (object.getCategory()) {

                case 0 :
                    boardImage.setImageResource(R.drawable.korean_language);
                    break;
                case 1 :
                    boardImage.setImageResource(R.drawable.news);
                    break;
                case 2 :
                    boardImage.setImageResource(R.drawable.media);
                    break;
                case 3 :
                    boardImage.setImageResource(R.drawable.jobs);
                    break;
                case 4 :
                    boardImage.setImageResource(R.drawable.scholarship_uni);
                    break;
                case 5 :
                    boardImage.setImageResource(R.drawable.tourism);
                    break;
                case 6 :
                    boardImage.setImageResource(R.drawable.history_culture);
                    break;
                case 7 :
                    boardImage.setImageResource(R.drawable.social);
                    break;
            }
        }

        progressBar.setVisibility(View.GONE);

        voteCountImg.setOnClickListener(new VoteClickListener(object, voteCount));
        voteCount.setOnClickListener(new VoteClickListener(object,voteCount));

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


    private void handleVoteUp(final Board q, TextView voteTextView,ParseUser cUser) {

        if (q.getVoters() != null && !q.getVoters().contains(cUser)) {
            int newVoteCount = q.getVoters().size() + 1;
            voteTextView.setText(String.valueOf(newVoteCount));
            q.addVoter(cUser, q);
            q.pinInBackground(ModelUtils.BOARD_PIN, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {

                        q.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d(LOG_TAG, "updated vote count and object");
                                } else {
                                    Log.d(LOG_TAG, "error occured while updating voters :"
                                            + e.getMessage());
                                }
                            }
                        });
                    } else {
                        Log.d(LOG_TAG, "error occured while updating voters locally :"
                                + e.getMessage());
                    }
                }
            });
        } else if (q.getVoters() != null && q.getVoters().contains(cUser)) {

            Snackbar.make(voteTextView, getResources().getString(R.string.voting_already), Snackbar.LENGTH_LONG)
                    .setAction("OK", null).show();

        }else if (q.getVoters() == null) {
            Log.d(LOG_TAG,"add first voters");
            int newVoteCount = 1;
            voteTextView.setText(String.valueOf(newVoteCount));
            q.addVoter(cUser, q);
            q.pinInBackground(ModelUtils.BOARD_PIN, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        q.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d(LOG_TAG, "updated vote count and object");
                                } else {
                                    Log.d(LOG_TAG, "error occured while updating voters :"
                                            + e.getMessage());
                                }
                            }
                        });
                    } else {
                        Log.d(LOG_TAG, "error occured while updating voters locally :"
                                + e.getMessage());
                    }
                }
            });


        }
    }

    private class VoteClickListener implements View.OnClickListener {

        private TextView textView;
        private Board board;
        private VoteClickListener(Board object, TextView view) {
            board= object;
            textView = view;

        }
        @Override
        public void onClick(View v) {
            handleVoteUp(board, textView, ParseUser.getCurrentUser());
        }
    }

    private void sendAnalytic(String boardTitle) {

        try {
            AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP).setScreenName(AConstants.SCREEN_BOARD+"_"+boardTitle);
            AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP).send(new HitBuilders.EventBuilder()
                    .setCategory(AConstants.SCREEN_BOARD_DETAIL)
                    .setAction(AConstants.SCREEN_BOARD_DETAIL)
                    .setLabel(boardTitle)
                    .build());
            AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP).send(new HitBuilders.ScreenViewBuilder().build());

        } catch (IllegalStateException e) {
        }
    }

}
