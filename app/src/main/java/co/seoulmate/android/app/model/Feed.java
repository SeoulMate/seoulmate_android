package co.seoulmate.android.app.model;

import android.os.AsyncTask;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import co.seoulmate.android.app.utils.ModelUtils;

/**
 * Created by hassankcdh on 2/11/15.
 */
@ParseClassName("Feed")
public class Feed extends ParseObject {

    private static final String LOG_TAG = "Feed";
    private  static List<Feed> result ;
    
    public Feed() {
//        Log.d(LOG_TAG,"Feed object saved!");
        
    }

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public int getCategory() {
        return getInt("category");
    }

    public void setCategory(int category) {
        put("category", category);
    }

    public void setUniversity(String uni ) { put("university",uni);}
    public String getUniversity() {return getString("university");}

    public int getPosition() {
        return getInt("position");
    }

    public void setPosition(int position) {
        put("position", position);
    }


    public List<ParseUser> getVoters(){ return getList(ModelUtils.VOTERS);}
    public void addVoter(ParseUser user,Feed f){ f.addUnique(ModelUtils.VOTERS, user);}

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public String getcreatedAt() {

        return convertDateToString(getCreatedAt());

    }

    public ParseFile getPhoto() {
        return getParseFile("photo");
    }

    public void setPhoto(ParseFile photo) {
        put("photo", photo);
    }

    public void setThumbnail(ParseFile photo) {

        put("thumb", photo);

    }

    public String getlink() {
        return getString("link");
    }

    public void setlink(String link) {
        put("link", link);
    }

    public boolean getSolvedStatus() { return getBoolean("solved"); }
    public void setSolvedStatus(Boolean solved) { put("solved",solved); }


    public ParseFile getThumbnail() {
        return getParseFile("thumb");
    }
    
    public void setcreatedAt(Date now) {
        
        put("createdAt", now);
        
    }

    public void setCommentCount(int commentCount) {

        put("commentCount", commentCount);

    }

    public int getCommentCount() {

        return getInt("commentCount");

    }

    /**
     * Creates a query for talks with all the includes
     */
    public static ParseQuery<Feed> createQuery(int pos) {
        ParseQuery<Feed> query = new ParseQuery<Feed>(Feed.class);
        query.include("user");
//        if(pos != -1)
//         query.whereEqualTo("position", pos);
        query.addDescendingOrder("createdAt");
        return query;
    }

    /**
     * Creates a query for talks with all the includes
     */
    private static ParseQuery<Feed> createLocalQuery(int pos) {
        ParseQuery<Feed> query = new ParseQuery<Feed>(Feed.class).fromLocalDatastore();
//        if(pos != -1)
//            query.whereEqualTo("position", pos);
		query.include("user");
        return query;
    }


    /**
     * Creates a query for my feed posts with all the includes
     */
    private static ParseQuery<Feed> createMyProfileQuery(int pos) {
        ParseQuery<Feed> query = new ParseQuery<Feed>(Feed.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include("user");

        return query;
    }

    /**
     * Creates a query for single feed item
     */
    private static ParseQuery<Feed> createSingleBoardQuery(int pos,String objectId) {
        ParseQuery<Feed> query = new ParseQuery<Feed>(Feed.class);
        query.whereEqualTo("objectId", objectId);
        query.include("user");
        return query;
    }


    /**
     * Retrieves the set of all Board posts, ordered by time. Uses the cache if
     * possible.
     */
    public static void findMyPostsInBackground(
            final FindCallback<Feed> callback) {

        final ParseQuery<Feed> query = Feed.createMyProfileQuery(0);
        // Retrieve the most recent ones
        query.orderByDescending("createdAt");

        new AsyncTask<Void,Void,ParseException>(){

            @Override
            protected ParseException doInBackground(Void... voids) {

                ParseException exception = null;
                try {
                    result = query.find();
                } catch (ParseException e) {
                    exception = e;
                    Log.d(LOG_TAG, "exception in fetching my posts : " + e.getMessage());
                }
                return exception;
            }

            @Override
            protected void onPostExecute(ParseException e) {

                if (result != null && result.size() > 0 && e== null) {

                    Log.d(LOG_TAG, "got my feeds | size : " + result.size());
                    callback.done(result, e);

                }
                else {
                    Log.d(LOG_TAG, "couldn't find any feed from user");
                    callback.done(null, e);


                }

            }
        }.execute();
    }


    private String convertDateToString(Date date) {

        if(date == null) 
            return new SimpleDateFormat("EEE, MMM d, yyyy").format(new Date());
        Format formatter = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm" );
        String s = formatter.format(date);

        return s;

    }
    
    private void unPinFeed(String feed) {

        try {
            ParseObject.unpinAll(feed);
        } catch (ParseException e) {
            Log.d(LOG_TAG, " Parse exception : " + e.getMessage());

        }
        
    }
    
    public static ParseQuery<Feed> createCompoundQuery(int position) {


        // First, query for the friends whom the current user follows
        ParseQuery<Activity> likeActivitiesQuery = new ParseQuery<Activity>("Activity");
        likeActivitiesQuery.whereMatches("type", Activity.LIKE);
//        likeActivitiesQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        likeActivitiesQuery.whereEqualTo("feed", ParseUser.getCurrentUser());

        // Get the photos from the Users returned in the previous query
        ParseQuery<Feed> photosFromFollowedUsersQuery = new ParseQuery<Feed>("Feed");
        photosFromFollowedUsersQuery.whereMatchesKeyInQuery("objectId", "objectId", likeActivitiesQuery);
//        photosFromFollowedUsersQuery.whereExists("image");

        // Get the current user's photos
        ParseQuery<Feed> photosFromCurrentUserQuery = new ParseQuery<Feed>("Feed");
        photosFromCurrentUserQuery.whereEqualTo("user", ParseUser.getCurrentUser());
//        photosFromCurrentUserQuery.whereExists("image");

        // We create a final compound query that will find all of the photos that were
        // taken by the user's friends or by the user
        ParseQuery<Feed> query = ParseQuery.or(Arrays.asList(photosFromFollowedUsersQuery, photosFromCurrentUserQuery));
        query.include("user");
        query.orderByDescending("createdAt");

        return query;
        
    }


    /**
     * Creates a local query for questions with all the includes
     */
    public static ParseQuery<Feed> createLocalQuery() {

        ParseQuery<Feed> query = new ParseQuery<Feed>(Feed.class);
        query.include(ModelUtils.USER);
        query.setLimit(ModelUtils.DEFAULT_LIMIT);
        query.orderByDescending(ModelUtils.CREATED_AT);
        query.fromLocalDatastore();

        return query;
    }

    /**
     * Creates a query for questions with all the includes
     */
    public static ParseQuery<Feed> createQuery() {
        ParseQuery<Feed> query = new ParseQuery<Feed>(Feed.class);
        query.include(ModelUtils.USER);
        query.setLimit(ModelUtils.DEFAULT_LIMIT);
        query.orderByDescending(ModelUtils.CREATED_AT);
        return query;
    }

    public static ParseQuery<Feed> createSingleLocalQuery(String objectId) {

        ParseQuery<Feed> query = new ParseQuery<Feed>(Feed.class);
        query.include(ModelUtils.USER);
        query.whereEqualTo(ModelUtils.OBJECT_ID, objectId);
        query.fromLocalDatastore();

        return query;
    }

    public static ParseQuery<Feed> createSingleQuery(String objectId) {

        ParseQuery<Feed> query = ParseQuery.getQuery("Feed");
        query.include(ModelUtils.USER);
        query.whereEqualTo(ModelUtils.OBJECT_ID, objectId);
        return query;
    }
}
