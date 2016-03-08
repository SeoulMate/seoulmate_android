package co.seoulmate.android.app.push;

import android.app.Activity;
import android.app.Notification;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import co.seoulmate.android.app.activities.BoardDetailActivity;
import co.seoulmate.android.app.activities.DispatchActivity;
import co.seoulmate.android.app.activities.FeedDetailActivity;
import co.seoulmate.android.app.activities.NotificationActivity;
import co.seoulmate.android.app.helpers.Constants;
import co.seoulmate.android.app.helpers.PreferencesHelper;
import co.seoulmate.android.app.utils.ModelUtils;

/**
 * Created by hassankcdh on 3/3/15.
 */
public class SMPushReceive extends ParsePushBroadcastReceiver {

    private static final String LOG_TAG = SMPushReceive.class.getSimpleName();
    public static final String PAYLOAD = "payload";
    private static final String TYPE = "t";

    private static final String FROM_USER = "fu";
    private static final String OBJECT_ID = "postId";




    public SMPushReceive() {
        super();
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        Log.d(LOG_TAG, "onPushReceive");

    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        if(context == null || intent == null)
            return;
        super.onPushOpen(context, intent);
        Log.d(LOG_TAG, "onPushOpen");
        intent = new Intent(context,NotificationActivity.class);
        Class<? extends Activity> cls = getActivity(context, intent);
        if (Build.VERSION.SDK_INT >= 16) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(cls);
            stackBuilder.addNextIntent(intent);
            stackBuilder.startActivities();
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }


       /* String uriString = null;
        JSONObject pushData = null;
        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
            uriString = pushData.optString(PAYLOAD);
            Log.d(LOG_TAG, "onPushOpen  payload : " + uriString + "  alert : " +
                    pushData.optString("alert"));

        } catch (JSONException e) {
            Log.v(LOG_TAG, "Unexpected JSONException when receiving push data: ", e);
        }
        Class<? extends Activity> cls = getActivity(context, intent);
        Intent intentAlert = null;
        if(pushData != null && ParseUser.getCurrentUser() != null) {

            if(uriString.equals(Constants.ACTIVITY_FEED_DETAIL)) {
                Log.d(LOG_TAG, "onPushOpen | ACTIVITY_FEED_DETAIL");
                String objectId = pushData.optString(OBJECT_ID);
*//*                intentAlert = new Intent(context,FeedDetailActivity.class);
                intentAlert.putExtra(ModelUtils.OBJECT_ID,objectId);*//*
                PreferencesHelper.writeToPreferenceBoardNotiId(context,objectId);

            } else if(uriString.equals(Constants.ACTIVITY_BOARD_DETAIL)) {

                String objectId = pushData.optString(OBJECT_ID);
                Log.d(LOG_TAG, "onPushOpen | ACTIVITY_BOARD_DETAIL --> : " + objectId);
*//*                intentAlert = new Intent(context,BoardDetailActivity.class);
                intentAlert.putExtra(ModelUtils.OBJECT_ID,objectId);*//*
                PreferencesHelper.writeToPreferenceQuestionNotiId(context, objectId);


            } else if (uriString.equals(Constants.ACTIVITY_ANSWER_POST)) {

                Log.d(LOG_TAG, "onPushOpen | ACTIVITY_ANSWER");
                String objectId = pushData.optString(OBJECT_ID);
                intentAlert = new Intent(context,FeedDetailActivity.class);
                intentAlert.putExtra(ModelUtils.OBJECT_ID,objectId);
                PreferencesHelper.writeToPreferenceQuestionNotiId(context, objectId);


            }else {
                Log.d(LOG_TAG, "onPushOpen | ELSE");
                intentAlert = new Intent(context, NotificationActivity.class);
            }
        } else {
            Log.d(LOG_TAG, "onPushOpen - either user null or pushdata null");
            intentAlert = new Intent(context,DispatchActivity.class);


        }
        intentAlert = new Intent(context, NotificationActivity.class);
        if(intentAlert == null)
            return;
*/
/*        intent = new Intent(context,NotificationActivity.class);
        Class<? extends Activity> cls = getActivity(context, intent);
        if (Build.VERSION.SDK_INT >= 16) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(cls);
            stackBuilder.addNextIntent(intent);
            stackBuilder.startActivities();
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }*/

    }



    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);

        Log.d(LOG_TAG, "onPushDismiss");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
       /* intent = new Intent(context,NotificationActivity.class);
        Class<? extends Activity> cls = getActivity(context, intent);
        if (Build.VERSION.SDK_INT >= 16) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(cls);
            stackBuilder.addNextIntent(intent);
            stackBuilder.startActivities();
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
*/
    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return super.getSmallIconId(context, intent);
    }

/*    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context, intent);


    }*/


    @Override
    protected Notification getNotification(Context context, Intent intent) {

        Log.d(LOG_TAG, "getNotification");
        return super.getNotification(context, intent);


    }
}
