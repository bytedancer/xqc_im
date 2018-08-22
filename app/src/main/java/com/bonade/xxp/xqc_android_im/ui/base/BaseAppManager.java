package com.bonade.xxp.xqc_android_im.ui.base;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

public class BaseAppManager {

    private static final String TAG = BaseAppManager.class.getSimpleName();

    private static BaseAppManager instance = null;

    private static List<Activity> mActivities = new LinkedList<>();

    private BaseAppManager(){

    }

    public synchronized static BaseAppManager getInstance() {
        if (null == instance) {
            instance = new BaseAppManager();
        }
        return instance;
    }

    public int size() {
        return mActivities.size();
    }

    public synchronized Activity getForwordActivity() {
        return size() > 0 ? mActivities.get(size() - 1) : null;
    }

    public synchronized void addActivity(Activity activity) {
        mActivities.add(activity);
    }

    public synchronized void removeActivity(Activity activity) {
        if (mActivities.contains(activity)) {
            mActivities.remove(activity);
        }
    }

    public synchronized void clear() {
        for (int i = mActivities.size() - 1; i > -1; i--) {
            Activity activity = mActivities.get(i);
            removeActivity(activity);
            activity.finish();
            i = mActivities.size();
        }
    }

    public synchronized Activity getTop() {
        return mActivities.get(mActivities.size() - 1);
    }

    public synchronized List<Activity> getAll() {
        return mActivities;
    }

    public synchronized void clearTop() {
        for (int i = mActivities.size() - 2; i > -1; i--) {
            Activity activity = mActivities.get(i);
            removeActivity(activity);
            activity.finish();
            i = mActivities.size() - 1;
        }
    }
}
