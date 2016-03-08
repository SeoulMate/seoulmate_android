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

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import co.seoulmate.android.app.R;
import org.ocpsoft.prettytime.PrettyTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.seoulmate.android.app.activities.BoardDetailActivity;
import co.seoulmate.android.app.activities.ProfileActivity;
import co.seoulmate.android.app.model.Board;
import co.seoulmate.android.app.utils.CategoryUtils;
import co.seoulmate.android.app.utils.ModelUtils;
import co.seoulmate.android.app.utils.UserUtils;

/**
 * Created by hassanabid on 10/24/15.
 */
public class BoardAdapter extends RecyclerView.Adapter<BoardViewHolder> {

    private static final String LOG_TAG = BoardAdapter.class.getSimpleName();
    private List<Board> mDataset;
    private Context mContext;
    private TextView voteTextView;

    public BoardAdapter(List<Board> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BoardViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_board_list_item, parent, false);
        TextView title = (TextView) v.findViewById(R.id.qtitle);
        TextView time = (TextView) v.findViewById(R.id.time_view);
        Button tagTextView = (Button) v.findViewById(R.id.tag_of_cat);
        TextView writer = (TextView) v.findViewById(R.id.writer_name);
        voteTextView = (TextView) v.findViewById(R.id.questionVotes);
        ImageButton voteIcon = (ImageButton) v.findViewById(R.id.questionVoteIcon);
        final LinearLayout voteLayout = (LinearLayout) v.findViewById(R.id.voteUpLayout);
        final RoundedImageView pic = (RoundedImageView) v.findViewById(R.id.profilePicList);

        BoardViewHolder vh = new BoardViewHolder(v,title,time,tagTextView,voteTextView,writer,voteIcon,voteLayout,
                pic,new BoardViewHolder.QuestionViewHolderClicks() {
            @Override
            public void onClickQuestion(View caller, final int position) {
                if(mDataset != null && mDataset.size() != 0) {
                    final Board q = mDataset.get(position);
                    if( caller instanceof TextView) {
                        Log.d(LOG_TAG, "question title clicked");
                        Intent intent = new Intent(mContext, BoardDetailActivity.class);
                        intent.putExtra(ModelUtils.OBJECT_ID, (String) caller.getTag());
                        mContext.startActivity(intent);
                    } else if (caller instanceof ImageButton || caller instanceof LinearLayout) {
                        Log.d(LOG_TAG, "voteUp  clicked for key : " + ModelUtils.BOARD_PIN +
                                " at :" + position);
                        int voteCount =  Integer.valueOf(voteTextView.getText().toString());
                        ParseUser cUser = ParseUser.getCurrentUser();
                        if (q.getVoters() != null && !q.getVoters().contains(cUser)) {
                            int newVoteCount = voteCount + 1;
                            voteTextView.setText(String.valueOf(newVoteCount));
                            q.addVoter(cUser, q);
                            q.pinInBackground(ModelUtils.BOARD_PIN, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        notifyItemChanged(position);
                                        q.saveInBackground(new SaveCallback() {
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
                        } else if (q.getVoters() == null) {
                            Log.d(LOG_TAG,"add first voters");
                            int newVoteCount = voteCount + 1;
                            voteTextView.setText(String.valueOf(newVoteCount));
                            q.addVoter(cUser, q);
                            q.pinInBackground(ModelUtils.BOARD_PIN, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        notifyItemChanged(position);
                                        q.saveInBackground(new SaveCallback() {
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
                        if(q.getUser() != null)
                         objectId = q.getUser().getObjectId();
                        Log.d(LOG_TAG,"userId :" + objectId);
                        profileIntent.putExtra(ModelUtils.USER_ID,objectId);
                        mContext.startActivity(profileIntent);

                    }
                }
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(final BoardViewHolder holder, int position) {


        if(mDataset != null && mDataset.size() == 0 ) {
            holder.mTimeView.setVisibility(View.INVISIBLE);
            holder.voteCount.setVisibility(View.INVISIBLE);
            holder.writerName.setVisibility(View.INVISIBLE);
            holder.mTitleView.setText(mContext.getResources().getString(R.string.no_board_post));
            holder.tagTextView.setVisibility(View.INVISIBLE);
            holder.profilePic.setVisibility(View.INVISIBLE);
        } else {
            Board board = mDataset.get(position);
            if(board == null)
                return;
            PrettyTime p = new PrettyTime();
            Date now = new Date(System.currentTimeMillis());
            long diff = now.getTime() - board.getCreatedAt().getTime();
            holder.mTitleView.setText(board.getTitle());
            holder.mTitleView.setTag(board.getObjectId());
            holder.mTimeView.setText(p.format(new Date(System.currentTimeMillis() - diff)));

            if(board.getVoters()!= null && board.getVoters().size() != 0) {
                Log.d(LOG_TAG,"No of voters : " + board.getVoters().size());
                holder.voteCount.setText(String.valueOf(board.getVoters().size()));
            } else {
                holder.voteCount.setText("0");
            }
            if(board.getUser() != null) {
                //TODO: Replace with Display Name
//              holder.writerName.setText(object.getString(ModelUtils.DISPLAY_NAME));
                holder.writerName.setText(board.getUser().getString(ModelUtils.FIRST_NAME));
                ParseFile image = board.getUser().getParseFile(UserUtils.PROFILE_PIC);
                if ( image!= null &&
                        image.getUrl() != null) {

                    Picasso.with(mContext)
                            .load(image.getUrl())
                            .placeholder(R.drawable.avatar_1_raster)
                            .error(R.drawable.avatar_9_raster)
                            .into(holder.profilePic);

                } else {
                    holder.profilePic.setImageResource(R.drawable.avatar_2_raster);
                }
            }
            else {
                holder.writerName.setText("N/A");
                holder.profilePic.setImageResource(R.drawable.avatar_2_raster);
            }

            int catPos = board.getCategory();
            holder.tagTextView.setText(String.format(Locale.US,
                    mContext.getResources().getString(R.string.hash_category),
                    CategoryUtils.getName(catPos)));



        }

    }

    @Override
    public int getItemCount() {
        if(mDataset != null && mDataset.size() == 0)
            return  1;
        return mDataset.size();
    }
}
