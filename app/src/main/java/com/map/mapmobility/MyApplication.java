package com.map.mapmobility;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;


public class MyApplication extends Application {
    public static String TAG = "MAP_MOBILITY_DEMO_Tag";

    private int activityCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.e("tag1234", ">>>>>>>activityCount:" +  ++activityCount);
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.e("tag1234", ">>>>>>activityCount:" +  --activityCount);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }

}
