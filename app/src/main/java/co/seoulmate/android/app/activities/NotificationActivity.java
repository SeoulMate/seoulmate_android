package co.seoulmate.android.app.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import co.seoulmate.android.app.R;
import co.seoulmate.android.app.helpers.Constants;
import co.seoulmate.android.app.model.Notification;
import co.seoulmate.android.app.utils.ModelUtils;

public class NotificationActivity extends AppCompatActivity {

    private AlertsAdapter alertsAdapter;
    private ListView alertsView;
    private LinearLayout noAlertsView;

    public static final String ALERT_MSG_ID = "messageId";

    public static final String LOG_TAG = NotificationActivity.class.getSimpleName();

    boolean isFeed = true;
    private View mContentView;
    private View mLoadingView;
    private int mShortAnimationDuration;
    List<Notification> results;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextAppearance(this, R.style.Category_Title);

        // Set up the views
        alertsView = (ListView) findViewById(R.id.alerts_view);
        noAlertsView = (LinearLayout) findViewById(R.id.no_alerts_view);

        mContentView = findViewById(R.id.no_alerts_view);
        mLoadingView = findViewById(R.id.loading_spinner_noti);

        // Initially hide the content view.
        mContentView.setVisibility(View.GONE);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        setTitle(getResources().getString(R.string.action_alerts));

        // Set up the adapter for the alert list
        alertsAdapter = new AlertsAdapter(this);
        alertsView.setAdapter(alertsAdapter);
        alertsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Notification message = alertsAdapter.getItem(position);
                if(message.getPostId() == null) {
                    if (message.getUrl() != null) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(message.getUrl()));
                        if (canResolveIntent(i)) {
                            startActivity(i);
                        }
                    }
                    return;
                }
                Intent intent= null;
                switch(message.getPayload()) {

                    case Constants.ACTIVITY_FEED_DETAIL:
                        intent = new Intent(NotificationActivity.this,FeedDetailActivity.class);
                        intent.putExtra(ModelUtils.OBJECT_ID,message.getPostId());
                        startActivity(intent);
                        break;

                    case Constants.ACTIVITY_ANSWER_POST:
                        intent = new Intent(NotificationActivity.this,FeedDetailActivity.class);
                        intent.putExtra(ModelUtils.OBJECT_ID,message.getPostId());
                        startActivity(intent);
                        break;

                    case Constants.ACTIVITY_BOARD_DETAIL:
                        intent = new Intent(NotificationActivity.this,BoardDetailActivity.class);
                        intent.putExtra(ModelUtils.OBJECT_ID,message.getPostId());
                        startActivity(intent);
                        break;

                    default:
                        break;
                }


            }
        });
    }

    public void checkForAlerts() {
        alertsAdapter.clear();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null ) {
            finish();
        }

        new RemoteDataTask().execute(0);


    }

    @Override
    public void onResume() {
        super.onResume();
        checkForAlerts();
    }

    private class AlertsAdapter extends ArrayAdapter<Notification> {

        private ViewHolder holder;
        private LayoutInflater inflater;

        public AlertsAdapter(Context context) {
            super(context, 0);
            inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {

            if (v == null) {
                v = inflater.inflate(R.layout.list_item_alert, parent, false);
                holder = new ViewHolder();
                holder.surveyTitle = (TextView) v
                        .findViewById(R.id.survey_title);
                holder.time = (TextView) v.findViewById(R.id.noti_time);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            Notification message = getItem(position);
            TextView surveyTitle = holder.surveyTitle;
            TextView notiTime = holder.time;
            notiTime.setText(Notification.convertDateToString(message.getCreatedAt()));
            surveyTitle.setText(message.getTitle());

            return v;
        }

    }

    private static class ViewHolder {
        TextView surveyTitle;
        TextView time;
    }

    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = getPackageManager()
                .queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }



    private void crossfade() {

        mLoadingView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoadingView.setVisibility(View.GONE);
                    }
                });

    }


    private class RemoteDataTask extends AsyncTask<Integer, Void, Void> {
        protected Void doInBackground(Integer... params) {

            ParseQuery<Notification> messageQuery = new ParseQuery<Notification>(Notification.class);
            messageQuery.addDescendingOrder("createdAt");
            messageQuery.setLimit(20);

            Log.d(LOG_TAG, "doinBackground !");

            try {
                Log.d(LOG_TAG, "fetch notifications");
                results = messageQuery.find();

            } catch (ParseException e) {
                Log.d(LOG_TAG, "exception --> " + e.getMessage());

            }
            return null;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(LOG_TAG, "onPostExecute !");
//            progressDialog.dismiss();
            crossfade();
            if (results != null) {
                if(!results.isEmpty()) {
                    Log.d(LOG_TAG, "on success Task ");

                    for (Notification message : results) {
                        alertsAdapter.add(message);
                    }
                }
            } else {
                if (alertsView != null)
                    alertsView.setEmptyView(noAlertsView);
                mContentView.setAlpha(0f);
                mContentView.setVisibility(View.VISIBLE);

                // Animate the content view to 100% opacity, and clear any animation
                // listener set on the view.
                mContentView.animate()
                        .alpha(1f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(null);
                crossfade();

            }

        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
