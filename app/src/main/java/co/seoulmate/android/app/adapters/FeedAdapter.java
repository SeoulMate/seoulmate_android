package co.seoulmate.android.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.seoulmate.android.app.R;
import co.seoulmate.android.app.activities.FeedDetailActivity;
import co.seoulmate.android.app.activities.ProfileActivity;
import co.seoulmate.android.app.model.Board;
import co.seoulmate.android.app.model.Feed;
import co.seoulmate.android.app.utils.CategoryUtils;
import co.seoulmate.android.app.utils.ModelUtils;
import co.seoulmate.android.app.utils.UserUtils;

/**
 * Created by hassanabid on 10/24/15.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedViewHolder> {

    private static final String LOG_TAG = FeedAdapter.class.getSimpleName();
    private List<Feed> mDataset;
    private Context mContext;
    private TextView voteTextView;

    public FeedAdapter(List<Feed> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_feed_list_item, parent, false);
        TextView title = (TextView) v.findViewById(R.id.qtitle);
        TextView time = (TextView) v.findViewById(R.id.time_view);
        Button tagTextView = (Button) v.findViewById(R.id.tag_of_cat);
        TextView writer = (TextView) v.findViewById(R.id.writer_name);
        voteTextView = (TextView) v.findViewById(R.id.questionVotes);
        ImageButton voteIcon = (ImageButton) v.findViewById(R.id.questionVoteIcon);
        final LinearLayout voteLayout = (LinearLayout) v.findViewById(R.id.voteUpLayout);
        final RoundedImageView pic = (RoundedImageView) v.findViewById(R.id.profilePicList);
        final ImageView status = (ImageView) v.findViewById(R.id.questionStatus);

        FeedViewHolder vh = new FeedViewHolder(v,title,time,tagTextView,voteTextView,writer,voteIcon,voteLayout,
                pic,status,new FeedViewHolder.FeedViewHolderClicks() {
            @Override
            public void onClickQuestion(View caller, final int position) {
                if(mDataset != null && mDataset.size() != 0) {
                    final Feed feed = mDataset.get(position);
                    if( caller instanceof TextView) {
                        Log.d(LOG_TAG, "question title clicked");
                        Intent intent = new Intent(mContext, FeedDetailActivity.class);
                        intent.putExtra(ModelUtils.OBJECT_ID, (String) caller.getTag());
                        intent.putExtra(ModelUtils.CATNO,feed.getCategory());
                        mContext.startActivity(intent);
                    } else if (caller instanceof ImageButton || caller instanceof LinearLayout) {
                        Log.d(LOG_TAG, "voteUp  clicked for key : " + ModelUtils.BOARD_PIN +
                                " at :" + position);
                        int voteCount =  Integer.valueOf(voteTextView.getText().toString());
                        ParseUser cUser = ParseUser.getCurrentUser();
                        if (feed.getVoters() != null && !feed.getVoters().contains(cUser)) {
                            int newVoteCount = voteCount + 1;
                            voteTextView.setText(String.valueOf(newVoteCount));
                            feed.addVoter(cUser, feed);
                            feed.pinInBackground(ModelUtils.FEED_PIN, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        notifyItemChanged(position);
                                        feed.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Log.d(LOG_TAG, "updated vote count and object for pos :" + position);
                                                } else {
                                                    Log.d(LOG_TAG, "error occurred while updating voters :"
                                                            + e.getMessage());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.d(LOG_TAG, "error occurred while updating voters locally :"
                                                + e.getMessage());
                                    }
                                }
                            });
                        } else if (feed.getVoters() == null) {
                            Log.d(LOG_TAG,"add first voters");
                            int newVoteCount = voteCount + 1;
                            voteTextView.setText(String.valueOf(newVoteCount));
                            feed.addVoter(cUser, feed);
                            feed.pinInBackground(ModelUtils.FEED_PIN, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        notifyItemChanged(position);
                                        feed.saveInBackground(new SaveCallback() {
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
                    } else if (caller instanceof RoundedImageView) {
                        Log.d(LOG_TAG, "click on user profile");
                        Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                        String objectId = "";
                        if(feed.getUser() != null)
                         objectId = feed.getUser().getObjectId();
                        Log.d(LOG_TAG,"userId :" + objectId);
                        profileIntent.putExtra(ModelUtils.USER_ID,objectId);
                        mContext.startActivity(profileIntent);

                    }
                }
            }
        });
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final FeedViewHolder holder, int position) {


        if(mDataset != null && mDataset.size() == 0 ) {
            holder.mTimeView.setVisibility(View.INVISIBLE);
            holder.voteCount.setVisibility(View.INVISIBLE);
            holder.writerName.setVisibility(View.INVISIBLE);
            holder.mTitleView.setText(mContext.getResources().getString(R.string.no_feed_post));
            holder.voteIconView.setVisibility(View.INVISIBLE);
            holder.tagTextView.setVisibility(View.INVISIBLE);
            holder.profilePic.setVisibility(View.INVISIBLE);
        } else {
            Feed feed = mDataset.get(position);
            if(feed == null)
                return;
            PrettyTime p = new PrettyTime();
            Date now = new Date(System.currentTimeMillis());
            long diff = now.getTime() - feed.getCreatedAt().getTime();
            holder.mTitleView.setText(feed.getTitle());
            holder.mTitleView.setTag(feed.getObjectId());
            holder.mTimeView.setText(p.format(new Date(System.currentTimeMillis() - diff)));
            if(feed.getSolvedStatus()) {
                holder.questionStatus.setVisibility(View.VISIBLE);
            }

            if(feed.getVoters()!= null && feed.getVoters().size() != 0) {
                Log.d(LOG_TAG,"No of voters : " + feed.getVoters().size());
                holder.voteCount.setText(String.valueOf(feed.getVoters().size()));
            } else {
                holder.voteCount.setText("0");
            }
            String writerName = "";
            if(feed.getUser() != null) {

//              holder.writerName.setText(object.getString(ModelUtils.DISPLAY_NAME));
                holder.writerName.setText(feed.getUser().getString(ModelUtils.FIRST_NAME));
                ParseFile image = feed.getUser().getParseFile(UserUtils.PROFILE_PIC);
                if ( image!= null &&
                        image.getUrl() != null) {

                    Picasso.with(mContext)
                            .load(image.getUrl())
                            .placeholder(R.drawable.avatar_1_raster)
                            .error(R.drawable.avatar_9_raster)
                            .into(holder.profilePic);
                /*    Glide.with(mContext)
                            .load(image.getUrl())
                            .centerCrop()
                            .placeholder(R.drawable.avatar_1_raster)
                            .into(holder.profilePic);*/

                } else {
                    holder.profilePic.setImageResource(R.drawable.avatar_2_raster);
                }
            }    else {
                holder.writerName.setText("N/A");

                holder.profilePic.setImageResource(R.drawable.avatar_2_raster);
            }

            int catPos = feed.getCategory();
            holder.tagTextView.setText(String.format(Locale.US,
                    mContext.getResources().getString(R.string.hash_category),
                    CategoryUtils.getName(catPos)));



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
}
