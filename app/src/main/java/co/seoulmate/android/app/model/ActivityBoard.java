package co.seoulmate.android.app.model;

import android.util.Log;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by hassankcdh on 2/11/15.
 */

@ParseClassName("ActivityBoard")
public class ActivityBoard extends ParseObject {

public static final String FOLLOW = "follow";
    public static final String LIKE = "like";
    public static final String COMMENT = "comment";

    int mCount;

    private static final String LOG_TAG = "Activity";

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getType() {
        return getString("type");
    }

    public void setType(String type) {
        put("type", type);
    }

    public String getContent() {
        return getString("content");
    }

    public void setContent(String content) {
        put("content", content);
    }



    public ParseUser getfromUser() {
        return getParseUser("fromUser");
    }

    public void setfromUser(ParseUser fromUser) {
        put("fromUser", fromUser);
    }

    public ParseUser gettoUser() {
        return getParseUser("toUser");
    }

    public void settoUser(ParseUser gettoUser) {
        put("toUser", gettoUser);
    }

    public void setBoard (Board board) {

        put("board",board);

    }

    public Board getBoard() {

        return (Board) get("board");

    }

    public static void findInBackground(String title,
                                        final GetCallback<ActivityBoard> callback) {
        ParseQuery<ActivityBoard> query = ParseQuery.getQuery(ActivityBoard.class);
        query.whereEqualTo("title", title);
        query.getFirstInBackground(new GetCallback<ActivityBoard>() {

            @Override
            public void done(ActivityBoard activity, ParseException e) {
                if (e == null) {
                    callback.done(activity, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }

    public static void findCountsinBackground(String objectId, final GetDataCallback callback){

        ParseQuery<ActivityBoard> query = ParseQuery.getQuery(ActivityBoard.class);
        query.whereEqualTo("type", ActivityBoard.LIKE);
        query.whereEqualTo("objectId", objectId);

        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {

                if(e == null) {


                } else {

//                    callback.done(i,e);

                }

            }
        });

    }


    public int getLikeCounts(String objectId) {


        ParseQuery<ParseObject> query = ParseQuery.getQuery("Activity");
        query.whereEqualTo("type", ActivityBoard.LIKE);
        query.whereEqualTo("objectId", objectId);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null) {
                    // The count request succeeded. Log the count
                    Log.d(LOG_TAG, "Post has " + count + " likes");
                    mCount = count;
                } else {
                    // The request failed
                    mCount = 0;
                }
            }
        });
        
        return mCount;

    }



    /**
     * Creates a query for talks with all the includes
     */
    private static ParseQuery<ActivityBoard> createQuery(String activityType,String objectId) {
        ParseQuery<ActivityBoard> query = new ParseQuery<ActivityBoard>(ActivityBoard.class);
        query.whereEqualTo("type", activityType);
        query.whereEqualTo("objectId", objectId);
		query.include("feed");
        
        return query;
    }

}
