/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.seoulmate.android.app.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.parse.ParseUser;

/**
 * Easy storage and retrieval of preferences.
 */
public class PreferencesHelper {

    private static final String USER_PREFERENCES = "userPreferences";
    private static final String USER_PREFERENCES_ACTIVITY = "userPreferences_activity";
    private static final String PREFERENCE_FIRST_NAME = USER_PREFERENCES + ".firstName";
    private static final String PREFERENCE_LAST_INITIAL = USER_PREFERENCES + "lastName";
    private static final String PREFERENCE_TAG = USER_PREFERENCES + ".tag";
    private static final String PREFERENCE_CATEGORY_NO = USER_PREFERENCES_ACTIVITY + ".catno";
    private static final String PREFERENCE_QUESTION_ID = "feedId";
    private static final String PREFERENCE_BOARD_ID = "boardId";
    private static final String PREFERENCE_QUESTION_NOTIFICATION = "feedNoti";
    private static final String PREFERENCE_BOARD_NOTIFICATION = "boardNoti";



    private PreferencesHelper() {
        //no instance
    }


    public static void writeToPreferences(Context context, ParseUser player,int tagPos) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(PREFERENCE_FIRST_NAME, player.getString("name"));
        editor.putString(PREFERENCE_LAST_INITIAL, player.getString("name"));
        Log.d(USER_PREFERENCES,"tagPos :" + tagPos);
        editor.putInt(PREFERENCE_TAG, tagPos);
        editor.apply();
        editor.commit();
    }

    public static void writeToActivityPreferences(Context context,int catPos) {
        SharedPreferences.Editor editor = getActivityEditor(context);
        editor.putInt(PREFERENCE_CATEGORY_NO, catPos);
        editor.apply();
        editor.commit();
    }

    public static void writeToPreferenceQuestionNotiId(Context context,String objectId) {

        SharedPreferences.Editor editor = getActivityEditor(context);
        editor.putString(PREFERENCE_QUESTION_ID, objectId);
        editor.putBoolean(PREFERENCE_QUESTION_NOTIFICATION, true);
        editor.apply();
        editor.commit();
        Log.d("PreferenceHelper", "write question data to preferences");

    }

    public static void writeToPreferenceQuestionNotiStatus(Context context,boolean status) {

        SharedPreferences.Editor editor = getActivityEditor(context);
        editor.putBoolean(PREFERENCE_QUESTION_NOTIFICATION, status);
        editor.apply();
        editor.commit();
    }

    public static boolean getQuestionNotiStatus(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        final boolean status = preferences.getBoolean(PREFERENCE_QUESTION_NOTIFICATION, false);
        return status;
    }

    public static void writeToPreferenceBoardNotiId(Context context,String objectId) {

        SharedPreferences.Editor editor = getActivityEditor(context);
        editor.putString(PREFERENCE_BOARD_ID, objectId);
        editor.putBoolean(PREFERENCE_BOARD_NOTIFICATION, true);
        editor.apply();
        editor.commit();
        Log.d("PreferenceHelper", "write board data to preferences");

    }

    public static void writeToPreferenceBoardNotiStatus(Context context,boolean status) {

        SharedPreferences.Editor editor = getActivityEditor(context);
        editor.putBoolean(PREFERENCE_BOARD_NOTIFICATION, status);
        editor.apply();
        editor.commit();
    }

    public static boolean getBoardNotiStatus(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        final boolean status = preferences.getBoolean(PREFERENCE_BOARD_NOTIFICATION, false);
        return status;
    }

    public static String getQuestionId(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        final String id = preferences.getString(PREFERENCE_QUESTION_ID, "default");
        return id;
    }
    public static String getBoardId(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        final String id = preferences.getString(PREFERENCE_BOARD_ID,"default");
        return id;
    }

    public static int getTagPosition(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        final int avatarPreference = preferences.getInt(PREFERENCE_TAG, 0);
        return avatarPreference;
    }

    public static int getCatPosition(Context context) {
        SharedPreferences preferences = getActivitySharedPreferences(context);
        final int categoryPreference = preferences.getInt(PREFERENCE_CATEGORY_NO, -2);
        return categoryPreference;
    }



    public static void signOut(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(PREFERENCE_FIRST_NAME);
        editor.remove(PREFERENCE_LAST_INITIAL);
        editor.remove(PREFERENCE_TAG);
        editor.apply();
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getActivityEditor(Context context) {
        SharedPreferences preferences = getActivitySharedPreferences(context);
        return preferences.edit();
    }

    private static SharedPreferences getActivitySharedPreferences(Context context) {
        return context.getSharedPreferences(USER_PREFERENCES_ACTIVITY, Context.MODE_PRIVATE);
    }

}
