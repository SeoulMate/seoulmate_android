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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.seoulmate.android.app.utils.ModelUtils;

/**
 * Created by hassankcdh on 2/11/15.
 */
@ParseClassName("Board")
public class Board extends ParseObject {

    private static final String LOG_TAG = "Board";
    private  static List<Board> result ;
    public static  final int  ALL_UNIVERSITIES_POS = 100;
    public static  final int  ALL_UNIVERSITIES_LIMIT = 20;



    public Board() {

    }

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getContent() {
        return getString("content");
    }

    public void setContent(String content) {
        put("content", content);
    }

    public List<ParseUser> getVoters(){ return getList(ModelUtils.VOTERS);}
    public void addVoter(ParseUser user,Board b){ b.addUnique(ModelUtils.VOTERS, user);}

    public void removeVoter(ParseUser user,Board b){ b.removeAll(ModelUtils.VOTERS, Arrays.asList(user));}


    public ParseFile getPhoto() {
        return getParseFile("photo");
    }

    public void setPhoto(ParseFile photo) {
        put("photo", photo);
    }

    public void setThumbnail(ParseFile photo) {

        put("thumb", photo);
        
    }

    public ParseFile getThumbnail() {
        return getParseFile("thumb");
    }


    public String getlink() {
        return getString("link");
    }

    public void setlink(String link) {
        put("link", link);
    }


    public int getCategory() {
        return getInt("category");
    }

    public void setCategory(int category) {
        put("category", category);
    }

    public int getViews() {
        return getInt("views");
    }

    public void setViews(int views) {
        put("views", views);
    }



    /**
     * *
     * @return university position , -1 for all
     */
    public int getPosition() {
        return getInt("position");
    }

    public void setPosition(int position) {
        put("position", position);
    }
    

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public String getcreatedAt() {

        return convertDateToString(getCreatedAt());

    }
    
    public void setcreatedAt(Date now) {
        
        put("createdAt", now);
        
    }

    public List<String> getViewers() {

        return getList("viewers");
    }
    
    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract static class BoardFindCallback implements FindCallback<Board> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(List<Board> objects, ParseException e) {
            if (!calledCallback) {
                if (objects != null) {
                    // We got a result, use it.
                    calledCallback = true;
                    doneOnce(objects, null);
                } else if (!isCachedResult) {
                    // We got called back twice, but got a null result both
                    // times. Pass on the latest error.
                    doneOnce(null, e);
                }
            }
            isCachedResult = false;
        }

        /**
         * Override this method with the callback that should only be called
         * once.
         */
        protected abstract void doneOnce(List<Board> objects, ParseException e);
    }



    /**
     * Retrieves the set of all Board posts, ordered by time. Uses the cache if
     * possible.
     */
    public static void findInBackground(int position,
            final FindCallback<Board> callback) {

        final ParseQuery<Board> query = Board.createLocalQuery(position);
        // Retrieve the most recent ones
        query.orderByDescending("createdAt");

        query.findInBackground(new BoardFindCallback() {
            @Override
            protected void doneOnce(List<Board> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {

                    Log.d(LOG_TAG, "got objects from Local Datastore | size : " + objects.size());
                    result = objects;
                    callback.done(result, e);
//                    ParseObject.pinAllInBackground("korean_language", objects);
                } else {

                    callback.done(null, e);
                }

            }
        });
    }


    /**
     * Retrieves the set of all Board posts, ordered by time. Uses the cache if
     * possible.
     */
    public static void findInBackground4Home(int position,

                                        final FindCallback<Board> callback) {

        Log.d(LOG_TAG, "findInBackground4Home is called");
        final ParseQuery<Board> query = Board.createLocalQueryHome(position);
        // Retrieve the most recent ones
        query.orderByDescending("createdAt");

        new AsyncTask<Void,Void,ParseException>() {

            @Override
            protected ParseException doInBackground(Void... voids) {

                ParseException exception=null;
                try {
                   result = query.find();
                }catch (ParseException e) {
                    exception = e;
                    Log.d(LOG_TAG, "exception while fetching items from home : " + e.getMessage());
                }
                return exception;
            }

            @Override
            protected void onPostExecute(ParseException e) {

                if (result != null && result.size() > 0  && e == null) {

                    Log.d(LOG_TAG, "got objects from Local Datastore | size : " + result.size());
                    callback.done(result, e);
                }
                else {

                    callback.done(null, e);
                }


            }
        }.execute();
/*
        query.findInBackground(new BoardFindCallback() {
            @Override
            protected void doneOnce(List<Board> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {

                    Log.d(LOG_TAG, "got objects from Local Datastore | size : " + objects.size());
                    result = objects;
                    callback.done(result, e);
                }
                else {

                    callback.done(null, e);
                }
                
            }
        });*/
    }


    /**
     * Retrieves the set of all Events/Posts, ordered by time. Uses the cache if
     * possible.
     */
    public static void findInBackgroundwhenRefresh(final int position,
            final FindCallback<Board> callback) {
        ParseQuery<Board> query = Board.createQuery(position);

        query.findInBackground(new BoardFindCallback() {
            @Override
            protected void doneOnce(List<Board> objects, ParseException e) {
                if (objects != null) {
                    try {
                        ParseObject.unpinAll("board_" + position);
                    } catch (ParseException exception) {

                        Log.d(LOG_TAG, "Parse exception while unpinning : " + e.getMessage());

                    }
                }
                if (e == null) {

                    ParseObject.pinAllInBackground("board_" + position, objects);
                }
                callback.done(objects, e);
            }
        });
    }

    /**
     * Retrieves the set of all Board items for home, ordered by time. Uses the cache if
     * possible.
     */
    public static void findInBackground4HomeRefresh(final int position,
                                                   final FindCallback<Board> callback) {
        ParseQuery<Board> query = Board.createQueryHome(position);

        query.findInBackground(new BoardFindCallback() {
            @Override
            protected void doneOnce(List<Board> objects, ParseException e) {
                if (e == null) {
                    if (objects != null && !objects.isEmpty()) {
                        try {
                            ParseObject.unpinAll("board_" + ALL_UNIVERSITIES_POS);
                        } catch (ParseException exception) {

                            Log.d(LOG_TAG, "Parse exception while unpinning : " + e.getMessage());

                        }
                        ParseObject.pinAllInBackground("board_" + ALL_UNIVERSITIES_POS, objects);
                        callback.done(objects, e);

                    } else {
                        callback.done(objects, e);


                    }

                } else {
                    callback.done(null, e);
                    Log.d(LOG_TAG, "Board - Exception : " + e.getMessage());

                }
            }
        });
    }

    /**
     * Gets the data for a single Board item. We use this instead of calling fetch on
     * a ParseObject so that we can use query cache if possible.
     */
    public static void getInBackground(final String objectId,int pos,
                                       final GetCallback<Board> callback) {
        ParseQuery<Board> query = Board.createSingleBoardQuery(pos, objectId);
        query.findInBackground(new BoardFindCallback() {
            @Override
            protected void doneOnce(List<Board> objects, ParseException e) {
                if (objects != null) {
                    // Emulate the behavior of getFirstInBackground by using
                    // only the first result.
                    if (objects.size() < 1) {
                        callback.done(null, new ParseException(
                                ParseException.OBJECT_NOT_FOUND,
                                "No post found  with id " + objectId + " was found."));
                    } else {
                        callback.done(objects.get(0), e);
                    }
                } else {
                    callback.done(null, e);
                }
            }
        });
    }

    public static void findInBackground(String title,int positon,
                                        final GetCallback<Board> callback) {
        ParseQuery<Board> query = ParseQuery.getQuery(Board.class);
        query.whereEqualTo("title", title);
        query.getFirstInBackground(new GetCallback<Board>() {

            @Override
            public void done(Board post, ParseException e) {
                if (e == null) {
                    callback.done(post, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }



    /**
     * Creates a query for board items with all the includes
     */
    public static ParseQuery<Board> createQuery(int pos) {
        ParseQuery<Board> query = new ParseQuery<Board>(Board.class);
		query.include("user");
        List<Integer> postions = new ArrayList<Integer>();
        postions.add(pos);
        postions.add(Board.ALL_UNIVERSITIES_POS);
        if(pos != -1) {
//            query.whereEqualTo("position", pos);
            query.whereContainedIn("position", postions);
        }

        query.orderByDescending("createdAt");

        return query;
    }

    /**
     * Creates a query for board items (home)  with all the includes
     */
    public static ParseQuery<Board> createQueryHome(int pos) {
        ParseQuery<Board> query = new ParseQuery<Board>(Board.class);
        query.include("user");
        query.whereEqualTo("position", ALL_UNIVERSITIES_POS);
        query.orderByDescending("createdAt,views");
        query.setLimit(ALL_UNIVERSITIES_LIMIT);
//        query.orderByDescending("createdAt,views");

        return query;
    }
    

    /**
     * Creates a query for talks with all the includes
     */
    private static ParseQuery<Board> createSingleBoardQuery(int pos,String objectId) {
        ParseQuery<Board> query = new ParseQuery<Board>(Board.class);
        query.whereEqualTo("objectId", objectId);
        query.include("user");
        return query;
    }

    /**
     * Creates a query for talks with all the includes
     */
    private static ParseQuery<Board> createMyProfileQuery(int pos) {
        ParseQuery<Board> query = new ParseQuery<Board>(Board.class);
        query.include("user");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
		query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
//      query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.setMaxCacheAge(TimeUnit.DAYS.toMinutes(5));
        return query;
    }


    /**
     * Retrieves the set of all Board posts, ordered by time. Uses the cache if
     * possible.
     */
    public static void findMyPostsInBackground(
                                        final FindCallback<Board> callback) {

        final ParseQuery<Board> query = Board.createMyProfileQuery(0);
        // Retrieve the most recent ones
        query.orderByDescending("createdAt");

        query.findInBackground(new BoardFindCallback() {
            @Override
            protected void doneOnce(List<Board> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {

                    Log.d(LOG_TAG, "got objects from Local Datastore | size : " + objects.size());
                    result = objects;
                } else {


                }
                callback.done(result, e);
            }
        });
    }

    /**
     * Creates a query for Board posts with all the includes
     */
    private static ParseQuery<Board> createLocalQuery(int position) {
        ParseQuery<Board> query = new ParseQuery<Board>(Board.class);
		query.include("user");
        List<Integer> postions = new ArrayList<Integer>();
        postions.add(position);
        postions.add(Board.ALL_UNIVERSITIES_POS);
        if(position != -1) {
//            query.whereEqualTo("position", pos);
            query.whereContainedIn("position", postions);
        }
//		query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
        query.fromLocalDatastore();
//        query.setMaxCacheAge(TimeUnit.DAYS.toMinutes(60));
        return query;
    }


    /**
     * Creates a query for Board posts (home) with all the includes
     */
    private static ParseQuery<Board> createLocalQueryHome(int position) {
        ParseQuery<Board> query = new ParseQuery<Board>(Board.class).fromLocalDatastore();
        query.include("user");
        query.whereEqualTo("position", ALL_UNIVERSITIES_POS);
        query.setLimit(ALL_UNIVERSITIES_LIMIT);
        return query;
    }



    private String convertDateToString(Date date) {

        if(date == null) 
            return new SimpleDateFormat("EEE, MMM d, yyyy").format(new Date());
//        Format formatter = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm" );
        Format formatter = new SimpleDateFormat("EEE, MMM d, yyyy" );
        String s = formatter.format(date);

        return s;

    }
    
    private void unPinBoard(String board) {

        try {
            ParseObject.unpinAll(board);
        } catch (ParseException e) {
            Log.d(LOG_TAG, " Parse exception : " + e.getMessage());

        }
        
    }

    public ParseQuery<Board> create() {

        // First, query for the friends whom the current user follows
        ParseQuery<Activity> followingActivitiesQuery = new ParseQuery<Activity>(Activity.class);
        followingActivitiesQuery.whereMatches("type", "follow");
        followingActivitiesQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());

        // Get the photos from the Users returned in the previous query
        ParseQuery<Board> photosFromFollowedUsersQuery = new ParseQuery<Board>(Board.class);
        photosFromFollowedUsersQuery.whereMatchesKeyInQuery("user", "toUser", followingActivitiesQuery);
        photosFromFollowedUsersQuery.whereExists("image");

        // Get the current user's photos
        ParseQuery<Board> photosFromCurrentUserQuery = new ParseQuery<Board>(Board.class);
        photosFromCurrentUserQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        photosFromCurrentUserQuery.whereExists("image");

        // We create a final compound query that will find all of the photos that were
        // taken by the user's friends or by the user
        ParseQuery<Board> query = ParseQuery.or(Arrays.asList(photosFromFollowedUsersQuery,
                photosFromCurrentUserQuery));
        query.include("user");
        query.orderByDescending("createdAt");

        return query;
    }


    private ParseQuery<Board> buildParseQuery() {
        ParseQuery<Activity> followingActivitiesQuery = ParseQuery.getQuery(Activity.class);
        followingActivitiesQuery.whereEqualTo("type", Activity.FOLLOW);
        followingActivitiesQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());

        // Using the activities from the query above, we find all of the photos taken by
        // the friends the current user is following
        ParseQuery<Board> photosFromFollowedUsersQuery = ParseQuery.getQuery(Board.class);
        photosFromFollowedUsersQuery.whereMatchesKeyInQuery("user", "toUser", followingActivitiesQuery);
        photosFromFollowedUsersQuery.whereExists("image");

        ParseQuery<Board> photosFromCurrentUserQuery = ParseQuery.getQuery(Board.class);
        photosFromCurrentUserQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        photosFromCurrentUserQuery.whereExists("image");

        ArrayList<ParseQuery<Board>> queries = new ArrayList<ParseQuery<Board>>();
        queries.add(photosFromFollowedUsersQuery);
        queries.add(photosFromCurrentUserQuery);
        ParseQuery<Board> query = ParseQuery.or(queries);
        query.include("user");
        query.orderByDescending("createdAt");
        return query;
    }


    /**
     * Creates a local query for questions with all the includes
     */
    public static ParseQuery<Board> createLocalQuery() {

        ParseQuery<Board> query = new ParseQuery<Board>(Board.class);
        query.whereEqualTo(ModelUtils.POSITION,ALL_UNIVERSITIES_POS);
        query.include(ModelUtils.USER);
        query.setLimit(ModelUtils.DEFAULT_LIMIT);
        query.orderByDescending(ModelUtils.CREATED_AT);
        query.fromLocalDatastore();

        return query;
    }

    /**
     * Creates a query for questions with all the includes
     */
    public static ParseQuery<Board> createQuery() {
        ParseQuery<Board> query = new ParseQuery<Board>(Board.class);
        query.whereEqualTo(ModelUtils.POSITION, ALL_UNIVERSITIES_POS);
        query.include(ModelUtils.USER);
        query.setLimit(ModelUtils.DEFAULT_LIMIT);
        query.orderByDescending(ModelUtils.CREATED_AT);
        return query;
    }



    public static ParseQuery<Board> createSingleLocalQuery(String objectId) {

        ParseQuery<Board> query = new ParseQuery<Board>(Board.class);
        query.include(ModelUtils.USER);
        query.whereEqualTo(ModelUtils.OBJECT_ID, objectId);
        query.fromLocalDatastore();

        return query;
    }

    public static ParseQuery<Board> createSingleQuery(String objectId) {

        ParseQuery<Board> query = ParseQuery.getQuery("Board");
        query.include(ModelUtils.USER);
        query.whereEqualTo(ModelUtils.OBJECT_ID, objectId);
        return query;
    }
    
}
