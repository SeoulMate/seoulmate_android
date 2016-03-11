package co.seoulmate.android.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import co.seoulmate.android.app.R;
import co.seoulmate.android.app.utils.ModelUtils;
import co.seoulmate.android.app.utils.UserUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class LeadershipFragment extends Fragment {

    private static final String LOG_TAG = LeadershipFragment.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private List<ParseUser> users;
    RecyclerView rv;
    private ProgressBar progress;
    private LinearLayoutManager mLayoutManager;

    public LeadershipFragment() {
    }


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LeadershipFragment newInstance(int sectionNumber) {
        LeadershipFragment fragment = new LeadershipFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_leadership, container, false);

        rv = (RecyclerView) rootView.findViewById(R.id.leaderRecyclerview);
        progress = (ProgressBar) getActivity().findViewById(R.id.progressBoard);
        rv.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);
        fetchUsers(rv);
        return rootView;
    }


    private void fetchUsers(final View rootView) {
        progress.setVisibility(View.VISIBLE);
        fetchUsersfromLocalDb();

    }

    private void fetchUsersfromLocalDb() {

        createSingleLocalQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects != null && objects.size() != 0) {
                    Log.d(LOG_TAG, "got users : " + objects.size());
                    users = objects;
                    setupRecyclerView(rv);
                } else if (e == null && objects.size() == 0) {
                    Log.d(LOG_TAG, "got no users : " + objects.size());
                    fetchUsersFromParse();

                } else {
                    fetchUsersFromParse();
                    Log.d(LOG_TAG, "exception occured :" + e.getMessage());
                }
            }
        });
    }

    private void fetchUsersFromParse() {

        createSingleQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects != null && objects.size() != 0) {
                    Log.d(LOG_TAG, "got users from parse : " + objects.size());
                    users = objects;
                    setupRecyclerView(rv);
                    pinUsers(users);
                } else if (e == null && objects.size() == 0) {
                    Log.d(LOG_TAG, "got 0 users from parse : " + objects.size());
                    progress.setVisibility(View.GONE);

                } else {
                    progress.setVisibility(View.GONE);

                    Log.d(LOG_TAG, "exception occurred while fetching from Parse :" + e.getMessage());
                }
            }
        });
    }

    public static ParseQuery<ParseUser> createSingleQuery() {

        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.orderByDescending(ModelUtils.POINTS);
        query.setLimit(120);

        return query;
    }

    public static ParseQuery<ParseUser> createSingleLocalQuery() {

        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.orderByDescending(ModelUtils.POINTS);
        query.setLimit(120);
        query.fromLocalDatastore();

        return query;
    }

    private void pinUsers(final List<ParseUser> userList) {

        ParseObject.unpinAllInBackground(ModelUtils.USER_PIN, userList, new DeleteCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    return;
                }
                ParseObject.pinAllInBackground(ModelUtils.USER_PIN, userList);
                Log.d(LOG_TAG, "pinned userList : " + ModelUtils.USER_PIN);

            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setAdapter(new LeaderBoardViewAdapter(getActivity(),
                users));
        progress.setVisibility(View.GONE);
    }

    public static class LeaderBoardViewAdapter
            extends RecyclerView.Adapter<LeaderBoardViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<ParseUser> mUsers;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mBoundString;

            public final View mView;
            public final ImageView mImageView;
            public final TextView mTextView;
            public final TextView mUserPoints;
            public final TextView mUserRank;
            public final ImageView mUserBadge;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.avatar);
                mTextView = (TextView) view.findViewById(R.id.userName);
                mUserPoints = (TextView) view.findViewById(R.id.points);
                mUserRank = (TextView) view.findViewById(R.id.userRank);
                mUserBadge = (ImageView) view.findViewById(R.id.achievementImg);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }


        public LeaderBoardViewAdapter(Context context, List<ParseUser> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mUsers = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.leaders_list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            holder.mUserRank.setText(String.valueOf(position+1));
            if(position < 6) {
                holder.mUserBadge.setImageResource(R.drawable.ic_achievement_star);
            }

            if (mUsers.get(position) != null) {
                holder.mTextView.setText(mUsers.get(position).getString(ModelUtils.FIRST_NAME));
                holder.mUserPoints.setText(String.valueOf(mUsers.get(position).getInt(ModelUtils.POINTS)));
                ParseFile image = mUsers.get(position).getParseFile(UserUtils.PROFILE_PIC);
                if( image != null && image.getUrl() != null) {
                    Picasso.with(holder.mImageView.getContext())
                            .load(image.getUrl())
                            .placeholder(R.drawable.avatar_6_raster)
                            .error(R.drawable.avatar_9_raster)
                            .into(holder.mImageView);
                } else {
                    holder.mImageView.setImageDrawable(holder.mImageView.getContext()
                            .getResources().getDrawable(R.drawable.avatar_1_raster));
                }
            } else {

                holder.mTextView.setText("N/A");
                holder.mImageView.setImageDrawable(holder.mImageView.getContext()
                        .getResources().getDrawable(R.drawable.avatar_1_raster));
                holder.mUserPoints.setText("0");


            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });


        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                fetchUsers(null);
                return true;
        }
        return false;
    }

}
