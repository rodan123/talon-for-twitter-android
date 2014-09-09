package com.klinker.android.twitter_l.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.klinker.android.twitter_l.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter_l.settings.AppSettings;
import com.klinker.android.twitter_l.utils.NotificationUtils;
import com.klinker.android.twitter_l.utils.Utils;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class MentionsRefreshService extends IntentService {

    SharedPreferences sharedPrefs;

    public MentionsRefreshService() {
        super("MentionsRefreshService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        Context context = getApplicationContext();
        AppSettings settings = AppSettings.getInstance(context);

        // if they have mobile data on and don't want to sync over mobile data
        if (Utils.getConnectionStatus(context) && !settings.syncMobile) {
            return;
        }

        try {
            Twitter twitter = Utils.getTwitter(context, settings);

            int currentAccount = sharedPrefs.getInt("current_account", 1);

            MentionsDataSource dataSource = MentionsDataSource.getInstance(context);

            long[] lastId = dataSource.getLastIds(currentAccount);
            Paging paging;
            paging = new Paging(1, 200);
            if (lastId[0] > 0) {
                paging.sinceId(lastId[0]);
            }

            List<twitter4j.Status> statuses = twitter.getMentionsTimeline(paging);

            int inserted = MentionsDataSource.getInstance(context).insertTweets(statuses, currentAccount);

            sharedPrefs.edit().putBoolean("refresh_me", true).commit();
            sharedPrefs.edit().putBoolean("refresh_me_mentions", true).commit();

            if (settings.notifications && settings.mentionsNot && inserted > 0) {
                if (intent.getBooleanExtra("from_launcher", false)) {
                    NotificationUtils.refreshNotification(context, true);
                } else {
                    NotificationUtils.refreshNotification(context);
                }
            }

            if (settings.syncSecondMentions) {
                startService(new Intent(context, SecondMentionsRefreshService.class));
            }

        } catch (TwitterException e) {
            // Error in updating status
            Log.d("Twitter Update Error", e.getMessage());
        }
    }
}