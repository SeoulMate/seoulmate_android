package co.seoulmate.android.app.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
public class BoardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String LOG_TAG = BoardViewHolder.class.getSimpleName();
    public TextView mTitleView;
    public TextView mTimeView;
    public Button tagTextView;
    public TextView writerName;
    public TextView voteCount;
    public ImageButton voteIconView;
    public LinearLayout voteUpLayout;
    public RoundedImageView profilePic;

    public QuestionViewHolderClicks mListener;

    public BoardViewHolder(View v, TextView title, TextView time, Button tagIcon
            , TextView vote, TextView writer, ImageButton voteIcon,
                           LinearLayout linearLayout, RoundedImageView pic,
                           QuestionViewHolderClicks listener
    ) {
        super(v);
        mListener = listener;
        mTitleView = title;
        mTimeView = time;
        tagTextView = tagIcon;
        voteCount = vote;
        writerName = writer;
        voteIconView = voteIcon;
        voteUpLayout = linearLayout;
        profilePic = pic;
        voteUpLayout.setOnClickListener(this);
        voteIconView.setOnClickListener(this);
        mTitleView.setOnClickListener(this);
        profilePic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG,"adapter position : " + getAdapterPosition() + " layout pos: "
                + getLayoutPosition());
        mListener.onClickQuestion(v,getAdapterPosition());
    }

    public static interface QuestionViewHolderClicks {
        public void onClickQuestion(View caller, int position);
    }
}