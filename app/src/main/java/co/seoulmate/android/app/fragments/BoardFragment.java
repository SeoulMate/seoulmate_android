package co.seoulmate.android.app.fragments;

import android.app.Fragment;
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
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.List;

import co.seoulmate.android.app.AnalyticsTrackers;
import co.seoulmate.android.app.R;
import co.seoulmate.android.app.adapters.BoardAdapter;
import co.seoulmate.android.app.helpers.AConstants;
import co.seoulmate.android.app.model.Board;
import co.seoulmate.android.app.utils.ModelUtils;

/**
 * Created by hassanabid on 3/1/16.
 */
public class BoardFragment extends android.support.v4.app.Fragment {

    private static final String LOG_TAG = BoardFragment.class.getSimpleName();
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private RecyclerView boardRecyvleview;
    private LinearLayoutManager mLayoutManager;
    private BoardAdapter mAdapter;
    private ProgressBar progress;


    public BoardFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static BoardFragment newInstance(int sectionNumber) {
        BoardFragment fragment = new BoardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sendAnalytics();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG,"onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_board, container, false);
        boardRecyvleview = (RecyclerView) rootView.findViewById(R.id.board_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        boardRecyvleview.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        boardRecyvleview.setLayoutManager(mLayoutManager);
        progress = (ProgressBar) getActivity().findViewById(R.id.progressBoard);


        getBoardPosts();

        return rootView;
    }

    private void getBoardPosts() {

        progress.setVisibility(View.VISIBLE);
        Board.createLocalQuery().findInBackground(new FindCallback<Board>() {

            @Override
            public void done(List<Board> boardList, ParseException e) {
                if (e == null && boardList.size() > 0) {
                    Log.d(LOG_TAG, "got board posts from LocalDB");
                    mAdapter = new BoardAdapter(boardList, getActivity());
                    boardRecyvleview.setAdapter(mAdapter);
                    boardRecyvleview.setItemAnimator(new DefaultItemAnimator());
                    progress.setVisibility(View.GONE);

                } else {
                    if (e == null && boardList.size() == 0) {
                        getFreshBoardPosts();
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

    private void getFreshBoardPosts() {
        Board.createQuery().findInBackground(new FindCallback<Board>() {
            @Override
            public void done(final List<Board> boardList, ParseException e) {
                if (e == null && boardList != null && boardList.size() > 0) {
                    Log.d(LOG_TAG,"got board posts from parse");
                    mAdapter = new BoardAdapter(boardList,getActivity());
                    boardRecyvleview.setAdapter(mAdapter);
                    boardRecyvleview.setItemAnimator(new DefaultItemAnimator());
                    pinBoardPosts(boardList);
                    progress.setVisibility(View.GONE);
                } else {

                    if (e == null && boardList != null && boardList.size() == 0) {
                        mAdapter = new BoardAdapter(boardList, getActivity());
                        boardRecyvleview.setAdapter(mAdapter);
                        pinBoardPosts(boardList);
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

    private void pinBoardPosts(final List<Board> boardList) {

        ParseObject.unpinAllInBackground(ModelUtils.BOARD_PIN, boardList, new DeleteCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    return;
                } else {
                    Log.d(LOG_TAG, "unpinned boardList : " + ModelUtils.BOARD_PIN + " size :" + boardList.size());

                }
                ParseObject.pinAllInBackground(ModelUtils.BOARD_PIN, boardList, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d(LOG_TAG, "pinned boardList : " + ModelUtils.BOARD_PIN + " size :" + boardList.size());

                    }
                });

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
                getFreshBoardPosts();
                return true;
        }
        return false;
    }

    private void sendAnalytics() {

        try {
            ;
            AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP).setScreenName(AConstants.SCREEN_BOARD);
            AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP).send(new HitBuilders.ScreenViewBuilder().build());
        } catch (IllegalStateException e) {
        }
    }
}
