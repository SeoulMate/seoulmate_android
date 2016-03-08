package co.seoulmate.android.app.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

/**
 * Created by hassanabid on 10/25/15.
 */
// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String LOG_TAG = CommentViewHolder.class.getSimpleName();
    public TextView mContentView;
    public TextView writerName;
    public TextView voteCount;
    public ImageButton voteIconView;
    public ImageView solveStateView;
    public TextView mTimeView;
    public LinearLayout voteUpLayout;
    public RoundedImageView profilePic;
    public ImageView answerStatus;


    public AnswerViewHolderClicks mListener;

    public CommentViewHolder(View v, TextView content
            , TextView vote, TextView writer, ImageButton voteIcon, ImageView solvedState, LinearLayout voteLayout,
                             RoundedImageView pic, TextView timeView,
                             AnswerViewHolderClicks listener) {
        super(v);
        mListener = listener;
        mContentView = content;
        voteCount = vote;
        writerName = writer;
        voteIconView = voteIcon;
        solveStateView = solvedState;
        voteUpLayout = voteLayout;
        profilePic = pic;
        mTimeView = timeView;
        voteIconView.setOnClickListener(this);
        mContentView.setOnClickListener(this);
        voteUpLayout.setOnClickListener(this);
        profilePic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG,"adapter position : " + getAdapterPosition() + " layout pos: "
                + getLayoutPosition());
        mListener.onClickAnswer(v, getAdapterPosition());
    }

    public static interface AnswerViewHolderClicks {
        public void onClickAnswer(View caller, int position);
    }
}