package co.seoulmate.android.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.seoulmate.android.app.R;
import co.seoulmate.android.app.activities.ProfileActivity;
import co.seoulmate.android.app.model.Feed;
import co.seoulmate.android.app.utils.CategoryUtils;
import co.seoulmate.android.app.utils.ModelUtils;
import co.seoulmate.android.app.utils.UserUtils;

/**
 * Created by hassanabid on 10/24/15.
 */
public class FeedCommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

    private static final String LOG_TAG = FeedCommentAdapter.class.getSimpleName();
    private List<co.seoulmate.android.app.model.Activity> mDataset;
    private Context mContext;
    private TextView voteTextView;
    private int mCatPos;
    private Drawable mSuccessIcon;
    private Drawable mFailedIcon;
    private ImageView mSolvedState;
    private Feed mQuestion;

    // Provide a suitable constructor (depends on the kind of dataset)
    public FeedCommentAdapter(List<co.seoulmate.android.app.model.Activity> myDataset, Context context, int cat, Feed q) {
        mDataset = myDataset;
        mContext = context;
        mCatPos = cat;
        mQuestion = q;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_comment_item, parent, false);
        TextView content = (TextView) v.findViewById(R.id.answerContent);
        TextView writer = (TextView) v.findViewById(R.id.answerWriter);
        voteTextView = (TextView) v.findViewById(R.id.answerVotes);
        mSolvedState = (ImageView) v.findViewById(R.id.solved_state);
        ImageButton voteIcon = (ImageButton) v.findViewById(R.id.answerVoteIcon);
        final LinearLayout voteLayout = (LinearLayout) v.findViewById(R.id.voteUpLayout);
        final RoundedImageView pic = (RoundedImageView) v.findViewById(R.id.profilePicList);
        final TextView time = (TextView) v.findViewById(R.id.time_view);

        // set the view's size, margins, paddings and layout parameters
        CommentViewHolder vh = new CommentViewHolder(v,content, voteTextView, writer,
                voteIcon,mSolvedState,voteLayout,pic,time,
                new CommentViewHolder.AnswerViewHolderClicks() {
            @Override
            public void onClickAnswer(final View caller, final int position) {
                if (mDataset != null && mDataset.size() != 0) {
                    final co.seoulmate.android.app.model.Activity answer = mDataset.get(position);
                    if (caller instanceof TextView) {
                        Log.d(LOG_TAG, "answer title clicked");
                        new BottomSheet.Builder(mContext, R.style.BottomSheet_StyleDialog).
                                title(mContext.getResources().getString(R.string.answer_popup_title)).
                                sheet(R.menu.answer_popup_menu).
                                listener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case R.id.accept_answer:
                                                if (answer.getStatus())
                                                    return;
                                                else if(ParseUser.getCurrentUser() != mQuestion.getUser())
                                                {
                                                    Snackbar.make(caller, mContext.getResources().
                                                                    getString(R.string.not_allowed_to_accept_answer),
                                                            Snackbar.LENGTH_SHORT)
                                                            .setAction("OK", null).show();
                                                    return;
                                                }
                                                answer.setStatus(true);
                                                answer.setPriority(ModelUtils.PRIORITY_ACCEPTED);
                                                mQuestion.setSolvedStatus(true);
                                                mQuestion.pinInBackground(ModelUtils.FEED_PIN + mCatPos);
                                                answer.pinInBackground(ModelUtils.FEED_COMMENT_PIN
                                                        + mQuestion.getObjectId(), new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            notifyItemChanged(position);
                                                            setSolvedStateForQuiz(mSolvedState, true);
                                                            answer.saveInBackground(new SaveCallback() {
                                                                @Override
                                                                public void done(ParseException e) {
                                                                    if (e == null) {
                                                                        Log.d(LOG_TAG, "answer status" +
                                                                                " changed on server");
                                                                        mQuestion.saveInBackground();
                                                                    } else {
                                                                        Log.d(LOG_TAG, "exception while " +
                                                                                "changing answer status on server" +
                                                                                e.getMessage());
                                                                        return;
                                                                    }
                                                                }
                                                            });
                                                            Log.d(LOG_TAG, "answer status changed to true");

                                                        } else {
                                                            Log.d(LOG_TAG, "exception while changing answer status " +
                                                                    e.getMessage());

                                                        }
                                                    }
                                                });
                                                break;
                                            case R.id.share:
                                                shareAnswer(answer);
                                                break;
                                        }
                                    }
                                }).show();
                    } else if (caller instanceof ImageButton || caller instanceof LinearLayout) {
                        Log.d(LOG_TAG, "voteUp clicked for key : " + ModelUtils.FEED_COMMENT_PIN +
                                mQuestion.getObjectId() + " at :" + position);
                        int voteCount = Integer.valueOf(voteTextView.getText().toString());
                        ParseUser cUser = ParseUser.getCurrentUser();
                        if (answer.getVoters() != null && !answer.getVoters().contains(cUser)) {
                            int newVoteCount = voteCount + 1;
                            voteTextView.setText(String.valueOf(newVoteCount));
                            answer.addVoter(cUser, answer);
                            answer.pinInBackground(ModelUtils.FEED_COMMENT_PIN
                                    + mQuestion.getObjectId(), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        notifyItemChanged(position);
                                        answer.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Log.d(LOG_TAG, "updated vote count and object for pos :" + position);
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
                        }else if (answer.getVoters() != null && answer.getVoters().contains(cUser)) {
                            int newVoteCount =0;
                            if(answer.getVoters().size() >= 1)
                             newVoteCount = answer.getVoters().size()  - 1;
                            voteTextView.setText(String.valueOf(newVoteCount));
                            answer.removeVoter(cUser, answer);
                            answer.pinInBackground(ModelUtils.FEED_COMMENT_PIN
                                    + mQuestion.getObjectId(), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        notifyItemChanged(position);
                                        answer.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Log.d(LOG_TAG, "updated vote count and object for pos :" + position);
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


                        } else if (answer.getVoters() == null) {
                            Log.d(LOG_TAG,"add first voters");
                            int newVoteCount = 1;
                            voteTextView.setText(String.valueOf(newVoteCount));
                            answer.addVoter(cUser, answer);
                            answer.pinInBackground(ModelUtils.FEED_COMMENT_PIN
                                    + mQuestion.getObjectId(), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        notifyItemChanged(position);
                                        answer.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Log.d(LOG_TAG, "updated vote count and object for pos :" + position);
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
                    } else  if (caller instanceof RoundedImageView) {
                            Log.d(LOG_TAG, "click on user profile");
                            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                            String objectId = answer.getfromUser().getObjectId();
                            Log.d(LOG_TAG, "userId :" + objectId);
                            profileIntent.putExtra(ModelUtils.USER_ID, objectId);
                        mContext.startActivity(profileIntent);

                    }
                }
            }
        });
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final CommentViewHolder holder, int position) {


        final Context mContext = holder.writerName.getContext();

        if(mDataset != null && mDataset.size() == 0 ) {
            holder.voteCount.setVisibility(View.INVISIBLE);
            holder.writerName.setVisibility(View.INVISIBLE);
            holder.mContentView.setText(mContext.getResources().getString(R.string.no_comments_posted));
            holder.voteIconView.setVisibility(View.INVISIBLE);
        } else {
            co.seoulmate.android.app.model.Activity answer = mDataset.get(position);
            if(answer == null) {
                Log.d(LOG_TAG,"answer is null");
                return;
            }
            PrettyTime p = new PrettyTime();
            Date now = new Date(System.currentTimeMillis());
            long diff = 0;
            if(answer.getCreatedAt() != null)
             diff = now.getTime() - answer.getCreatedAt().getTime();
            else if (answer.getUpdatedAt() != null)
                diff = now.getTime() - answer.getUpdatedAt().getTime();
            holder.mTimeView.setText(p.format(new Date(System.currentTimeMillis() - diff)));
            holder.mContentView.setText(answer.getContent());
            if(answer.getVoters()!= null && answer.getVoters().size() != 0) {
                Log.d(LOG_TAG,"No of voters : " + answer.getVoters().size());
                holder.voteCount.setText(String.valueOf(answer.getVoters().size()));
            } else {
                holder.voteCount.setText("0");
            }
            if(answer.getfromUser() != null) {
  /*              answer.getfromUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        holder.writerName.setText(object.getString(ModelUtils.FIRST_NAME));
                    }
                });*/
                holder.writerName.setText(answer.getfromUser().getString(ModelUtils.FIRST_NAME));
                ParseFile image = answer.getfromUser().getParseFile(UserUtils.PROFILE_PIC);
                if ( image!= null &&
                        image.getUrl() != null) {
                    Picasso.with(mContext)
                            .load(image.getUrl())
                            .placeholder(R.drawable.avatar_1_raster)
                            .error(R.drawable.avatar_9_raster)
                            .into(holder.profilePic);

                }else {
                    holder.profilePic.setImageResource(R.drawable.avatar_2_raster);
                }

            } else {

                holder.writerName.setText("N/A");
                holder.profilePic.setImageResource(R.drawable.avatar_2_raster);

            }

            setSolvedStateForQuiz(holder.solveStateView, answer.getStatus());


        }

    }

    private void update() {
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(mDataset != null && mDataset.size() == 0)
            return  1;
        return mDataset.size();
    }

    private void setSolvedStateForQuiz(ImageView solvedState, boolean solved) {

        final Drawable tintedImage;
        if (solved) {
            Log.d(LOG_TAG,"answer marked as :" +solved);
            tintedImage = getSuccessIcon(mContext);
        } else {
            Log.d(LOG_TAG,"answer marked as :" +solved);
            tintedImage = getFailedIcon(mContext);
            solvedState.setVisibility(View.INVISIBLE);
        }
        solvedState.setImageDrawable(tintedImage);
    }

    private Drawable getSuccessIcon(Context context) {
        if (null == mSuccessIcon) {
            mSuccessIcon = loadAndTint(context, R.drawable.check_pressed, R.color.theme_green_primary);
        }
        return mSuccessIcon;
    }

    private Drawable getFailedIcon(Context context) {
        if (null == mFailedIcon) {
            mFailedIcon = loadAndTint(context, R.drawable.check, R.color.theme_red_primary);
        }
        return mFailedIcon;
    }

    /**
     * Convenience method to aid tintint of vector drawables at runtime.
     *
     * @param context The {@link Context} for this app.
     * @param drawableId The id of the drawable to load.
     * @param tintColor The tint to apply.
     * @return The tinted drawable.
     */
    private Drawable loadAndTint(Context context, @DrawableRes int drawableId,
                                 @ColorRes int tintColor) {
        Drawable imageDrawable = ContextCompat.getDrawable(context, drawableId);
        if (imageDrawable == null) {
            throw new IllegalArgumentException("The drawable with id " + drawableId
                    + " does not exist");
        }
        DrawableCompat.setTint(imageDrawable, tintColor);
        return imageDrawable;
    }

    private void shareAnswer(co.seoulmate.android.app.model.Activity answer) {
        mContext.startActivity(Intent.createChooser(
                createShareIntent(R.string.share_template, mQuestion.getTitle() + "- " +
                                mQuestion.getTitle(),
                       CategoryUtils.getName(mQuestion.getCategory()),
                        mQuestion.getlink() != null ? mQuestion.getlink() : "",answer.getContent()),
                mContext.getString(R.string.title_share)));
    }

    public Intent createShareIntent(int messageTemplateResId, String title, String hashtags,
                                    String url,String answer) {
        String photoUrl = mQuestion.getPhoto() != null ?
                mQuestion.getPhoto().getUrl(): "";
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from((Activity) mContext)
                .setType("text/plain")
                .setStream(Uri.parse(photoUrl))
                .setText(mContext.getString(messageTemplateResId,
                        title, hashtags, " " + url , answer));
        return builder.getIntent();
    }
}
