package co.seoulmate.android.app.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.List;

import co.seoulmate.android.app.R;
import co.seoulmate.android.app.adapters.BoardAdapter;
import co.seoulmate.android.app.adapters.FeedAdapter;
import co.seoulmate.android.app.model.Board;
import co.seoulmate.android.app.model.Feed;
import co.seoulmate.android.app.utils.ModelUtils;

/**
 * Created by hassanabid on 3/1/16.
 */
public class FeedFragment extends android.support.v4.app.Fragment {

    private static final String LOG_TAG = FeedFragment.class.getSimpleName();
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private RecyclerView boardRecyvleview;
    private LinearLayoutManager mLayoutManager;
    private FeedAdapter mAdapter;
    private ProgressBar progress;


    public FeedFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FeedFragment newInstance(int sectionNumber) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG,"onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_board, container, false);
        boardRecyvleview = (RecyclerView) rootView.findViewById(R.id.board_recycler_view);

//        boardRecyvleview.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        boardRecyvleview.setLayoutManager(mLayoutManager);
        progress = (ProgressBar) getActivity().findViewById(R.id.progressBoard);


        getFeedPosts();

        return rootView;
    }

    private void getFeedPosts() {

        progress.setVisibility(View.VISIBLE);
        Feed.createLocalQuery().findInBackground(new FindCallback<Feed>() {

            @Override
            public void done(List<Feed> boardList, ParseException e) {
                if (e == null && boardList.size() > 0) {
                    Log.d(LOG_TAG, "got board posts from LocalDB");
                    mAdapter = new FeedAdapter(boardList, getActivity());
                    boardRecyvleview.setAdapter(mAdapter);
                    boardRecyvleview.setItemAnimator(new DefaultItemAnimator());
                    progress.setVisibility(View.GONE);

                } else {
                    if (e == null && boardList.size() == 0) {
                        getFreshFeedPosts();
                    } else {
                        Snackbar.make(boardRecyvleview, getResources().getString(R.string.something_went_wrong),
                                Snackbar.LENGTH_LONG)
                                .setAction("OK", null).show();
                        progress.setVisibility(View.GONE);

                        Log.d(LOG_TAG, "error while fetching questions : " + e.getMessage());

                    }
                }
            }
        });

    }

    private void getFreshFeedPosts() {
        Feed.createQuery().findInBackground(new FindCallback<Feed>() {
            @Override
            public void done(final List<Feed> feedList, ParseException e) {
                if (e == null && feedList != null && feedList.size() > 0) {
                    Log.d(LOG_TAG, "got board posts from parse");
                    mAdapter = new FeedAdapter(feedList, getActivity());
                    boardRecyvleview.setAdapter(mAdapter);
                    boardRecyvleview.setItemAnimator(new DefaultItemAnimator());
                    pinFeedPosts(feedList);
                    progress.setVisibility(View.GONE);
                } else {

                    if (e == null && feedList != null && feedList.size() == 0) {
                        mAdapter = new FeedAdapter(feedList, getActivity());
                        boardRecyvleview.setAdapter(mAdapter);
                        pinFeedPosts(feedList);
                        progress.setVisibility(View.GONE);
                        Log.d(LOG_TAG, "get fresh board posts - with size 0");
                    } else {
                        Snackbar.make(boardRecyvleview, getResources().getString(R.string.something_went_wrong),
                                Snackbar.LENGTH_LONG)
                                .setAction("OK", null).show();
                        Log.d(LOG_TAG, "error while fetching board posts : " + e.getMessage());
                        progress.setVisibility(View.GONE);

                    }
                }

            }
        });
    }

    private void pinFeedPosts(final List<Feed> feedList) {

        ParseObject.unpinAllInBackground(ModelUtils.BOARD_PIN, feedList, new DeleteCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    return;
                }
                ParseObject.pinAllInBackground(ModelUtils.BOARD_PIN, feedList);
                Log.d(LOG_TAG, "pinned boardList : " + ModelUtils.BOARD_PIN);

            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        if(progress != null)
            progress.setVisibility(View.GONE);

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
                getFreshFeedPosts();
                return true;
        }
        return false;
    }

}
