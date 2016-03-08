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

import java.util.Arrays;
import java.util.List;

import co.seoulmate.android.app.utils.ModelUtils;

/**
 * Created by hassankcdh on 2/11/15.
 */

@ParseClassName("Activity")
public class Activity extends ParseObject {

public static final String FOLLOW = "follow";
    public static final String LIKE = "like";
    public static final String COMMENT = "comment";

    int mCount;

    private static final String LOG_TAG = "Activity";
  
  
    private  static List<Board> result ;

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

    public boolean getStatus(){ return getBoolean("solved"); };
    public void setStatus(Boolean status)  { put("solved", status); }

    public void setPriority(int priority) {put("priority",priority);}
    public int getPriority() { return getInt("priority");}

    public List<ParseUser> getVoters(){ return getList(ModelUtils.VOTERS);}
    public void addVoter(ParseUser user,Activity b){ b.addUnique(ModelUtils.VOTERS, user);}

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
    public void removeVoter(ParseUser user,Activity b){ b.removeAll(ModelUtils.VOTERS, Arrays.asList(user));}

    public void setFeed (Feed feed) {
        
        put("feed",feed);
        
    }
    
    public Feed getFeed() {
        
        return (Feed) get("feed");
        
    }

    public static void findInBackground(String title,
                                        final GetCallback<Activity> callback) {
        ParseQuery<Activity> query = ParseQuery.getQuery(Activity.class);
        query.whereEqualTo("title", title);
        query.getFirstInBackground(new GetCallback<Activity>() {

            @Override
            public void done(Activity activity, ParseException e) {
                if (e == null) {
                    callback.done(activity, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }
    
    public static void findCountsinBackground(String objectId, final GetDataCallback callback){

        ParseQuery<Activity> query = ParseQuery.getQuery(Activity.class);
        query.whereEqualTo("type", Activity.LIKE);
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
        query.whereEqualTo("type", Activity.LIKE);
        query.whereEqualTo("objectId", objectId);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, com.parse.ParseException e) {
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
    private static ParseQuery<Activity> createQuery(String activityType,String objectId) {
        ParseQuery<Activity> query = new ParseQuery<Activity>(Activity.class);
        query.whereEqualTo("type", activityType);
        query.whereEqualTo("objectId", objectId);
		query.include("feed");
        
        return query;
    }

    /**
     * Creates a query for questions with all the includes
     */
    public static ParseQuery<Activity> createQuery(Feed q) {

        ParseQuery<Activity> query = new ParseQuery<Activity>(Activity.class);
        query.whereEqualTo(ModelUtils.FEED, q);
        query.include(ModelUtils.FROM_USER);
        query.orderByDescending(ModelUtils.PRIORITY);
        query.whereEqualTo("type", Activity.COMMENT);


        return query;
    }


    /**
     * Creates a local query for questions with all the includes
     */
    public static ParseQuery<Activity> createLocalQuery(Feed q) {

        ParseQuery<Activity> query = new ParseQuery<Activity>(Activity.class);
        query.whereEqualTo(ModelUtils.FEED, q);
        query.include(ModelUtils.FROM_USER);
        query.orderByDescending(ModelUtils.PRIORITY);
        query.whereEqualTo("type", Activity.COMMENT);
        query.fromLocalDatastore();

        return query;
    }


}
