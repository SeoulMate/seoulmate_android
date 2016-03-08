package co.seoulmate.android.app.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.seoulmate.android.app.R;
import co.seoulmate.android.app.adapters.FeedCommentAdapter;
import co.seoulmate.android.app.model.Activity;
import co.seoulmate.android.app.model.Feed;
import co.seoulmate.android.app.utils.CategoryUtils;
import co.seoulmate.android.app.utils.ModelUtils;
import co.seoulmate.android.app.utils.UserUtils;

public class FeedDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = FeedDetailActivity.class.getSimpleName();

    private TextView questionContent;
    private TextView showAnswers;
    private List<Activity> answers;
    private int mCategory;
    private Feed mQuestion;
    private Toolbar toolbar;
    private String mHashTag;
    private TextView mProgressText;
    private int mQuizSxize = 10;
    private boolean isShowingAnswers;
    private ProgressBar mProgressBar;
    private ProgressBar mAnswersProgress;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FeedCommentAdapter mAdapter;
    private RecyclerView answerRecyclerView;
    private TextView emptyTextView;
    private AppBarLayout appBarLayout;
    private ParseUser mCurrentUser;
    private Interpolator mLinearOutSlowInInterpolator;
    private static final int FOREGROUND_COLOR_CHANGE_DELAY = 750;
    private Runnable mMoveOffScreenRunnable;
    private Handler mHandler;
    private RoundedImageView profilePic;
    private TextView qWriter;
    private TextView timeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String objectId = getIntent().getStringExtra(ModelUtils.OBJECT_ID);
        mCategory = getIntent().getIntExtra(ModelUtils.CATNO,0);
        Log.d(LOG_TAG, "id " + objectId + " category :" + mCategory);
        Log.d(LOG_TAG, "mCategory:" + mCategory);
        setContentView(R.layout.activity_feed_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.Category_Title);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        answerRecyclerView = (RecyclerView) findViewById(R.id.recyclerviewScroll);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout_q);
        mAnswersProgress = (ProgressBar) findViewById(R.id.questionDetailProgress);
        questionContent = (TextView) findViewById(R.id.qContentDetail);
        emptyTextView = (TextView) findViewById(R.id.emptyAnswerView);
        profilePic = (RoundedImageView) findViewById(R.id.profilePicQUser);
        qWriter = (TextView) findViewById(R.id.questionWriter);
        timeView = (TextView) findViewById(R.id.time_view_q);

/*        ColorDrawable cd = new ColorDrawable(getResources()
                .getColor(ColorUtils.getMainIconColorDark(mCategory)));
        appBarLayout.setBackground(cd);*/
//        collapsingToolbarLayout.setContentScrimColor(ColorUtils.getMainIconColorDark(mCategory));

        findSingleLocalQuestion(objectId);
        mCurrentUser = ParseUser.getCurrentUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerDialog = createAnswerDialog(new EditText(FeedDetailActivity.this), null);
                answerDialog.show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    private void findSingleLocalQuestion(final String objectId) {

        Feed.createSingleLocalQuery(objectId).getInBackground(objectId, new GetCallback<Feed>() {
            @Override
            public void done(Feed object, ParseException e) {
                if (e == null && object != null) {
                    mQuestion = object;
                    updateFeedView(object, toolbar);
                    Log.d(LOG_TAG, "fetched feed from localdatastore");
                } else {
                    findSingleQuestion(objectId);
                }

            }
        });
    }

    private void findSingleQuestion(String objectId) {
        Feed.createSingleQuery(objectId).getInBackground(objectId, new GetCallback<Feed>() {
            @Override
            public void done(Feed object, ParseException e) {
                if (e == null && object != null) {
                    mQuestion = object;
                    updateFeedView(object, toolbar);
                    object.pinInBackground(ModelUtils.FEED_PIN);
                    Log.d(LOG_TAG, "fetched question from parse");
                } else {
                    Snackbar.make(questionContent, getResources().getString(R.string.something_went_wrong),
                            Snackbar.LENGTH_LONG)
                            .setAction("OK", null).show();
                    Log.d(LOG_TAG, "fetched question error : " + e.getMessage());
                }


            }
        });
    }

    private void updateFeedView(Feed q, Toolbar toolbar) {

//        handleDeleteOption();
        getHastTag(mQuestion);
        setQuestionView(mQuestion);
        setSupportActionBar(toolbar);
        String name = "N/A";
        if(mQuestion.getUser() != null)
         name = mQuestion.getUser().getString(ModelUtils.FIRST_NAME);

        setTitle(String.format(Locale.US,getResources().getString(R.string.posted_by),name));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        isShowingAnswers = false;
        getAnswerAdapter(answerRecyclerView);
        appBarLayout.setMinimumHeight(100);

    }

    private void getHastTag(Feed q) {

            mHashTag = CategoryUtils.getName(q.getCategory());
    }

    private void setQuestionView(Feed q) {

        if(q == null)
            return;

        questionContent.setText(q.getTitle());
        Log.d(LOG_TAG,"content size :" + q.getTitle().length());
//        collapsingToolbarLayout.setTitle(q.getTitle());
        PrettyTime p = new PrettyTime();
        Date now = new Date(System.currentTimeMillis());
        long diff = now.getTime() - q.getCreatedAt().getTime();
        timeView.setText(p.format(new Date(System.currentTimeMillis() - diff)));

        String writerName = "";
        if(q.getUser() != null) {
/*            q.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    qWriter.setText(String.format(Locale.US,
                            getResources().getString(R.string.answer_writer),
                            object.getString(ModelUtils.NAME)));
                }
            });*/
            qWriter.setText(q.getUser().getString(ModelUtils.FIRST_NAME));
            ParseFile image = q.getUser().getParseFile(UserUtils.PROFILE_PIC);
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
            qWriter.setText("N/A");
            profilePic.setImageResource(R.drawable.avatar_1_raster);

        }


        ViewCompat.animate(profilePic)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setStartDelay(500)
                .scaleX(1)
                .scaleY(1)
                .start();

        questionContent.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private int dp(int pixels) {
        return (int) getResources().getDisplayMetrics().density * pixels;
    }

    /**
     * Convenience method to hide the keyboard.
     *
     * @param view A view in the hierarchy.
     */
    protected void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = getInputMethodManager();
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    private void getAnswerAdapter(final RecyclerView answerListView) {
        mAnswersProgress.setVisibility(View.VISIBLE);
        Activity.createQuery(mQuestion).findInBackground(new FindCallback<Activity>() {
            @Override
            public void done(final List<Activity> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    emptyTextView.setVisibility(View.GONE);
                    Log.d(LOG_TAG, "answers-server : " + objects.size());
                    mAdapter = new FeedCommentAdapter(objects, FeedDetailActivity.this,
                            mCategory, mQuestion);
                    answerListView.setHasFixedSize(true);

                    // use a linear layout manager
                    RecyclerView.LayoutManager mLayoutManager = new
                            LinearLayoutManager(FeedDetailActivity.this);
                    answerListView.setLayoutManager(mLayoutManager);
                    answerListView.setAdapter(mAdapter);
                    answerListView.setVisibility(View.VISIBLE);
                    mAnswersProgress.setVisibility(View.GONE);
                    ParseUser.pinAllInBackground(ModelUtils.FEED_COMMENT_PIN + mQuestion.getObjectId(), objects);

                } else {
                    if (e == null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        mAnswersProgress.setVisibility(View.GONE);
                        isShowingAnswers = false;
                        Snackbar.make(answerListView, getResources().getString(R.string.no_comments_posted), Snackbar.LENGTH_LONG)
                                .setAction("OK", null).show();
                        Log.d(LOG_TAG, "no answers yet");
                    } else {
                        emptyTextView.setVisibility(View.VISIBLE);
                        mAnswersProgress.setVisibility(View.GONE);
                        Log.d(LOG_TAG, "exception while fetching answers : " + e.getMessage());
                    }
                }
            }
        });

    }

    private void getAnswerAdapterLocal(final RecyclerView answerListView) {
        mAnswersProgress.setVisibility(View.VISIBLE);
        Activity.createLocalQuery(mQuestion).findInBackground(new FindCallback<Activity>() {
            @Override
            public void done(final List<Activity> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    emptyTextView.setVisibility(View.GONE);
                    Log.d(LOG_TAG, "answers-local : " + objects.size());
                    mAdapter = new FeedCommentAdapter(objects, FeedDetailActivity.this,
                            mCategory, mQuestion);
                    answerListView.setHasFixedSize(true);

                    // use a linear layout manager
                    RecyclerView.LayoutManager mLayoutManager = new
                            LinearLayoutManager(FeedDetailActivity.this);
                    answerListView.setLayoutManager(mLayoutManager);
                    answerListView.setAdapter(mAdapter);
                    answerListView.setVisibility(View.VISIBLE);
                    mAnswersProgress.setVisibility(View.GONE);
                    ParseUser.pinAllInBackground(ModelUtils.FEED_COMMENT_PIN + mQuestion.getObjectId(), objects);

                } else {
                    if (e == null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        mAnswersProgress.setVisibility(View.GONE);
                        isShowingAnswers = false;
                        Snackbar.make(answerListView, getResources().getString(R.string.no_comments_posted), Snackbar.LENGTH_LONG)
                                .setAction("OK", null).show();
                    } else {
                        emptyTextView.setVisibility(View.VISIBLE);
                        mAnswersProgress.setVisibility(View.GONE);
                        Log.d(LOG_TAG, "exception while fetching answers : " + e.getMessage());
                    }
                }
            }
        });

    }

    private void saveAnswer(String content, final View view) {

        mLinearOutSlowInInterpolator = new LinearOutSlowInInterpolator();
        mHandler = new Handler();
        final Activity answer = new Activity();
        answer.setContent(content);
        answer.setFeed(mQuestion);
        answer.setPriority(0);
        answer.setfromUser(ParseUser.getCurrentUser());
        answer.settoUser(mQuestion.getUser());
        answer.setStatus(false);
        answer.setTitle(content);
        answer.setType(Activity.COMMENT);
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        acl.setPublicWriteAccess(true);
        answer.setACL(acl);
        answer.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if( e == null ) {
                    Snackbar.make(view, getResources().getString(R.string.comment_submitted),
                            Snackbar.LENGTH_LONG)
                            .setAction("OK", null).show();

                    answer.pinInBackground(ModelUtils.FEED_COMMENT_PIN + mQuestion.getObjectId(), new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            getAnswerAdapterLocal(answerRecyclerView);
                        }
                    });
                    Log.d(LOG_TAG, "answer saved!");
                } else {

                    Log.d(LOG_TAG,"exception while saving answer - " + e);
                }
            }
        });

    }

    private AlertDialog answerDialog;
    private String newTag = "";
    private EditText inputTag;
    private AlertDialog createAnswerDialog(final EditText input,final View root) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogRootView = inflater.inflate(R.layout.dialog_answer, null);
        final EditText answerView = (EditText) dialogRootView.findViewById(R.id.answer_edit_text);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogRootView)
                // Add action buttons
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Editable value = answerView.getText();
                        newTag = value.toString();
                        if(TextUtils.isEmpty(newTag)) {
                            Snackbar.make(questionContent, getResources().
                                    getString(R.string.enter_some_text), Snackbar.LENGTH_LONG)
                                    .setAction("OK", null).show();
                        } else {

                            saveAnswer(newTag,questionContent);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        return builder.create();
    }

    @Override
    public void onDetachedFromWindow() {
        if (mMoveOffScreenRunnable != null) {
            mHandler.removeCallbacks(mMoveOffScreenRunnable);
        }
        super.onDetachedFromWindow();
    }
   MenuItem delete;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG,"onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_q_detail, menu);
        delete = menu.findItem(R.id.action_delete);
        handleDeleteOption();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            confirmDeleteDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleDeleteOption(){
        if(mQuestion == null)
            return;
        if(mQuestion.getUser() != null && mQuestion.getUser() != ParseUser.getCurrentUser()) {
            if(delete != null   )
                delete.setVisible(false);
        }
    }

    private void confirmDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                deleteQuestion();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setTitle(R.string.delete_confirm_title).setMessage(R.string.delete_confirm_message);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteQuestion() {
        mQuestion.unpinInBackground(ModelUtils.FEED_PIN, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                mQuestion.deleteInBackground();
                finish();
            }
        });
    }
}
